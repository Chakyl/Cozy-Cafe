package io.github.chakyl.cozycafe.network;

import io.github.chakyl.cozycafe.CozyCafe;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = CozyCafe.MODID)
public class EvilPacketsIHateThem {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                ServerBoundOpenMenuSelectorMenuPacket.TYPE,
                ServerBoundOpenMenuSelectorMenuPacket.STREAM_CODEC,
                ServerBoundOpenMenuSelectorMenuPacket::handle
        );

        registrar.playToServer(
                ServerBoundOpenMenuStatsMenuPacket.TYPE,
                ServerBoundOpenMenuStatsMenuPacket.STREAM_CODEC,
                ServerBoundOpenMenuStatsMenuPacket::handle
        );

        registrar.playToServer(
                ServerBoundOpenCafeManagerMenuPacket.TYPE,
                ServerBoundOpenCafeManagerMenuPacket.STREAM_CODEC,
                ServerBoundOpenCafeManagerMenuPacket::handle
        );

        registrar.playToServer(
                ServerBoundToggleCafeOpenPacket.TYPE,
                ServerBoundToggleCafeOpenPacket.STREAM_CODEC,
                ServerBoundToggleCafeOpenPacket::handle
        );

        registrar.playToServer(
                ServerBoundRemoveMenuItemPacket.TYPE,
                ServerBoundRemoveMenuItemPacket.STREAM_CODEC,
                ServerBoundRemoveMenuItemPacket::handle
        );

        registrar.playToServer(
                ServerBoundRenameCafePacket.TYPE,
                ServerBoundRenameCafePacket.STREAM_CODEC,
                ServerBoundRenameCafePacket::handle
        );

        registrar.playToServer(
                ServerBoundToggleCafeAreaPacket.TYPE,
                ServerBoundToggleCafeAreaPacket.STREAM_CODEC,
                ServerBoundToggleCafeAreaPacket::handle
        );

        registrar.playToServer(
                ServerBoundClearCafePacket.TYPE,
                ServerBoundClearCafePacket.STREAM_CODEC,
                ServerBoundClearCafePacket::handle
        );

        registrar.playToClient(
                ClientBoundAddMenuItemPacket.TYPE,
                ClientBoundAddMenuItemPacket.STREAM_CODEC,
                ClientBoundAddMenuItemPacket::handle
        );

        registrar.playToClient(
                ClientBoundCafeCannotOpenPacket.TYPE,
                ClientBoundCafeCannotOpenPacket.STREAM_CODEC,
                ClientBoundCafeCannotOpenPacket::handle
        );

    }

    public static <MSG extends CustomPacketPayload> void sendToServer(MSG message) {
        PacketDistributor.sendToServer(message);
    }

    public static <MSG extends CustomPacketPayload> void sendToPlayer(MSG message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }
}
