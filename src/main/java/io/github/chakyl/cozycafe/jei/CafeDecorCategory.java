package io.github.chakyl.cozycafe.jei;

import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.data.CafeModifier;
import io.github.chakyl.cozycafe.data.CafeThemeRegistry;
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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Objects;
import java.util.stream.Collectors;

import static com.wdiscute.libtooltips.Tooltips.resolveTagsToComponentFromTranslationKey;
import static io.github.chakyl.cozycafe.util.ModifierUtils.getModifierFlavorsTooltips;
import static io.github.chakyl.cozycafe.util.ModifierUtils.getModifierTooltips;

public class CafeDecorCategory implements IRecipeCategory<CafeDecorRecipe> {

    public static final RecipeType<CafeDecorRecipe> TYPE = RecipeType.create(CozyCafe.MODID, "cafe_decor", CafeDecorRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final Component name;

    public CafeDecorCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(177, 22);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.PAINTING));
        this.name = Component.translatable("jei.cozycafe.title.cafe_decor_category");
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
    public RecipeType<CafeDecorRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CafeDecorRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 2, 2).addItemStack(recipe.item).addRichTooltipCallback((view, tooltip) -> {
            if (recipe.allModifiers != null) {
                if (recipe.allModifiers.size() > 1 && Screen.hasShiftDown()) {
                    tooltip.add(resolveTagsToComponentFromTranslationKey("tooltip.libtooltips.generic.shift_down"));
                    tooltip.add(Component.translatable("gui.cozycafe.decor.modifiers").withStyle(ChatFormatting.GRAY));
                }
                if (recipe.allModifiers.size() < 2 || Screen.hasShiftDown()) {
                    for (CafeModifier modifier : recipe.allModifiers) {
                        tooltip.addAll(getModifierTooltips(modifier));
                        tooltip.addAll(getModifierFlavorsTooltips(modifier, true));
                    }
                } else if (recipe.allModifiers.size() > 1) {
                    tooltip.add(resolveTagsToComponentFromTranslationKey("tooltip.libtooltips.generic.shift_up"));
                }
            } else {
                tooltip.add(Component.translatable("gui.cozycafe.decor.no_modifiers").withStyle(ChatFormatting.GRAY));
            }
        }).setStandardSlotBackground();

    }

    @Override
    public void draw(CafeDecorRecipe cafeDecorRecipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(cafeDecorRecipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("jei.cozycafe.decor.max", cafeDecorRecipe.maxUsages + "x " + (!Objects.equals(cafeDecorRecipe.decorName, Component.empty()) ? Component.translatable(cafeDecorRecipe.decorName.getString()).getString() : Component.translatable(cafeDecorRecipe.item.getDescriptionId()).getString())), 24, 1, 4210752, false);
        if (!cafeDecorRecipe.decorThemes.isEmpty()) {
            StringBuilder decorList = new StringBuilder();
            for (String decor : cafeDecorRecipe.decorThemes) {
                if (CafeThemeRegistry.INSTANCE.getForID(decor) != null) {
                    if (!decorList.isEmpty()) {
                        decorList.append(", ");
                    }
                    decorList.append(CafeThemeRegistry.INSTANCE.getForID(decor).themeName().getString());
                }
            }
            if (!decorList.isEmpty()) {
                guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("gui.cozycafe.decor.themes", decorList.toString()), 24, 11, 4210752, false);
            }
        }

    }

}