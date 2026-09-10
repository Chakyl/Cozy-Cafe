package io.github.chakyl.cozycafe.util;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;

import java.util.Objects;
import java.util.Optional;

public class FoodClassificationUtils {

    public static boolean dropsBowl(ItemStack stack) {
        if (stack.isEmpty()) return false;
        FoodProperties foodProperties = stack.getItem().getFoodProperties(stack, null);
        if (foodProperties == null) return false;
        Optional<ItemStack> conversion = Objects.requireNonNull(foodProperties).usingConvertsTo();
        if (conversion.isPresent() && conversion.get().is(Items.BOWL)) return true;
        return stack.hasCraftingRemainingItem() && stack.getCraftingRemainingItem().is(Items.BOWL);
    }

    public static boolean dropsBottle(ItemStack stack) {
        if (stack.isEmpty()) return false;
        FoodProperties foodProperties = stack.getItem().getFoodProperties(stack, null);
        if (foodProperties == null) return false;
        Optional<ItemStack> conversion = Objects.requireNonNull(foodProperties).usingConvertsTo();
        if (conversion.isPresent() && conversion.get().is(Items.GLASS_BOTTLE)) return true;
        return stack.hasCraftingRemainingItem() && stack.getCraftingRemainingItem().is(Items.GLASS_BOTTLE);
    }
}
