package io.github.chakyl.cozycafe.network;

import io.github.chakyl.cozycafe.CozyCafe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class EvilPacketsIHateThem {
    private static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder.named(
                    new ResourceLocation(CozyCafe.MODID, "main"))
            .serverAcceptedVersions((version) -> true)
            .clientAcceptedVersions((version) -> true)
            .networkProtocolVersion(() -> String.valueOf(1))
            .simpleChannel();

    public static void register() {
        INSTANCE.messageBuilder(ServerBoundOpenMenuSelectorMenuPacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerBoundOpenMenuSelectorMenuPacket::encode)
                .decoder(ServerBoundOpenMenuSelectorMenuPacket::new)
                .consumerMainThread(ServerBoundOpenMenuSelectorMenuPacket::handle)
                .add();

        INSTANCE.messageBuilder(ServerBoundOpenCafeManagerMenuPacket.class, 1, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerBoundOpenCafeManagerMenuPacket::encode)
                .decoder(ServerBoundOpenCafeManagerMenuPacket::new)
                .consumerMainThread(ServerBoundOpenCafeManagerMenuPacket::handle)
                .add();

        INSTANCE.messageBuilder(ServerBoundToggleCafeOpenPacket.class, 2, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerBoundToggleCafeOpenPacket::encode)
                .decoder(ServerBoundToggleCafeOpenPacket::new)
                .consumerMainThread(ServerBoundToggleCafeOpenPacket::handle)
                .add();

        INSTANCE.messageBuilder(ClientBoundAddMenuItemPacket.class, 3, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientBoundAddMenuItemPacket::encode)
                .decoder(ClientBoundAddMenuItemPacket::new)
                .consumerMainThread(ClientBoundAddMenuItemPacket::handle)
                .add();

        INSTANCE.messageBuilder(ClientBoundCafeCannotOpenPacket.class, 4, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientBoundCafeCannotOpenPacket::encode)
                .decoder(ClientBoundCafeCannotOpenPacket::new)
                .consumerMainThread(ClientBoundCafeCannotOpenPacket::handle)
                .add();

        INSTANCE.messageBuilder(ServerBoundRemoveMenuItemPacket.class, 5, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerBoundRemoveMenuItemPacket::encode)
                .decoder(ServerBoundRemoveMenuItemPacket::new)
                .consumerMainThread(ServerBoundRemoveMenuItemPacket::handle)
                .add();

        INSTANCE.messageBuilder(ServerBoundRenameCafePacket.class, 6, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerBoundRenameCafePacket::encode)
                .decoder(ServerBoundRenameCafePacket::new)
                .consumerMainThread(ServerBoundRenameCafePacket::handle)
                .add();

        INSTANCE.messageBuilder(ServerBoundShowCafeAreaPacket.class, 7, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerBoundShowCafeAreaPacket::encode)
                .decoder(ServerBoundShowCafeAreaPacket::new)
                .consumerMainThread(ServerBoundShowCafeAreaPacket::handle)
                .add();

        INSTANCE.messageBuilder(ServerBoundClearCafePacket.class, 8, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerBoundClearCafePacket::encode)
                .decoder(ServerBoundClearCafePacket::new)
                .consumerMainThread(ServerBoundClearCafePacket::handle)
                .add();


    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
