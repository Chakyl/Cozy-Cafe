package io.github.chakyl.cozycafe.network;

import io.github.chakyl.cozycafe.gui.MenuSelectorMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundAddMenuItemPacket {
    private final ItemStack itemStack;

    public ClientBoundAddMenuItemPacket(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ClientBoundAddMenuItemPacket(FriendlyByteBuf buffer) {
        this.itemStack = buffer.readItem();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeItem(this.itemStack);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientPacketHandler.handleAddItem(this.itemStack));
        context.setPacketHandled(true);
    }

    private static class ClientPacketHandler {
        private static void handleAddItem(ItemStack stack) {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;
            if (player.containerMenu instanceof MenuSelectorMenu clientMenu) {
                clientMenu.addToClientMenu(stack);
            }
        }
    }
}