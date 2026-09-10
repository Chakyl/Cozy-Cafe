package io.github.chakyl.cozycafe.network;

import io.github.chakyl.cozycafe.blockentities.CafeManagerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static io.github.chakyl.cozycafe.CozyCafe.loc;

public record ServerBoundToggleCafeAreaPacket(BlockPos pos) implements CustomPacketPayload {
    public static final Type<ServerBoundToggleCafeAreaPacket> TYPE = new Type<>(loc("show_cafe_area"));

    public static final StreamCodec<FriendlyByteBuf, ServerBoundToggleCafeAreaPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ServerBoundToggleCafeAreaPacket::pos,
            ServerBoundToggleCafeAreaPacket::new
    );

    @Override
    public Type<ServerBoundToggleCafeAreaPacket> type() {
        return TYPE;
    }

    public static void handle(ServerBoundToggleCafeAreaPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ServerLevel level = player.serverLevel();
                if (level.isLoaded(packet.pos())) {
                    BlockEntity cafeManager = level.getBlockEntity(packet.pos());
                    if (cafeManager instanceof CafeManagerBlockEntity cafeManagerBlockEntity) {
                        cafeManagerBlockEntity.toggleCafeArea();
                    }
                }
            }
        });
    }
}