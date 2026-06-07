package io.github.chakyl.cozycafe.network;

import io.github.chakyl.cozycafe.gui.MenuSelectorMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

public class ServerBoundRemoveMenuItemPacket {
    int index;
    public ServerBoundRemoveMenuItemPacket(int index) {
        this.index = index;
    }

    public ServerBoundRemoveMenuItemPacket(FriendlyByteBuf buffer) {
        this.index = buffer.readInt();
    }

    public void encode(FriendlyByteBuf buffer) {
            buffer.writeInt(this.index);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player == null) return;
        if (player.containerMenu instanceof MenuSelectorMenu serverMenu) {
            serverMenu.removeFromMenu(this.index);
        }
        context.get().setPacketHandled(true);
    }

}
