package io.github.chakyl.cozycafe.gui;


import com.mojang.blaze3d.systems.RenderSystem;
import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.data.CafeMenuItem;
import io.github.chakyl.cozycafe.data.CafeMenuItemRegistry;
import io.github.chakyl.cozycafe.network.EvilPacketsIHateThem;
import io.github.chakyl.cozycafe.network.ServerBoundRemoveMenuItemPacket;
import io.github.chakyl.cozycafe.util.MenuItemSelectionState;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class MenuSelectorScreen extends AbstractContainerScreen<MenuSelectorMenu> {

    private static final ResourceLocation GUI_LOCATION = new ResourceLocation(CozyCafe.MODID, "textures/gui/menu_selector.png");
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int SELL_ITEM_1_X = 5;
    private static final int SELL_ITEM_2_X = 35;
    private static final int BUY_ITEM_X = 68;
    private static final int LABEL_Y = 6;
    private static final int NUMBER_OF_MENU_BUTTONS = 4;
    private static final int TRADE_BUTTON_X = 5;
    private static final int MENU_BUTTON_HEIGHT = 19;
    private static final int MENU_BUTTON_WIDTH = 87;
    private static final int SCROLLER_HEIGHT = 27;
    private static final int SCROLLER_WIDTH = 6;
    private static final int SCROLL_BAR_HEIGHT = MENU_BUTTON_HEIGHT * NUMBER_OF_MENU_BUTTONS;
    private static final int SCROLL_BAR_TOP_POS_Y = 12;
    private static final int SCROLL_BAR_START_X = 162;
    private static final int LIMIT_ICON_START_X = 320;
    private static final int LIMIT_ICON_SIZE = 9;
    private MenuItemSelectionState lastStatus = MenuItemSelectionState.UNSET;
    private int shopItem;
    int scrollOff;
    private boolean isDragging;
    private boolean isCafeOpen = false;
    private List<ItemStack> cafeMenu = new ArrayList<>();
    private CafeMenuItemButton[] cafeMenuItemButtons = new CafeMenuItemButton[NUMBER_OF_MENU_BUTTONS];

    public MenuSelectorScreen(MenuSelectorMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 176;
        this.imageHeight = 176;
    }

    private void postButtonClick(int index) {
        this.menu.removeFromClientMenu(index);
        EvilPacketsIHateThem.sendToServer(new ServerBoundRemoveMenuItemPacket(index));
    }

    @Override
    protected void init() {
        super.init();
        int leftPos = this.getGuiLeft();
        int topPos = this.getGuiTop();
        int offset = topPos + 12;
        cafeMenu = this.menu.getCafeMenu();
        for (int l = 0; l < NUMBER_OF_MENU_BUTTONS; ++l) {
            final int slotIndex = l;
            this.cafeMenuItemButtons[l] = this.addRenderableWidget(new CafeMenuItemButton(leftPos + 74, offset, l, (button) -> {
                if (button instanceof CafeMenuItemButton) {
                    int itemIndex = this.menu.getCafeMenu().size() > 4 ? this.scrollOff + slotIndex : slotIndex;
                    if (itemIndex < this.menu.getCafeMenu().size()) {
                        this.postButtonClick(itemIndex);
                    }
                }

            }));
            offset += MENU_BUTTON_HEIGHT;
        }
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float pPartialTick, int pMouseX, int pMouseY) {
        int left = this.getGuiLeft();
        int top = this.getGuiTop();
        gfx.blit(GUI_LOCATION, left, top, 0, 0.0F, 0.0F, this.imageWidth, this.imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

//        PoseStack poseStack = gfx.pose();
//        poseStack.pushPose();
//        poseStack.translate(left + 42, top + 16, 0);
//        poseStack.scale(2f, 2f, 1.0f);
//        gfx.drawString(this.font, Component.translatable("Cafe Sun"), 0, 0, 0x181425, false);
//        poseStack.popPose();
//
//        gfx.drawString(this.font, Component.translatable("gui.cozycafe.cafe_manager.edit_menu"), left + 64, top + 71, 0x181425, false);
//        gfx.drawString(this.font, Component.translatable("gui.cozycafe.cafe_manager.open"), left + 75, top + 185, 0xFFFFFF, false);

        int feedbackIconX = left + 4;
        int feedbackIconY = top + 58;

        MenuItemSelectionState currentStatus = MenuItemSelectionState.fromCode(this.menu.getMenuItemAdditionStatus());
        if (this.cafeMenu != null && !this.cafeMenu.isEmpty()) {
            int k = top + 5;
            int l = left + 74 + NUMBER_OF_MENU_BUTTONS;
            this.renderScroller(gfx);
            int i1 = 0;
            for (ItemStack menuItem : this.cafeMenu) {
                int j1 = k + 8;
                if (!this.canScroll(this.cafeMenu.size()) || i1 >= this.scrollOff && i1 < NUMBER_OF_MENU_BUTTONS + this.scrollOff) {
                    gfx.renderFakeItem(menuItem, l, j1);
                    CafeMenuItem cafeMenuItem = CafeMenuItemRegistry.INSTANCE.getForItem(menuItem.getItem());
                    gfx.drawString(this.font, Component.translatable("category.cozycafe." + cafeMenuItem.category().toString().toLowerCase()), l + 24, j1 + 4, 16777215, true);
                    k += MENU_BUTTON_HEIGHT;
                    ++i1;
                } else {
                    ++i1;
                }

            }
        }

        for (CafeMenuItemButton MenuSelectorScreen$cafeMenuItemButton : this.cafeMenuItemButtons) {
            if (MenuSelectorScreen$cafeMenuItemButton.isHoveredOrFocused()) {
                MenuSelectorScreen$cafeMenuItemButton.renderToolTip(gfx, pMouseX, pMouseY);
            }

            MenuSelectorScreen$cafeMenuItemButton.visible = MenuSelectorScreen$cafeMenuItemButton.index < this.cafeMenu.size();
        }
        RenderSystem.enableDepthTest();
        // Menu addition status
        if (currentStatus == MenuItemSelectionState.VALID) {
            gfx.blit(GUI_LOCATION, feedbackIconX, feedbackIconY, 8, 176, 8, 8);
            gfx.drawString(this.font, Component.translatable("gui.cozycafe.menu_selector.added"), feedbackIconX + 8 + 2, feedbackIconY + 1, 0x181425, false);
        } else if (currentStatus == MenuItemSelectionState.INVALID) {
            gfx.blit(GUI_LOCATION, feedbackIconX, feedbackIconY, 0, 176, 8, 8);
            gfx.drawString(this.font, Component.translatable("gui.cozycafe.menu_selector.invalid"), feedbackIconX + 8 + 2, feedbackIconY + 1, 0x181425, false);
        }


    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        // Update UI from menu item addition attempt
        MenuItemSelectionState currentStatus = MenuItemSelectionState.fromCode(this.menu.getMenuItemAdditionStatus());
        if (currentStatus != lastStatus) {
            if (currentStatus == MenuItemSelectionState.VALID) {
                Minecraft.getInstance().player.playSound(SoundEvents.NOTE_BLOCK_CHIME.get(), 1.0F, 1.0F);
                this.cafeMenu = this.menu.getCafeMenu();
            } else if (currentStatus == MenuItemSelectionState.INVALID) {
                Minecraft.getInstance().player.playSound(SoundEvents.NOTE_BLOCK_BASS.get(), 1.0F, 1.0F);
            }
            this.lastStatus = currentStatus;
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            ItemStack itemStack = this.hoveredSlot.getItem();
            CafeMenuItem cafeMenuItem = CafeMenuItemRegistry.INSTANCE.getForItem(itemStack.getItem());
            if (cafeMenuItem != null) {
                List<Component> tooltipList = new ArrayList<>(getTooltipFromItem(Minecraft.getInstance(), itemStack));
                tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.price", cafeMenuItem.price()));
                tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.item_category", Component.translatable("category.cozycafe." + cafeMenuItem.category().toString().toLowerCase()).getString()).withStyle(ChatFormatting.GRAY));
                if (cafeMenuItem.bowlFood()) {
                    tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.bowl_food").withStyle(ChatFormatting.GRAY));
                }
                if (cafeMenuItem.bottleDrink()) {
                    tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.bottle_drink").withStyle(ChatFormatting.RED));
                }
                guiGraphics.renderTooltip(this.font, tooltipList, itemStack.getTooltipImage(), mouseX, mouseY);
            } else {
                super.renderTooltip(guiGraphics, mouseX, mouseY);
            }
        } else {
            super.renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        int i = this.cafeMenu.size();
        if (this.canScroll(i)) {
            int j = i - NUMBER_OF_MENU_BUTTONS;
            this.scrollOff = Mth.clamp((int) ((double) this.scrollOff - pDelta), 0, j);
        }

        return true;
    }

    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        int i = this.cafeMenu.size();
        if (this.isDragging) {
            int j = this.topPos + SCROLL_BAR_TOP_POS_Y;
            int k = j + SCROLL_BAR_HEIGHT;
            int l = i - NUMBER_OF_MENU_BUTTONS;
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
        if (this.canScroll(this.cafeMenu.size()) && pMouseX > (double) (i + SCROLL_BAR_START_X) && pMouseX < (double) (i + SCROLL_BAR_START_X + 6) && pMouseY > (double) (j + SCROLL_BAR_TOP_POS_Y) && pMouseY <= (double) (j + SCROLL_BAR_TOP_POS_Y + SCROLL_BAR_HEIGHT + 1)) {
            this.isDragging = true;
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    private void renderScroller(GuiGraphics pGuiGraphics) {
        int pPosX = this.getGuiLeft();
        int pPosY = this.getGuiTop();
        int i = this.cafeMenu.size() + 1 - NUMBER_OF_MENU_BUTTONS;
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
        return pNumOffers > NUMBER_OF_MENU_BUTTONS;
    }


    @OnlyIn(Dist.CLIENT)
    class CafeMenuItemButton extends Button {
        final int index;

        public CafeMenuItemButton(int pX, int pY, int pIndex, OnPress pOnPress) {
            super(pX, pY, MENU_BUTTON_WIDTH, MENU_BUTTON_HEIGHT, CommonComponents.EMPTY, pOnPress, DEFAULT_NARRATION);
            this.index = pIndex;
            this.visible = false;
        }


        public int getIndex() {
            return this.index;
        }

        public void renderToolTip(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
            if (this.isHovered && MenuSelectorScreen.this.cafeMenu.size() > this.index + MenuSelectorScreen.this.scrollOff) {
                List<Component> tooltipList = new ArrayList<>(3);
                CafeMenuItem cafeMenuItem = CafeMenuItemRegistry.INSTANCE.getForItem(MenuSelectorScreen.this.cafeMenu.get(this.index + MenuSelectorScreen.this.scrollOff).getItem());
                tooltipList.add(cafeMenuItem.item().getDefaultInstance().getHoverName());
                tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.price", cafeMenuItem.price()));
                tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.item_category", Component.translatable("category.cozycafe." + cafeMenuItem.category().toString().toLowerCase()).getString()).withStyle(ChatFormatting.GRAY));
                if (cafeMenuItem.bowlFood()) {
                    tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.bowl_food").withStyle(ChatFormatting.GRAY));
                }
                if (cafeMenuItem.bottleDrink()) {
                    tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.bottle_drink").withStyle(ChatFormatting.RED));
                }
                tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.remove").withStyle(ChatFormatting.RED));

                // TODO: 1.1 - Flavors and themes
                pGuiGraphics.renderTooltip(MenuSelectorScreen.this.font, tooltipList, Items.ACACIA_FENCE.getDefaultInstance().getTooltipImage(), pMouseX, pMouseY);

            }

        }
    }

}