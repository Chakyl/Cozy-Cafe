package io.github.chakyl.cozycafe.jei;

import io.github.chakyl.cozycafe.data.CafeMenuItem;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CafeMenuItemRecipe {
    final ItemStack dish;
    final CafeMenuItem.MenuItemCategory category;
    final int price;
    final boolean bowlFood;
    final boolean bottleDrink;
    final List<String> flavors;

    public CafeMenuItemRecipe(CafeMenuItem menuItem) {
        this.dish = menuItem.item().getDefaultInstance();
        this.category = menuItem.category();
        this.price = menuItem.price();
        this.bowlFood = menuItem.bowlFood();
        this.bottleDrink = menuItem.bottleDrink();
        this.flavors = menuItem.flavors();
    }
}
