package io.github.chakyl.cozycafe.util;

import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FoodClassificationUtils {

    public static boolean dropsBowl(ItemStack stack) {
        if (stack.isEmpty()) return false;

        if (stack.getItem() instanceof BowlFoodItem) return true;

        return stack.hasCraftingRemainingItem() && stack.getCraftingRemainingItem().is(Items.BOWL);
    }

    public static boolean dropsBottle(ItemStack stack) {
        if (stack.isEmpty()) return false;

        return stack.hasCraftingRemainingItem() && stack.getCraftingRemainingItem().is(Items.GLASS_BOTTLE);
    }
}
