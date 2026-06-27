package io.github.chakyl.cozycafe.entities;

import com.mojang.authlib.GameProfile;
import io.github.chakyl.cozycafe.blockentities.CafeMenuBlockEntity;
import io.netty.util.internal.StringUtil;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;

public class CustomerEntity extends PathfinderMob {
    private static final EntityDataAccessor<CompoundTag> SKIN_PROFILE = SynchedEntityData.defineId(CustomerEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private GameProfile cachedProfile = null;
    private BlockPos targetMenuPos;
    private int travelTime = 0;

    public CustomerEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new NavigateToMenuGoal(this, 0.9f));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.7f));
    }
    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            this.travelTime++;
            if (this.travelTime >= CafeMenuBlockEntity.MAX_TRAVEL_TIME) {
                this.discard();
            }
        }
    }

    public void setCustomerSkinFromUsername(String name) {
        if (StringUtil.isNullOrEmpty(name) || this.level().isClientSide()) return;

        MinecraftServer server = this.level().getServer();
        if (server == null) return;

        GameProfileCache cache = server.getProfileCache();
        if (cache != null) {
            cache.getAsync(name, profile -> {
                if (profile != null && profile.isPresent()) {
                    Util.backgroundExecutor().execute(() -> {
                        GameProfile filledProfile = server.getSessionService().fillProfileProperties(profile.get(), true);
                        server.execute(() -> {
                            this.entityData.set(SKIN_PROFILE, NbtUtils.writeGameProfile(new CompoundTag(), filledProfile));
                        });
                    });
                }
            });
        }
    }


    public GameProfile getOrCreateProfile() {
        return this.cachedProfile;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        super.onSyncedDataUpdated(pKey);
        if (SKIN_PROFILE.equals(pKey)) {
            CompoundTag tag = this.entityData.get(SKIN_PROFILE);
            if (!tag.isEmpty()) {
                this.cachedProfile = NbtUtils.readGameProfile(tag);
            } else {
                this.cachedProfile = null;
            }
        }
    }

    public BlockPos getTargetMenuPos() {
        return this.targetMenuPos;
    }

    public void setTargetMenuPos(BlockPos pos) {
        this.targetMenuPos = pos;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SKIN_PROFILE, new CompoundTag());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        if (this.targetMenuPos != null) {
            nbt.put("targetMenuPos", NbtUtils.writeBlockPos(this.targetMenuPos));
        }
        nbt.putInt("travelTime", this.travelTime);
        nbt.put("customerProfile", this.entityData.get(SKIN_PROFILE));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("targetMenuPos")) {
            this.targetMenuPos = NbtUtils.readBlockPos(nbt.getCompound("targetMenuPos"));
        }
        this.travelTime = nbt.getInt("travelTime");
        if (nbt.contains("customerProfile")) {
            this.entityData.set(SKIN_PROFILE, nbt.getCompound("customerProfile"));
        }
    }

    public static class NavigateToMenuGoal extends Goal {
        private final CustomerEntity customer;
        private final double speed;
        private int timeToRecalcPath;
        // TODO: Make customers walk on the floor
        public NavigateToMenuGoal(CustomerEntity customer, double speed) {
            this.customer = customer;
            this.speed = speed;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            BlockPos target = this.customer.getTargetMenuPos();
            if (target == null) return false;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            BlockPos target = this.customer.getTargetMenuPos();
            if (target == null) return false;
            return !this.customer.getNavigation().isDone() && this.canReach(target);
        }

        @Override
        public void start() {
            this.timeToRecalcPath = 0;
        }
        private boolean canReach(BlockPos target) {
            return this.customer.distanceToSqr(target.getX() + 0.5, target.getY(), target.getZ() + 0.5) <= 4.0D;
        }

        @Override
        public void tick() {
            BlockPos target = this.customer.getTargetMenuPos();
            if (target == null) return;

            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 10 + this.customer.getRandom().nextInt(10);

                BlockPos floorTarget = target.below();
                Path path = this.customer.getNavigation().createPath(floorTarget, 1);

                if (path != null) {
                    this.customer.getNavigation().moveTo(path, this.speed);
                } else {
                    this.customer.getNavigation().moveTo(floorTarget.getX() + 0.5, floorTarget.getY(), floorTarget.getZ() + 0.5, this.speed);
                }
            }

            if (this.canReach(target)) {
                if (this.customer.level().getBlockEntity(target) instanceof CafeMenuBlockEntity cafeMenuBlockEntity) {
                    cafeMenuBlockEntity.onCustomerArrived(this.customer);
                }
            }
        }
    }
}