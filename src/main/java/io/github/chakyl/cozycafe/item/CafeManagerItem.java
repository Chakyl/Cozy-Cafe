package io.github.chakyl.cozycafe.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

import static io.github.chakyl.cozycafe.CozyRegistry.DataComponentsRegistry.CAFE_DATA;

public class CafeManagerItem extends BlockItem {

    public CafeManagerItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> pTooltip, TooltipFlag pFlag) {
        super.appendHoverText(pStack, pContext, pTooltip, pFlag);

        CompoundTag cafeData = pStack.get(CAFE_DATA);
        if (cafeData != null && !cafeData.isEmpty()) {

            if (cafeData.contains("cafe_name")) {
                pTooltip.add(Component.translatable("tooltip.cozycafe.cafe_manager.cafe_name", cafeData.getString("cafe_name")).withStyle(ChatFormatting.AQUA));
            }
            if (cafeData.contains("reputation")) {
                StringBuilder stars = new StringBuilder();
                stars.append("★".repeat(Math.max(0, Mth.clamp((int) Math.floor((double) cafeData.getInt("reputation") / 1000), 0, 5))));
                if (stars.isEmpty()) {
                    pTooltip.add(Component.translatable("tooltip.cozycafe.cafe_manager.no_reputation").withStyle(ChatFormatting.RED));
                } else {
                    pTooltip.add(Component.translatable("tooltip.cozycafe.cafe_manager.reputation", stars.toString()).withStyle(ChatFormatting.GOLD));
                }
            }
        } else {
            pTooltip.add(Component.translatable("tooltip.cozycafe.cafe_manager").withStyle(ChatFormatting.GRAY));
        }
    }
}