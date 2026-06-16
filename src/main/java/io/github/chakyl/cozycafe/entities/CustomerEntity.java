package io.github.chakyl.cozycafe.entities;

import io.github.chakyl.cozycafe.blockentities.CafeMenuBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;

public class CustomerEntity extends PathfinderMob {
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

    public BlockPos getTargetMenuPos() {
        return this.targetMenuPos;
    }

    public void setTargetMenuPos(BlockPos pos) {
        this.targetMenuPos = pos;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        if (this.targetMenuPos != null) {
            nbt.put("targetMenuPos", NbtUtils.writeBlockPos(this.targetMenuPos));
        }
        nbt.putInt("travelTime", this.travelTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("targetMenuPos")) {
            this.targetMenuPos = NbtUtils.readBlockPos(nbt.getCompound("targetMenuPos"));
        }
        this.travelTime = nbt.getInt("travelTime");
    }

    public static class NavigateToMenuGoal extends Goal {
        private final CustomerEntity customer;
        private final double speed;
        private int timeToRecalcPath;

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