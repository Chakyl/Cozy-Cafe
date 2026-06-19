package io.github.chakyl.cozycafe.item;

import io.github.chakyl.cozycafe.registry.CozyRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        // TODO: freshness?
    }

    public boolean checkFreshness(ItemStack stack, Level level) {
        if (stack.hasTag() && stack.getTag().contains("platedTime")) {
            long platedTime = stack.getTag().getLong("platedTime");
            long age = level.getGameTime() - platedTime;
            long maxFreshnessTicks = 24000;
            if (age >= maxFreshnessTicks) {
                stack.getTag().remove("platedTime");
                if (stack.getTag().isEmpty()) {
                    stack.setTag(null);
                }
                return false;
            }
            return true;
        }
        return false;
    }
}