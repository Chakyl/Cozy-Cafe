package io.github.chakyl.cozycafe.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CafeManagerItem extends BlockItem {

    public CafeManagerItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);

        CompoundTag tag = pStack.getTag();
        if (tag != null && tag.contains("cafeData")) {
            CompoundTag blockEntityTag = tag.getCompound("cafeData");

            if (blockEntityTag.contains("cafeName")) {
                pTooltip.add(Component.translatable("tooltip.cozycafe.cafe_manager.cafe_name", blockEntityTag.getString("cafeName")).withStyle(ChatFormatting.AQUA));
            }
            if (blockEntityTag.contains("reputation")) {
                StringBuilder stars = new StringBuilder();
                for (int i = 0; i < Mth.clamp((int) Math.floor((double) blockEntityTag.getInt("reputation") / 1000), 0, 5); i++) {
                    stars.append("★");
                }
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