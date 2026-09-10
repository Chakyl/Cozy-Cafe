package io.github.chakyl.cozycafe.gui;


import com.mojang.blaze3d.systems.RenderSystem;
import io.github.chakyl.cozycafe.cafemodifiers.CafeModifierActions;
import io.github.chakyl.cozycafe.cafemodifiers.CafeModifiers;
import io.github.chakyl.cozycafe.data.CafeMenuItem;
import io.github.chakyl.cozycafe.data.CafeMenuItemRegistry;
import io.github.chakyl.cozycafe.data.CafeModifier;
import io.github.chakyl.cozycafe.data.CafeTheme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static io.github.chakyl.cozycafe.CozyCafe.loc;
import static io.github.chakyl.cozycafe.util.GeneralUtils.getMenuItemTooltip;
import static io.github.chakyl.cozycafe.util.ModifierUtils.*;

@OnlyIn(Dist.CLIENT)
public class CafeStatsScreen extends AbstractContainerScreen<CafeStatsMenu> {

    private static final ResourceLocation GUI_LOCATION = loc("textures/gui/cafe_stats.png");
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int SELL_ITEM_1_X = 5;
    private static final int SELL_ITEM_2_X = 35;
    private static final int BUY_ITEM_X = 68;
    private static final int LABEL_Y = 6;
    private static final int NUMBER_OF_MODIFIERS = 5;
    private static final int MODIFIER_HEIGHT = 17;
    private static final int MODIFIER_WIDTH = 120;
    private static final int MENU_BUTTON_WIDTH = 87;
    private static final int SCROLLER_HEIGHT = 22;
    private static final int SCROLLER_WIDTH = 6;
    private static final int SCROLL_BAR_HEIGHT = MODIFIER_HEIGHT * NUMBER_OF_MODIFIERS;
    private static final int SCROLL_BAR_TOP_POS_Y = 122;
    private static final int SCROLL_BAR_START_X = 155;
    private static final int LIMIT_ICON_START_X = 320;
    private static final int LIMIT_ICON_SIZE = 9;
    int scrollOff;
    private boolean isDragging;
    private CafeModifiers cafeModifiers;
    private HashMap<String, Integer> modifierCounts;

    public CafeStatsScreen(CafeStatsMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 176;
        this.imageHeight = 225;
    }

    @Override
    protected void init() {
        super.init();
        int leftPos = this.getGuiLeft();
        int topPos = this.getGuiTop();
        int offset = topPos + 22;
        cafeModifiers = this.menu.getCafeModifiers();
        modifierCounts = getModifierCounts(this.menu.getCafeModifiers());
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
    }

    private void renderModifierActions(GuiGraphics gfx, int startX, int startY, CafeModifierActions cafeModifierActions) {
        int actionOffset = startY;
        int offsetAmount = 11;
        if (!cafeModifierActions.getTip().isEmpty()) {
            gfx.drawString(this.font, Component.translatable("gui.cozycafe.modifier.tip", cafeModifierActions.getTip().getTextRepresentation()), startX, actionOffset, 0xFFFFFF, true);
            actionOffset += offsetAmount;
        }
        if (!cafeModifierActions.getPrice().isEmpty()) {
            gfx.drawString(this.font, Component.translatable("gui.cozycafe.modifier.price", cafeModifierActions.getPrice().getTextRepresentation()), startX, actionOffset, 0xFFFFFF, true);
            actionOffset += offsetAmount;
        }
        if (!cafeModifierActions.getCustomerImpact().isEmpty()) {
            gfx.drawString(this.font, Component.translatable("gui.cozycafe.modifier.customer_impact", cafeModifierActions.getCustomerImpact().getTextRepresentation()), startX, actionOffset, 0xFFFFFF, true);
            actionOffset += offsetAmount;
        }
        if (!cafeModifierActions.getPatience().isEmpty()) {
            gfx.drawString(this.font, Component.translatable("gui.cozycafe.modifier.patience", cafeModifierActions.getPatience().getTextRepresentation()), startX, actionOffset, 0xFFFFFF, true);
        }
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float pPartialTick, int pMouseX, int pMouseY) {
        int left = this.getGuiLeft();
        int top = this.getGuiTop();
        gfx.blit(GUI_LOCATION, left, top, 0, 0.0F, 0.0F, this.imageWidth, this.imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        CafeTheme theme = this.menu.getCafeTheme();
        gfx.drawString(this.font, Component.translatable("gui.cozy_cafe.theme"), left + 20, top + 22, 0xFFFFFF, true);
        if (theme != null) {
            gfx.renderFakeItem(theme.modifier().modifierIcon().getDefaultInstance(), left + 22, top + 36);
            gfx.drawString(this.font, theme.themeName(), left + 42, top + 40, 0xFFFFFF, true);
            renderModifierActions(gfx, left + 28, top + 58, theme.modifier().modifierActions());
            gfx.blit(GUI_LOCATION, left + 140, top + 36, 176, 32, 16, 16);
            if (pMouseX >= left + 20 && pMouseX < left + MODIFIER_WIDTH + 40 && pMouseY >= top + 34 && pMouseY < top + 54) {
                final List<Component> tooltipList = getModifierFlavorsTooltips(theme.modifier(), false);
                gfx.renderTooltip(this.font, tooltipList, Items.AIR.getDefaultInstance().getTooltipImage(), pMouseX, pMouseY);
            }
        } else {

            gfx.drawString(this.font, Component.translatable("gui.cozy_cafe.no_theme"), left + 22, top + 40, 0xFFFFFF, true);
            gfx.drawWordWrap(this.font, FormattedText.of(Component.translatable("gui.cozy_cafe.adding_theme").getString()), left + 33, top + 65, 120, 0x000000);
            gfx.drawWordWrap(this.font, FormattedText.of(Component.translatable("gui.cozy_cafe.adding_theme").getString()), left + 32, top + 64, 120, 0xFFFFFF);
        }
        gfx.drawString(this.font, Component.translatable("gui.cozy_cafe.active_modifiers"), left + 20, top + 109, 0xFFFFFF, true);
        if (this.cafeModifiers != null && !this.cafeModifiers.isEmpty()) {
            int k = top + 105;
            int l = left + 14 + NUMBER_OF_MODIFIERS;
            this.renderScroller(gfx);
            int i1 = 0;
            List<String> renderedIds = new ArrayList<>();
            for (CafeModifier modifier : cafeModifiers) {
                if (renderedIds.contains(modifier.modifierId())) continue;
                int j1 = k + 17;
                if (!this.canScroll(this.cafeModifiers.size()) || i1 >= this.scrollOff && i1 < NUMBER_OF_MODIFIERS + this.scrollOff) {
                    gfx.renderFakeItem(modifier.modifierIcon().getDefaultInstance(), l, j1);
                    gfx.drawString(this.font, modifier.modifierName(), l + (modifier.modifierIcon() == Items.AIR ? 0 : 20), j1 + 4, 0xFFFFFF, true);
                    gfx.drawString(this.font, modifierCounts.get(modifier.modifierId()) + "/" + modifier.maxModifierCount(), l + 99, j1 + 4, 0xFFFFFF, true);

                    if (pMouseX >= l && pMouseX < l + MODIFIER_WIDTH && pMouseY >= j1 && pMouseY < j1 + MODIFIER_HEIGHT) {
                        final List<Component> tooltipList = getModifierTooltips(modifier, modifierCounts.get(modifier.modifierId()));
                        gfx.renderTooltip(this.font, tooltipList, Items.AIR.getDefaultInstance().getTooltipImage(), pMouseX, pMouseY);
                    }

                    gfx.blit(GUI_LOCATION, l + 120, j1, 176, 32, 16, 16);
                    if (pMouseX >= l +MODIFIER_WIDTH && pMouseX < l + MODIFIER_WIDTH + 16 && pMouseY >= j1 && pMouseY < j1 + MODIFIER_HEIGHT) {
                        final List<Component> tooltipList = getModifierFlavorsTooltips(modifier, false);
                        gfx.renderTooltip(this.font, tooltipList, Items.AIR.getDefaultInstance().getTooltipImage(), pMouseX, pMouseY);
                    }
                    k += MODIFIER_HEIGHT;
                    ++i1;
                } else {
                    ++i1;
                }
                renderedIds.add(modifier.modifierId());
            }
        }

        RenderSystem.enableDepthTest();


    }
    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            ItemStack itemStack = this.hoveredSlot.getItem();
            CafeMenuItem cafeMenuItem = CafeMenuItemRegistry.INSTANCE.getForItem(itemStack.getItem());
            if (cafeMenuItem != null) {
                final List<Component> tooltipList = getMenuItemTooltip(itemStack, cafeMenuItem);
                guiGraphics.renderTooltip(this.font, tooltipList, itemStack.getTooltipImage(), mouseX, mouseY);
            } else {
                super.renderTooltip(guiGraphics, mouseX, mouseY);
            }
        } else {
            super.renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }


    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        int i = this.cafeModifiers.size();
        if (this.canScroll(i)) {
            int j = i - NUMBER_OF_MODIFIERS;
            this.scrollOff = Mth.clamp((int) ((double) this.scrollOff - pDelta), 0, j);
        }

        return true;
    }

    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        int i = this.cafeModifiers.size();
        if (this.isDragging) {
            int j = this.topPos + SCROLL_BAR_TOP_POS_Y;
            int k = j + SCROLL_BAR_HEIGHT;
            int l = i - NUMBER_OF_MODIFIERS;
            float f = ((float) pMouseY - (float) j - 13.5F) / ((float) (k - j) - 27.0F);
            f = f * (float) l + 0.5F;
            this.scrollOff = Mth.clamp((int) f, 0, l);
            return true;
        } else {
            return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
    }

    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        this.isDragging = false;
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        if (this.canScroll(this.cafeModifiers.size()) && pMouseX > (double) (i + SCROLL_BAR_START_X) && pMouseX < (double) (i + SCROLL_BAR_START_X + 6) && pMouseY > (double) (j + SCROLL_BAR_TOP_POS_Y) && pMouseY <= (double) (j + SCROLL_BAR_TOP_POS_Y + SCROLL_BAR_HEIGHT + 1)) {
            this.isDragging = true;
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    private void renderScroller(GuiGraphics pGuiGraphics) {
        int pPosX = this.getGuiLeft();
        int pPosY = this.getGuiTop();
        int i = this.cafeModifiers.size() + 1 - NUMBER_OF_MODIFIERS;
        if (i > 1) {
            int j = SCROLL_BAR_HEIGHT - (SCROLLER_HEIGHT + (i - 1) * SCROLL_BAR_HEIGHT / i);
            int k = j / i + SCROLL_BAR_HEIGHT / i;
            int l = SCROLL_BAR_HEIGHT - SCROLLER_HEIGHT;
            int i1 = Math.min(l, this.scrollOff * k);
            if (this.scrollOff == i - 1) {
                i1 = l;
            }
            pGuiGraphics.blit(GUI_LOCATION, pPosX + SCROLL_BAR_START_X, pPosY + SCROLL_BAR_TOP_POS_Y + i1, 0, 176.0F, 0.0F, 6, SCROLLER_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        } else {
            pGuiGraphics.blit(GUI_LOCATION, pPosX + SCROLL_BAR_START_X, pPosY + SCROLL_BAR_TOP_POS_Y, 0, 182.0F, 0.0F, 6, SCROLLER_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }

    }

    private boolean canScroll(int pNumOffers) {
        return pNumOffers > NUMBER_OF_MODIFIERS;
    }


}