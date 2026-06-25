package io.github.chakyl.cozycafe.network;

import io.github.chakyl.cozycafe.blockentities.CafeManagerBlockEntity;
import io.github.chakyl.cozycafe.gui.CafeManagerScreen;
import io.github.chakyl.cozycafe.gui.MenuSelectorMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundCafeCannotOpenPacket {
    private final Byte errorCode;

    public ClientBoundCafeCannotOpenPacket(Byte errorCode) {
        this.errorCode = errorCode;
    }

    public ClientBoundCafeCannotOpenPacket(FriendlyByteBuf buffer) {
        this.errorCode = buffer.readByte();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeByte(this.errorCode);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if (net.minecraft.client.Minecraft.getInstance().screen instanceof CafeManagerScreen screen) {
                screen.setErrorMessage(this.errorCode);
            }
        });
        context.setPacketHandled(true);
    }

}