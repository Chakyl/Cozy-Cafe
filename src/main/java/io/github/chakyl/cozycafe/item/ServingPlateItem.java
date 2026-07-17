package io.github.chakyl.cozycafe.item;

import io.github.chakyl.cozycafe.registry.CozyRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack plateStack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            ItemStack storedFood = getStoredFood(plateStack);
            if (!storedFood.isEmpty()) {
                if (!level.isClientSide) {
                    if (!player.getInventory().add(storedFood)) {
                        player.drop(storedFood, false);
                    }

                    if (plateStack.getCount() >= 1) {
                        plateStack.shrink(1);

                    } else {
                        plateStack.setTag(null);
                    }

                    ItemStack dirtyPlate = new ItemStack(CozyRegistry.ItemRegistry.DIRTY_SERVING_PLATE.get());
                    if (!player.getInventory().add(dirtyPlate)) {
                        player.drop(dirtyPlate, false);
                    }
                }

                player.playSound(net.minecraft.sounds.SoundEvents.ITEM_PICKUP, 0.2F, (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2F + 1.0F);

                return InteractionResultHolder.sidedSuccess(plateStack, level.isClientSide());
            }
        }

        return super.use(level, player, hand);
    }

    @Override
    public Component getName(ItemStack stack) {
        ItemStack food = getStoredFood(stack);
        if (!food.isEmpty()) return Component.translatable("item.cozycafe.serving_plate.served", food.getHoverName());

        return super.getName(stack);
    }
}