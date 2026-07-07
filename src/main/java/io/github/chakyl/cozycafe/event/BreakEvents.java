package io.github.chakyl.cozycafe.event;

import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.blockentities.CafeManagerBlockEntity;
import io.github.chakyl.cozycafe.blockentities.CafeMenuBlockEntity;
import io.github.chakyl.cozycafe.blockentities.CafeSignBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CozyCafe.MODID)
public class BreakEvents {


    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof Level level)) return;

        BlockEntity blockEntity = level.getBlockEntity(event.getPos());
        boolean shouldCancel = false;

        if (blockEntity instanceof CafeManagerBlockEntity cafeManager) {
            shouldCancel = cafeManager.isOpen();
        } else if (blockEntity instanceof CafeSignBlockEntity cafeSign) {
            if (cafeSign.getLinkedManager() != null && level.getBlockEntity(cafeSign.getLinkedManager()) instanceof CafeManagerBlockEntity linkedManager) {
                shouldCancel = linkedManager.isOpen();
            }
        } else if (blockEntity instanceof CafeMenuBlockEntity cafeMenu) {
            if (cafeMenu.getCafeManager() != null && level.getBlockEntity(cafeMenu.getCafeManager()) instanceof CafeManagerBlockEntity linkedManager) {
                shouldCancel = linkedManager.isOpen();
            }
        }

        if (shouldCancel) {
            if (event.getPlayer() != null)
                event.getPlayer().sendSystemMessage(Component.translatable("block.cozycafe.any.cannot_break").withStyle(ChatFormatting.RED));
            event.setCanceled(true);
        }
    }
}