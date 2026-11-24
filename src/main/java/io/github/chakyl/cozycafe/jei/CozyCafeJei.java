package io.github.chakyl.cozycafe.jei;

import io.github.chakyl.cozycafe.CozyRegistry;
import io.github.chakyl.cozycafe.DataMapRegistry;
import io.github.chakyl.cozycafe.data.CafeMenuItem;
import io.github.chakyl.cozycafe.data.CafeMenuItemRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static io.github.chakyl.cozycafe.CozyCafe.loc;


@JeiPlugin
public class CozyCafeJei implements IModPlugin {

    public static final ResourceLocation UID = loc("plugin");

    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {
        reg.addRecipeCategories(new CafeMenuItemCategory(reg.getJeiHelpers().getGuiHelper()));
        reg.addRecipeCategories(new CafeDecorCategory(reg.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<CafeMenuItemRecipe> menuItemRecipes = new ArrayList<>();
        for (CafeMenuItem recipe : CafeMenuItemRegistry.INSTANCE.getValues()) {
            menuItemRecipes.add(new CafeMenuItemRecipe(recipe));
        }
        menuItemRecipes.sort(Comparator.comparing((CafeMenuItemRecipe r) -> r.category).thenComparingInt(r -> r.price).reversed());
        registration.addRecipes(CafeMenuItemCategory.TYPE, menuItemRecipes);
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        List<CafeDecorRecipe> decorRecipes = new ArrayList<>();
        Registry<Block> blockRegistry = level.registryAccess().registryOrThrow(Registries.BLOCK);

        blockRegistry.getDataMap(DataMapRegistry.DECOR).forEach((blockKey, value) -> {
            blockRegistry.getOptional(blockKey).ifPresent(block -> {
                Item item = block.asItem();
                if (item != Items.AIR) {
                    decorRecipes.add(new CafeDecorRecipe(value, item.getDefaultInstance()));
                }
            });
        });
        registration.addRecipes(CafeDecorCategory.TYPE, decorRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        reg.addRecipeCatalyst(new ItemStack(CozyRegistry.ItemRegistry.SERVING_PLATE.value()), CafeMenuItemCategory.TYPE);
        reg.addRecipeCatalyst(new ItemStack(CozyRegistry.BlockRegistry.CAFE_MANAGER.value()), CafeMenuItemCategory.TYPE);
        reg.addRecipeCatalyst(new ItemStack(CozyRegistry.BlockRegistry.CAFE_MANAGER.value()), CafeDecorCategory.TYPE);
    }

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

}