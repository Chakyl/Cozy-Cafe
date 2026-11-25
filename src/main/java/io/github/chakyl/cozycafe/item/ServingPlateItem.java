package io.github.chakyl.cozycafe.item;

import io.github.chakyl.cozycafe.CozyRegistry;
import io.github.chakyl.cozycafe.data.CafeMenuItem;
import io.github.chakyl.cozycafe.data.CafeMenuItemRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

import java.util.List;

import static io.github.chakyl.cozycafe.CozyRegistry.DataComponentsRegistry.PLATED_FOOD;

public class ServingPlateItem extends Item {

    public ServingPlateItem(Properties properties) {
        super(properties);
    }

    public static ItemStack createPlatedFood(ItemStack food) {
        ItemStack platedResult = new ItemStack(CozyRegistry.ItemRegistry.SERVING_PLATE.get());
        platedResult.set(PLATED_FOOD, ItemContainerContents.fromItems(List.of(food)));
        return platedResult;
    }

    public static ItemStack getStoredFood(ItemStack platedFood) {
        if (!platedFood.has(PLATED_FOOD)) return ItemStack.EMPTY;
        ItemContainerContents container = platedFood.get(PLATED_FOOD);
        if (container == null || container == ItemContainerContents.EMPTY) return ItemStack.EMPTY;

        return container.getStackInSlot(0);
    }
    public static CafeMenuItem getCafeMenuItem(ItemStack platedFood) {
        ItemStack food = getStoredFood(platedFood);
        if (food.isEmpty()) return null;
        return CafeMenuItemRegistry.INSTANCE.getForItem(food.getItem());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack plateStack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            ItemStack storedFood = getStoredFood(plateStack);
            if (!storedFood.isEmpty()) {
                if (!level.isClientSide) {
                    if (!player.getInventory().add(storedFood.copy())) {
                        player.drop(storedFood.copy(), false);
                    }

                    ItemStack dirtyPlate = new ItemStack(CozyRegistry.ItemRegistry.DIRTY_SERVING_PLATE.get());
                    if (plateStack.getCount() == 1) {
                        player.setItemInHand(hand, dirtyPlate);
                        plateStack = dirtyPlate;
                    } else {
                        plateStack.shrink(1);
                        if (!player.getInventory().add(dirtyPlate)) {
                            player.drop(dirtyPlate, false);
                        }
                    }
                }

                player.playSound(net.minecraft.sounds.SoundEvents.ITEM_PICKUP, 0.2F, (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2F + 1.0F);
                player.inventoryMenu.broadcastChanges();
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