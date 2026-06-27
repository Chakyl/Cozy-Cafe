package io.github.chakyl.cozycafe.network;

import io.github.chakyl.cozycafe.gui.MenuSelectorMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

public class ServerBoundOpenCafeManagerMenuPacket {
    public ServerBoundOpenCafeManagerMenuPacket() {
    }

    public ServerBoundOpenCafeManagerMenuPacket(FriendlyByteBuf buffer) {
    }

    public void encode(FriendlyByteBuf buffer) {
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            if (player.containerMenu instanceof MenuSelectorMenu menu && menu.stillValid(player)) {
                NetworkHooks.openScreen(player, new SimpleMenuProvider((cId, inv, playerEntity) -> new MenuSelectorMenu(cId, inv, menu.blockEntity), Component.translatable("container.cozycafe.cafe_manager")));
            }
        }
        context.get().setPacketHandled(true);
    }

}
