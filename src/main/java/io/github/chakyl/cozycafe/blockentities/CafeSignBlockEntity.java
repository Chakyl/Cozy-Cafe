package io.github.chakyl.cozycafe.blockentities;

import io.github.chakyl.cozycafe.registry.CozyRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static io.github.chakyl.cozycafe.blocks.CafeSignBlock.OPEN;

public class CafeSignBlockEntity extends BlockEntity {
    private BlockPos linkedManager;

    public CafeSignBlockEntity(BlockPos pos, BlockState state) {
        super(CozyRegistry.BlockEntityRegistry.CAFE_SIGN.get(), pos, state);
    }

    public BlockPos getLinkedManager() {
        return linkedManager;
    }

    public void setLinkedManager(BlockPos linkedManager) {
        this.linkedManager = linkedManager;
    }

    public void setOpen(boolean open) {
        BlockState currentState = this.level.getBlockState(this.getBlockPos());
        if (currentState.hasProperty(OPEN)) {
            this.level.setBlock(this.getBlockPos(), currentState.setValue(OPEN, open), 3);
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
            this.setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        if (this.linkedManager != null) {
            nbt.put("linkedManager", NbtUtils.writeBlockPos(this.linkedManager));
        }
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        if (nbt.contains("linkedManager")) {
            this.linkedManager = NbtUtils.readBlockPos(nbt.getCompound("linkedManager"));
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
