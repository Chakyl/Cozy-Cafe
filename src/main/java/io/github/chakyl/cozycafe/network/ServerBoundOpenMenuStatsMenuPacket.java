package io.github.chakyl.cozycafe.network;

import io.github.chakyl.cozycafe.blockentities.CafeManagerBlockEntity;
import io.github.chakyl.cozycafe.cafemodifiers.CafeModifiers;
import io.github.chakyl.cozycafe.data.CafeTheme;
import io.github.chakyl.cozycafe.gui.CafeStatsMenu;
import io.github.chakyl.cozycafe.gui.MenuSelectorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.Optional;

import static io.github.chakyl.cozycafe.CozyCafe.loc;

public record ServerBoundOpenMenuStatsMenuPacket(BlockPos pos) implements CustomPacketPayload {
    public static final Type<ServerBoundOpenMenuStatsMenuPacket> TYPE = new Type<>(loc("open_cafe_stats_menu"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundOpenMenuStatsMenuPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ServerBoundOpenMenuStatsMenuPacket::pos,
            ServerBoundOpenMenuStatsMenuPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerBoundOpenMenuStatsMenuPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Level level = player.level();
                if (level.getBlockEntity(packet.pos()) instanceof CafeManagerBlockEntity cafeManagerBlockEntity) {
                    cafeManagerBlockEntity.saveDecorData(level, packet.pos, level.getBlockState(packet.pos));
                    player.openMenu(new SimpleMenuProvider((cId, inv, playerEntity) -> new CafeStatsMenu(cId, inv, cafeManagerBlockEntity), Component.translatable("container.cozycafe.cafe_stats")),
                            buffer -> {
                                ByteBufCodecs.optional(ByteBufCodecs.fromCodec(CafeTheme.CODEC)).encode(buffer, Optional.ofNullable(cafeManagerBlockEntity.getActiveTheme()));
                                ByteBufCodecs.optional(ByteBufCodecs.fromCodec(CafeModifiers.CODEC)).encode(buffer, Optional.ofNullable(cafeManagerBlockEntity.getCafeModifiers()));
                            }
                    );
                }
            }
        });
    }

}
