package io.github.chakyl.cozycafe.blockentities;

import io.github.chakyl.cozycafe.blocks.CafeMenuBlock;
import io.github.chakyl.cozycafe.entities.CustomerEntity;
import io.github.chakyl.cozycafe.registry.CozyRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class CafeMenuBlockEntity extends BlockEntity {
    private static int MAX_WAIT_TIME = 600;
    private int currentCourse = 0;
    private int waitTime = -1;
    private boolean hasCustomer = false;
    private ItemStack requestedItem = ItemStack.EMPTY;

    public CafeMenuBlockEntity(BlockPos pos, BlockState state) {
        super(CozyRegistry.BlockEntityRegistry.CAFE_MENU.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide()) {
            if (!requestedItem.isEmpty()) {
                if (waitTime == -1) {
                    this.waitTime = 0;
                    this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
                } else {
                    if (waitTime == MAX_WAIT_TIME) {
                        // TODO: Signal to Cafe Manager
                        this.closeMenu();
                    } else {
                        waitTime++;
                        this.setChanged();
                        if (this.waitTime >= 300 && this.level.getGameTime() % 100 == 0) {
                            this.setChanged();
                            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
                        }
                    }
                }

            }
        }
    }

    public void handleServe(Level pLevel, BlockPos pPos, Player pPlayer, ItemStack handStack) {
        if (handStack.is(requestedItem.getItem())) {
            if (!pPlayer.isCreative()) handStack.shrink(1);
            this.waitTime = -1;
            this.setCurrentCourse(this.currentCourse + 1);
            this.setRequestedItem(ItemStack.EMPTY);
            ((ServerLevel) level).sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    pPos.getX() + 0.5, pPos.getY() + 0.5, pPos.getZ() + 0.5,
                    5,
                    0.5, 0.5, 0.5,
                    1.0
            );
            Minecraft.getInstance().player.playSound(SoundEvents.NOTE_BLOCK_CHIME.get(), 1.0F, 1.0F);
        } else {
            Minecraft.getInstance().player.playSound(SoundEvents.NOTE_BLOCK_BASS.get(), 1.0F, 1.0F);
        }
    }

    public boolean canServe() {
        return !requestedItem.isEmpty();
    }

    public boolean canReceiveNewChoice() {
        return this.hasCustomer && requestedItem.isEmpty() && waitTime == -1;
    }

    public static int getMaxWaitTime() {
        return MAX_WAIT_TIME;
    }

    public static void setMaxWaitTime(int maxWaitTime) {
        MAX_WAIT_TIME = maxWaitTime;
    }

    public int getCurrentCourse() {
        return currentCourse;
    }

    public void setCurrentCourse(int currentCourse) {
        if (currentCourse < 3) {
            this.currentCourse = currentCourse;
            this.setChanged();
        } else {
            this.closeMenu();
            // TODO: Make customer leave
            this.currentCourse = 0;
        }
    }

    public void closeMenu() {
        this.waitTime = -1;
        this.currentCourse = 0;
        this.hasCustomer = false;
        this.setRequestedItem(ItemStack.EMPTY);
    }


    public void onCustomerArrived(PathfinderMob customer) {
        if (!this.level.isClientSide() && !this.hasCustomer) {
            if (customer instanceof CustomerEntity customerEntity) {
                customerEntity.setRemoved(Entity.RemovalReason.UNLOADED_TO_CHUNK);
            }
            this.hasCustomer = true;
            this.setChanged();
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public int getWaitTime() {
        return this.waitTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public ItemStack getRequestedItem() {
        return requestedItem;
    }

    public void setRequestedItem(ItemStack requestedItem) {
        this.requestedItem = requestedItem;
        this.setChanged();
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    public boolean getHasCustomer() {
        return this.hasCustomer;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(this.worldPosition).expandTowards(0, 1.5, 0).inflate(0.5, 0, 0.5);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("currentCourse", this.currentCourse);
        tag.putInt("waitTime", this.waitTime);
        tag.putBoolean("hasCustomer", this.hasCustomer);
        if (!this.requestedItem.isEmpty()) {
            tag.put("requestedItem", this.requestedItem.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.currentCourse = tag.getInt("currentCourse");
        this.waitTime = tag.getInt("waitTime");
        this.hasCustomer = tag.getBoolean("hasCustomer");
        if (tag.contains("requestedItem")) {
            this.requestedItem = ItemStack.of(tag.getCompound("requestedItem"));
        } else {
            this.requestedItem = ItemStack.EMPTY;
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        this.load(tag);

    }
}
