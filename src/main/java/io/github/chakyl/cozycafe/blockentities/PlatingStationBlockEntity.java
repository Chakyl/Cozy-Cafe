package io.github.chakyl.cozycafe.blockentities;

import io.github.chakyl.cozycafe.item.ServingPlateItem;
import io.github.chakyl.cozycafe.registry.CozyRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PlatingStationBlockEntity extends BlockEntity {
    private ItemStack plateItem = ItemStack.EMPTY;

    public PlatingStationBlockEntity(BlockPos pos, BlockState state) {
        super(CozyRegistry.BlockEntityRegistry.PLATING_STATION.get(), pos, state);
    }

    public ItemStack getPlateItem() {
        return this.plateItem;
    }

    // evil method
    public InteractionResult handlePlating(Level level, Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (this.plateItem.isEmpty()) {
            if (heldItem.is(CozyRegistry.ItemRegistry.SERVING_PLATE.get())) {
                if (!level.isClientSide) {
                    this.plateItem = heldItem.split(1);
                    this.setChangedForRender();
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        } else if (this.plateItem.is(CozyRegistry.ItemRegistry.SERVING_PLATE.get())) {
            if (!heldItem.isEmpty() && heldItem.getItem().isEdible()) {
                if (!level.isClientSide) {
                    this.plateItem = ServingPlateItem.createPlatedFood(heldItem.split(1));
                    this.setChangedForRender();
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (heldItem.isEmpty() && !level.isClientSide) {
                player.setItemInHand(hand, this.plateItem.copy());
                this.plateItem = ItemStack.EMPTY;
                this.setChangedForRender();
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else {
            if (!level.isClientSide) {
                if (!player.getInventory().add(this.plateItem)) {
                    player.drop(this.plateItem, false);
                }
                this.plateItem = ItemStack.EMPTY;
                this.setChangedForRender();
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return null;
    }

    private void setChangedForRender() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("plateItem", this.plateItem.save(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.plateItem = ItemStack.of(tag.getCompound("plateItem"));
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