package io.github.chakyl.cozycafe.jei;

import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.CozyRegistry;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.stream.Collectors;

public class CafeMenuItemCategory implements IRecipeCategory<CafeMenuItemRecipe> {

    public static final RecipeType<CafeMenuItemRecipe> TYPE = RecipeType.create(CozyCafe.MODID, "menu_items", CafeMenuItemRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final Component name;

    public CafeMenuItemCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(177, 22);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(CozyRegistry.ItemRegistry.SERVING_PLATE.value()));
        this.name = Component.translatable("jei.cozycafe.title.menu_item_category");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }


    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public Component getTitle() {
        return this.name;
    }

    @Override
    public RecipeType<CafeMenuItemRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CafeMenuItemRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 2, 2).addItemStack(recipe.dish).addRichTooltipCallback((view, tooltip) -> {
            tooltip.add(Component.translatable("gui.cozycafe.menu_selector.flavors", recipe.flavors.stream()
                    .map(flavor -> Component.translatable("flavor.cozycafe." + flavor).getString())
                    .collect(Collectors.joining(", "))).withStyle(ChatFormatting.GRAY));
        }).setStandardSlotBackground();

    }

    @Override
    public void draw(CafeMenuItemRecipe cafeMenuItemRecipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(cafeMenuItemRecipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        guiGraphics.drawString(Minecraft.getInstance().font, Component.literal(Component.translatable("jei.cozycafe.menu_item_category.price", cafeMenuItemRecipe.price).getString() + " | " + Component.translatable("category.cozycafe." + cafeMenuItemRecipe.category.toString().toLowerCase()).getString()), 16, 1, 4210752, false);
        if (cafeMenuItemRecipe.bowlFood)
            guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("gui.cozycafe.menu_selector.bowl_food").withStyle(ChatFormatting.ITALIC), 24, 11, 4210752, false);
        if (cafeMenuItemRecipe.bottleDrink)
            guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("gui.cozycafe.menu_selector.bottle_drink").withStyle(ChatFormatting.ITALIC), 24, 11, 4210752, false);

    }

}