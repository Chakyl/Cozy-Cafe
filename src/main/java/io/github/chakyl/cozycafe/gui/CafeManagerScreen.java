package io.github.chakyl.cozycafe.gui;


import com.mojang.blaze3d.vertex.PoseStack;
import io.github.chakyl.cozycafe.CozyCafe;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collection;

@OnlyIn(Dist.CLIENT)
public class CafeManagerScreen extends AbstractContainerScreen<CafeManagerMenu> {
    /**
     * The GUI texture for the villager merchant GUI.
     */
    private static final ResourceLocation GUI_LOCATION = new ResourceLocation(CozyCafe.MODID, "textures/gui/cafe_manager.png");
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int LABEL_Y = 6;
    private static final int NUMBER_OF_SHOP_BUTTONS = 8;
    private static final int SHOP_BUTTON_HEIGHT = 20;
    private static final int SHOP_BUTTON_WIDTH = 153 - SHOP_BUTTON_HEIGHT;
    private static final int SCROLLER_HEIGHT = 27;
    private static final int SCROLLER_WIDTH = 6;
    private static final int SCROLL_BAR_HEIGHT = SHOP_BUTTON_HEIGHT * NUMBER_OF_SHOP_BUTTONS;
    private static final int SCROLL_BAR_TOP_POS_Y = 18;
    private static final int SCROLL_BAR_START_X = 162;
    private static final int INDICATOR_ICON_SIZE = 7;
    private static final int INDICATOR_ICONS_START_X = 112;
    private int shopItem;
    int scrollOff;
    private boolean isDragging;

    public CafeManagerScreen(CafeManagerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 176;
        this.imageHeight = 208;
    }


    protected void init() {
        super.init();
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int k = j + 18;

    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
    }

    protected void renderBg(GuiGraphics gfx, float pPartialTick, int pMouseX, int pMouseY) {
        int left = this.getGuiLeft();
        int top = this.getGuiTop();
        gfx.blit(GUI_LOCATION, left, top, 0, 0.0F, 0.0F, this.imageWidth, this.imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        PoseStack poseStack = gfx.pose();
        poseStack.pushPose();
        poseStack.translate(left + 42, top + 16, 0);
        poseStack.scale(2f, 2f, 1.0f);
        gfx.drawString(this.font, Component.translatable("Cafe Sun"), 0, 0, 0x181425, false);

        poseStack.popPose();
        gfx.drawString(this.font, Component.translatable("gui.cozycafe.cafe_manager.edit_menu"), left + 64, top + 71, 0x181425, false);
        gfx.drawString(this.font, Component.translatable("gui.cozycafe.cafe_manager.open"), left + 75, top + 185, 0xFFFFFF, false);
//        gfx.drawString(this.font, Component.translatable("gui.cozycafe.cafe_manager.close"), left + 60, top + 27, 0xFFFFFF, false);
    }

    /**
     * Renders the graphical user interface (GUI) element.
     *
     * @param pGuiGraphics the GuiGraphics object used for rendering.
     * @param pMouseX      the x-coordinate of the mouse cursor.
     * @param pMouseY      the y-coordinate of the mouse cursor.
     * @param pPartialTick the partial tick time.
     */
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int k = j + 4 + 1;
        int l = i + 4;
        int i1 = 8;

        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }
}