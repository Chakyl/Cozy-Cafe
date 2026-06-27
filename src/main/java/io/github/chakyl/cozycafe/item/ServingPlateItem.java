package io.github.chakyl.cozycafe.item;

import io.github.chakyl.cozycafe.registry.CozyRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ServingPlateItem extends Item {
    public static String PLATED_FOOD = "platedFood";

    public ServingPlateItem(Properties properties) {
        super(properties);
    }

    public static ItemStack createPlatedFood(ItemStack food) {
        ItemStack platedResult = new ItemStack(CozyRegistry.ItemRegistry.SERVING_PLATE.get());
        CompoundTag tag = new CompoundTag();
        tag.put(PLATED_FOOD, food.save(new CompoundTag()));
        platedResult.setTag(tag);

        return platedResult;
    }

    public static ItemStack getStoredFood(ItemStack platedFood) {
        if (platedFood.hasTag() && platedFood.getTag().contains(PLATED_FOOD)) {
            return ItemStack.of(platedFood.getTag().getCompound(PLATED_FOOD));
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Component getName(ItemStack stack) {
        ItemStack food = getStoredFood(stack);
        if (!food.isEmpty()) return Component.translatable("item.cozycafe.serving_plate.served", food.getHoverName());

        return super.getName(stack);
    }
}