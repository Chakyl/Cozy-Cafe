package io.github.chakyl.cozycafe.gui;


import com.mojang.blaze3d.systems.RenderSystem;
import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.data.CafeMenuItem;
import io.github.chakyl.cozycafe.data.CafeMenuItemRegistry;
import io.github.chakyl.cozycafe.network.EvilPacketsIHateThem;
import io.github.chakyl.cozycafe.network.ServerBoundClearCafePacket;
import io.github.chakyl.cozycafe.network.ServerBoundRemoveMenuItemPacket;
import io.github.chakyl.cozycafe.util.MenuItemSelectionState;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
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

import static io.github.chakyl.cozycafe.util.GeneralUtils.formatPrice;

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
    private static final int TRADE_BUTTON_WIDTH = 138;
    private static final int MENU_BUTTON_HEIGHT = 15;
    private static final int MENU_BUTTON_WIDTH = 87;
    private static final int SCROLLER_HEIGHT = 27;
    private static final int SCROLLER_WIDTH = 6;
    private static final int SCROLL_BAR_HEIGHT = MENU_BUTTON_HEIGHT * NUMBER_OF_MENU_BUTTONS;
    private static final int SCROLL_BAR_TOP_POS_Y = 22;
    private static final int SCROLL_BAR_START_X = 155;
    private static final int LIMIT_ICON_START_X = 320;
    private static final int LIMIT_ICON_SIZE = 9;
    int scrollOff;
    private MenuItemSelectionState lastStatus = MenuItemSelectionState.UNSET;
    private boolean isDragging;
    private List<ItemStack> cafeMenu = new ArrayList<>();
    private final CafeMenuItemButton[] cafeMenuItemButtons = new CafeMenuItemButton[NUMBER_OF_MENU_BUTTONS];

    public MenuSelectorScreen(MenuSelectorMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 176;
        this.imageHeight = 225;
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
        int offset = topPos + 22;

        cafeMenu = this.menu.getCafeMenu();
        for (int l = 0; l < NUMBER_OF_MENU_BUTTONS; ++l) {
            final int slotIndex = l;
            this.cafeMenuItemButtons[l] = this.addRenderableWidget(new CafeMenuItemButton(leftPos + 140, offset, l, 15, 15, 176, 32, 16, GUI_LOCATION, 256, 256, (button) -> {
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

        gfx.drawString(this.font, Component.translatable("gui.cozycafe.menu_selector.name"), left + 16, top + 12, 0xFFFFFF, true);
        gfx.drawString(this.font, Component.literal(this.menu.blockEntity.getMenu().size() + "/25"), left + 132, top + 12, 0xFFFFFF, true);
        gfx.drawString(this.font, Component.literal(String.valueOf(this.menu.blockEntity.getStarsFromReputation())), left + 21, top + 95, 0xFFFFFF, true);
        gfx.drawString(this.font, Component.translatable("gui.cozycafe.menu_selector.stars_required", Math.max(3, this.menu.blockEntity.getStarsFromReputation() * CozyCafe.CONFIG.menuSizePerStar.get())), left + 44, top + 95, 0xFFFFFF, true);

        MenuItemSelectionState currentStatus = MenuItemSelectionState.fromCode(this.menu.getMenuItemAdditionStatus());
        if (this.cafeMenu != null && !this.cafeMenu.isEmpty()) {
            int k = top + 14;
            int l = left + 14 + NUMBER_OF_MENU_BUTTONS;
            this.renderScroller(gfx);
            int i1 = 0;
            for (ItemStack menuItem : this.cafeMenu) {
                int j1 = k + 8;
                if (!this.canScroll(this.cafeMenu.size()) || i1 >= this.scrollOff && i1 < NUMBER_OF_MENU_BUTTONS + this.scrollOff) {
                    gfx.renderFakeItem(menuItem, l, j1);
                    CafeMenuItem cafeMenuItem = CafeMenuItemRegistry.INSTANCE.getForItem(menuItem.getItem());
                    gfx.drawString(this.font, Component.translatable("category.cozycafe." + cafeMenuItem.category().toString().toLowerCase()), l + 20, j1 + 4, 16777215, true);
                    Component priceStr = Component.translatable("gui.cozycafe.menu_selector.inline_price", formatPrice(cafeMenuItem.price()));
                    int priceOffset = 16;
                    gfx.drawString(this.font, priceStr, l + TRADE_BUTTON_WIDTH - font.width(priceStr) - priceOffset, j1 + 4, 16777215, true);


                    if (pMouseX >= l && pMouseX < l + TRADE_BUTTON_WIDTH - priceOffset && pMouseY >= j1 && pMouseY < j1 + MENU_BUTTON_HEIGHT) {
                        List<Component> tooltipList = new ArrayList<>(getTooltipFromItem(Minecraft.getInstance(), cafeMenuItem.item().getDefaultInstance()));
                        tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.price", formatPrice(cafeMenuItem.price())));
                        tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.item_category", Component.translatable("category.cozycafe." + cafeMenuItem.category().toString().toLowerCase()).getString()).withStyle(ChatFormatting.GRAY));
                        if (cafeMenuItem.bowlFood()) {
                            tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.bowl_food").withStyle(ChatFormatting.GRAY));
                        }
                        if (cafeMenuItem.bottleDrink()) {
                            tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.bottle_drink").withStyle(ChatFormatting.RED));
                        }
                        gfx.renderTooltip(this.font, tooltipList, cafeMenuItem.item().getDefaultInstance().getTooltipImage(), pMouseX, pMouseY);
                    }


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
        int feedbackIconX = left + 44;
        int feedbackIconY = top + 117;
        if (currentStatus == MenuItemSelectionState.VALID) {
            gfx.blit(GUI_LOCATION, feedbackIconX - 4, feedbackIconY - 4, 176, 48, 16, 16);
            gfx.drawString(this.font, Component.translatable("gui.cozycafe.menu_selector.added"), feedbackIconX + 12, feedbackIconY, 0xFFFFFF, true);
        } else if (currentStatus == MenuItemSelectionState.INVALID) {
            gfx.blit(GUI_LOCATION, feedbackIconX - 4, feedbackIconY - 4, 176, 32, 16, 16);
            gfx.drawString(this.font, Component.translatable("gui.cozycafe.menu_selector.invalid"), feedbackIconX + 12, feedbackIconY, 0xFFFFFF, true);
        } else {
            gfx.drawString(this.font, Component.translatable("gui.cozycafe.menu_selector.place_items"), feedbackIconX, feedbackIconY, 0xFFFFFF, true);
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
    class CafeMenuItemButton extends ImageButton {
        final int index;

        public CafeMenuItemButton(int pX, int pY, int pIndex, int pWidth, int pHeight, int pXTexStart, int pYTexStart, int pYDiffTex, ResourceLocation pResourceLocation, int pTextureWidth, int pTextureHeight, Button.OnPress pOnPress) {
            super(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, pYDiffTex, pResourceLocation, pTextureWidth, pTextureHeight, pOnPress);
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
                tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.remove").withStyle(ChatFormatting.RED));

                // TODO: 1.1 - Flavors and themes
                pGuiGraphics.renderTooltip(MenuSelectorScreen.this.font, tooltipList, Items.ACACIA_FENCE.getDefaultInstance().getTooltipImage(), pMouseX, pMouseY);

            }

        }
    }

}