package io.github.chakyl.cozycafe.event;


import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.CozyRegistry;
import io.github.chakyl.cozycafe.DataMapRegistry;
import io.github.chakyl.cozycafe.cafemodifiers.DecorBlock;
import io.github.chakyl.cozycafe.data.CafeMenuItem;
import io.github.chakyl.cozycafe.data.CafeMenuItemRegistry;
import io.github.chakyl.cozycafe.data.CafeModifier;
import io.github.chakyl.cozycafe.data.CafeModifierRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;
import java.util.Objects;

import static com.wdiscute.libtooltips.Tooltips.resolveTagsToComponentFromTranslationKey;
import static io.github.chakyl.cozycafe.util.GeneralUtils.getMenuItemTooltip;
import static io.github.chakyl.cozycafe.util.ModifierUtils.*;

@EventBusSubscriber(modid = CozyCafe.MODID, value = Dist.CLIENT)
public class TooltipEvents {
    @SubscribeEvent
    public static void decorTooltipEvent(ItemTooltipEvent event) {
        if (event.getEntity().getItemInHand(InteractionHand.OFF_HAND).getItem() != CozyRegistry.ItemRegistry.CAFE_CATALOG.get())
            return;
        List<Component> comp = event.getToolTip();
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof BlockItem blockItem) {
            DecorBlock decorBlock = BuiltInRegistries.BLOCK.wrapAsHolder(blockItem.getBlock()).getData(DataMapRegistry.DECOR);
            if (decorBlock == null) return;
            Component decorTT = getDecorThemes(decorBlock);
            if (decorTT != null) comp.add(decorTT);
            comp.add(Component.translatable("gui.cozycafe.decor.max", decorBlock.getMaxUsages() + "x " + (!Objects.equals(decorBlock.getDecorName(), Component.empty()) ? Component.translatable(decorBlock.getDecorName().getString()).getString() : Component.translatable(blockItem.getDescriptionId()).getString())).withStyle(ChatFormatting.RED));
            if (decorBlock.getCafeModifiers() != null) {
                if (decorBlock.getCafeModifiers().size() > 1 && Screen.hasShiftDown()) {
                    comp.add(resolveTagsToComponentFromTranslationKey("tooltip.libtooltips.generic.shift_down"));
                    comp.add(Component.translatable("gui.cozycafe.decor.modifiers").withStyle(ChatFormatting.GRAY));
                }
                if (decorBlock.getCafeModifiers().size() < 2 || Screen.hasShiftDown()) {
                    for (CafeModifier modifier : decorBlock.getCafeModifiers()) {
                        comp.addAll(getModifierTooltips(modifier));
                        comp.addAll(getModifierFlavorsTooltips(modifier, true));
                    }
                } else if (decorBlock.getCafeModifiers().size() > 1) {
                    comp.add(resolveTagsToComponentFromTranslationKey("tooltip.libtooltips.generic.shift_up"));
                }
            }

        }
    }

    @SubscribeEvent
    public static void menuItemTooltipEvent(ItemTooltipEvent event) {
        if (event.getEntity().getItemInHand(InteractionHand.OFF_HAND).getItem() != CozyRegistry.ItemRegistry.CAFE_CATALOG.get())
            return;
        List<Component> comp = event.getToolTip();
        ItemStack stack = event.getItemStack();
        if (!CafeMenuItemRegistry.INSTANCE.isMenuItem(stack.getItem())) return;
        CafeMenuItem cafeMenuItem = CafeMenuItemRegistry.INSTANCE.getForItem(stack.getItem());
        if (cafeMenuItem == null) return;
        comp.addAll(getMenuItemTooltip(List.of(), cafeMenuItem.item().getDefaultInstance(), cafeMenuItem));
    }
}