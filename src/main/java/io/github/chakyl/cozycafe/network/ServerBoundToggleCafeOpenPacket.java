package io.github.chakyl.cozycafe.network;

import io.github.chakyl.cozycafe.blockentities.CafeManagerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerBoundToggleCafeOpenPacket {
    private final BlockPos pos;
    private final boolean isOpen;

    public ServerBoundToggleCafeOpenPacket(BlockPos pos, boolean isOpen) {
        this.pos = pos;
        this.isOpen = isOpen;
    }

    public ServerBoundToggleCafeOpenPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.isOpen = buffer.readBoolean();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.pos);
        buffer.writeBoolean(this.isOpen);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                ServerLevel level = player.serverLevel();
                if (level.isLoaded(this.pos)) {
                    BlockEntity cafeManager = level.getBlockEntity(this.pos);
                    if (cafeManager instanceof CafeManagerBlockEntity cafeManagerBlockEntity) {
                        cafeManagerBlockEntity.setOpen(this.isOpen, true);
                    }
                }
            }
        });
        return true;
    }
}