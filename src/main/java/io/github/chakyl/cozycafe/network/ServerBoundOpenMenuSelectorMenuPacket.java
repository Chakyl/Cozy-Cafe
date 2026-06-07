package io.github.chakyl.cozycafe.network;

import io.github.chakyl.cozycafe.blockentities.CafeManagerBlockEntity;
import io.github.chakyl.cozycafe.gui.MenuSelectorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;
import java.util.function.Supplier;

public class ServerBoundOpenMenuSelectorMenuPacket {
    private final BlockPos pos;
    public ServerBoundOpenMenuSelectorMenuPacket(BlockPos pos) {
        this.pos = pos;
    }

    public ServerBoundOpenMenuSelectorMenuPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.pos);
    }
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                Level level = player.level();
                if (level.getBlockEntity(this.pos) instanceof CafeManagerBlockEntity cafeManagerBlockEntity) {
                    NetworkHooks.openScreen(player, new SimpleMenuProvider((cId, inv, playerEntity) -> new MenuSelectorMenu(cId, inv, cafeManagerBlockEntity),
                            Component.translatable("container.cozycafe.menu_selector")
                    ), buffer -> {
                        buffer.writeBlockPos(this.pos);
                        List<ItemStack> menuList = cafeManagerBlockEntity.getMenu();
                        if (menuList == null) {
                            buffer.writeInt(0);
                        } else {
                            buffer.writeInt(menuList.size());
                            for (ItemStack stack : menuList) {
                                buffer.writeItem(stack);
                            }
                        }
                    });

                }
            }
        });
        context.setPacketHandled(true);
    }

}
