package io.github.chakyl.cozycafe.jei;

import io.github.chakyl.cozycafe.cafemodifiers.CafeModifiers;
import io.github.chakyl.cozycafe.cafemodifiers.DecorBlock;
import io.github.chakyl.cozycafe.data.CafeMenuItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CafeDecorRecipe {
    final ItemStack item;
    final int maxUsages;
    final List<String> decorThemes;
    final Component decorName;
    final CafeModifiers allModifiers;

    public CafeDecorRecipe(DecorBlock decorBlock, ItemStack item) {
        this.item = item;
        this.maxUsages = decorBlock.getMaxUsages();
        this.decorThemes = decorBlock.getDecorThemes();
        this.decorName = decorBlock.getDecorName();
        this.allModifiers = decorBlock.getCafeModifiers();
    }
}
