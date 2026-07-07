package io.github.chakyl.cozycafe.gui;


import com.mojang.blaze3d.vertex.PoseStack;
import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.data.CafeMenuItem;
import io.github.chakyl.cozycafe.data.CafeMenuItemRegistry;
import io.github.chakyl.cozycafe.network.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

import static io.github.chakyl.cozycafe.util.GeneralUtils.formatPrice;


@OnlyIn(Dist.CLIENT)
public class CafeManagerScreen extends AbstractContainerScreen<CafeManagerMenu> {

    private static final ResourceLocation GUI_LOCATION = new ResourceLocation(CozyCafe.MODID, "textures/gui/cafe_manager.png");
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    int scrollOff;
    private int shopItem;
    private boolean isDragging;
    private List<ItemStack> cafeMenu = new ArrayList<>();
    private Component errorMessage = null;
    private int errorDisplayTicks = 0;
    private ImageButton toggleOpenButton;
    private EditBox nameField;
    private boolean isNameEditing = false;
    private ItemStack hoveredItemToRender = ItemStack.EMPTY;

    public CafeManagerScreen(CafeManagerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 176;
        this.imageHeight = 225;
    }

    public void setErrorMessage(Byte errorCode) {
        this.errorMessage = switch (errorCode) {
            case 0 -> Component.translatable("block.cozycafe.cafe_manager.no_sign");
            case 1 -> Component.translatable("block.cozycafe.cafe_manager.menu_too_small");
            case 2 -> Component.translatable("block.cozycafe.cafe_manager.menu_too_small_for_stars");
            case 3 -> Component.translatable("block.cozycafe.cafe_manager.already_opened");
            case 4 -> Component.translatable("block.cozycafe.cafe_manager.nearby_manager");
            default -> throw new IllegalStateException("Unexpected value: " + errorCode);
        };
        this.errorDisplayTicks = 240;
    }

    @Override
    protected void init() {
        super.init();
        int leftPos = this.getGuiLeft();
        int topPos = this.getGuiTop();
        cafeMenu = this.menu.getCafeMenu();
        ImageButton editMenuButton = new ImageButton(leftPos + 20, topPos + 36, 17, 15, 224, 80, 16, GUI_LOCATION, 256, 256, (button) -> {
            if (!CafeManagerScreen.this.menu.getIsCafeOpen() && this.minecraft != null) {
                EvilPacketsIHateThem.sendToServer(new ServerBoundOpenMenuSelectorMenuPacket(this.menu.blockEntity.getBlockPos()));
            }
        }){
            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                this.setTooltip(Tooltip.create(Component.translatable(CafeManagerScreen.this.menu.getIsCafeOpen() ? "tooltip.cozycafe.cafe_manager.cannot_edit":"gui.cozycafe.cafe_manager.edit_menu")));
            }
        };
        this.addRenderableWidget(editMenuButton);
        // hgate this
        this.toggleOpenButton = new ImageButton(leftPos + 56, topPos + 192, 64, 24, 176, 0, 32, GUI_LOCATION, 256, 256, (button) -> {
            EvilPacketsIHateThem.sendToServer(new ServerBoundToggleCafeOpenPacket(this.menu.blockEntity.getBlockPos()));
        }) {
            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                boolean open = CafeManagerScreen.this.menu.getIsCafeOpen();
                int yOffset = open ? 192 : 0;
                if (this.isHoveredOrFocused()) yOffset += 32;
                this.setTooltip(Tooltip.create(Component.translatable("gui.cozycafe.cafe_manager." + (open ? "close" : "open"))));
                guiGraphics.blit(GUI_LOCATION, this.getX(), this.getY(), 176, yOffset, this.width, this.height, 256, 256);
            }
        };
        this.addRenderableWidget(toggleOpenButton);

        this.nameField = new EditBox(this.font, leftPos + 40, topPos + 18, 96, 16, Component.empty());
        this.nameField.setMaxLength(32);
        this.nameField.setEditable(false);
        this.nameField.setVisible(false);
        this.addRenderableWidget(this.nameField);

        ImageButton toggleEditButton = new ImageButton(leftPos + 146, topPos + 36, 15, 15, 176, 80, 16, GUI_LOCATION, 256, 256, (button) -> {
            this.isNameEditing = !this.isNameEditing;
            this.nameField.setEditable(this.isNameEditing);
            this.nameField.setVisible(this.isNameEditing);

            if (!this.isNameEditing) {
                EvilPacketsIHateThem.sendToServer(new ServerBoundRenameCafePacket(this.menu.blockEntity.getBlockPos(), this.nameField.getValue()));
            } else {
                this.nameField.setValue(this.menu.getCafeName());
                this.nameField.setFocused(true);
            }
        });
        toggleEditButton.setTooltip(Tooltip.create(Component.translatable("gui.cozycafe.cafe_manager.edit_name")));
        this.addRenderableWidget(toggleEditButton);

        ImageButton showAreaButton = new ImageButton(leftPos + this.imageWidth - 25, topPos + this.imageHeight - 29, 15, 15, 192, 80, 16, GUI_LOCATION, 256, 256, (button) -> {
            EvilPacketsIHateThem.sendToServer(new ServerBoundShowCafeAreaPacket(this.menu.blockEntity.getBlockPos()));
            this.onClose();
        });
        showAreaButton.setTooltip(Tooltip.create(Component.translatable("gui.cozycafe.cafe_manager.show_area")));
        this.addRenderableWidget(showAreaButton);

        ImageButton clearCafeButton = new ImageButton(leftPos + 13, topPos + this.imageHeight - 29, 15, 15, 208, 80, 16, GUI_LOCATION, 256, 256, (button) -> {
            if (Screen.hasShiftDown()) {
                EvilPacketsIHateThem.sendToServer(new ServerBoundClearCafePacket(this.menu.blockEntity.getBlockPos()));
                this.onClose();
            }
        });
        clearCafeButton.setTooltip(Tooltip.create(Component.translatable("gui.cozycafe.cafe_manager.clear_data")));
        this.addRenderableWidget(clearCafeButton);
    }


    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (this.nameField.isFocused()) {
            if (pKeyCode == 256 || pKeyCode == 257) {
                this.isNameEditing = false;
                this.nameField.setEditable(false);
                this.nameField.setVisible(false);
                this.nameField.setFocused(false);
                this.menu.setName(this.nameField.getValue());
                return true;
            }

            this.nameField.keyPressed(pKeyCode, pScanCode, pModifiers);
            return true;
        }

        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.errorDisplayTicks > 0) {
            this.errorDisplayTicks--;
            if (this.errorDisplayTicks == 0) {
                this.errorMessage = null;
            }
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
        if (!this.isNameEditing) {
            Component titleText = Component.literal(this.menu.getCafeName());
            PoseStack poseStack = gfx.pose();
            poseStack.pushPose();
            poseStack.translate(left + (this.imageWidth / 2.0f), top + 22, 0);
            gfx.drawString(this.font, titleText, -(this.font.width(titleText) / 2), 0, 0x181425, false);
            poseStack.popPose();
        }
        // Stars
        for (int i = 0; i < this.menu.getStars(); i++) {
            gfx.blit(GUI_LOCATION, this.leftPos + 57 + (i * 12), this.topPos + 34, 176, 112, 16, 16);
        }
        // Menu Items
        if (this.cafeMenu != null && !this.cafeMenu.isEmpty()) {
            int slotWidth = 29;
            int slotHeight = 25;
            int startX = this.leftPos + 20;
            int startY = this.topPos + 55;

            int maxColumns = 5;
            int hoveredIndex = -1;
            ItemStack hoveredItem = ItemStack.EMPTY;
            int index = 0;
            for (ItemStack menuItem : this.cafeMenu) {
                int col = index % maxColumns;
                int row = index / maxColumns;
                int itemX = startX + (col * slotWidth);
                int itemY = startY + (row * slotHeight) + (row * 2);

                CafeMenuItem cafeMenuItem = CafeMenuItemRegistry.INSTANCE.getForItem(menuItem.getItem());
                int backgroundOffset = switch (cafeMenuItem.category()) {
                    case MAIN -> 0;
                    case DRINK -> 16;
                    case DESSERT -> 32;
                };
                gfx.blit(GUI_LOCATION, itemX + 3, itemY + 7, 176 + backgroundOffset, 128, 16, 16);
                gfx.renderFakeItem(menuItem, itemX + 2, itemY + 4);

                gfx.renderItemDecorations(this.font, menuItem, itemX, itemY);
                if (pMouseX >= itemX && pMouseX < itemX + 20 + 2 && pMouseY >= itemY && pMouseY < itemY + 23 + 2) {
                    hoveredIndex = index;
                    hoveredItem = menuItem;
                }
                index++;
                if (row > 3) break;
            }
            if (hoveredIndex != -1 && !hoveredItem.isEmpty()) {
                this.hoveredItemToRender = hoveredItem;
            }
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.hoveredItemToRender = ItemStack.EMPTY;
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
        if (this.errorMessage != null) {
            pGuiGraphics.renderComponentTooltip(this.font, wrapErrorText(this.font, this.errorMessage), this.leftPos + 11, this.topPos + 156);
        }
        if (!this.hoveredItemToRender.isEmpty()) {
            CafeMenuItem cafeMenuItem = CafeMenuItemRegistry.INSTANCE.getForItem(this.hoveredItemToRender.getItem());
            if (cafeMenuItem != null) {
                List<Component> tooltipList = new ArrayList<>(getTooltipFromItem(Minecraft.getInstance(), this.hoveredItemToRender));
                tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.price", formatPrice(cafeMenuItem.price())));
                tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.item_category", Component.translatable("category.cozycafe." + cafeMenuItem.category().toString().toLowerCase()).getString()).withStyle(ChatFormatting.GRAY));

                if (cafeMenuItem.bowlFood()) {
                    tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.bowl_food").withStyle(ChatFormatting.GRAY));
                }
                if (cafeMenuItem.bottleDrink()) {
                    tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.bottle_drink").withStyle(ChatFormatting.RED));
                }
                pGuiGraphics.renderTooltip(this.font, tooltipList, this.hoveredItemToRender.getTooltipImage(), pMouseX, pMouseY);
            }
        }
    }

    private List<Component> wrapErrorText(Font font, Component component) {
        List<Component> errorComps = new ArrayList<>();
        List<FormattedText> lines = font.getSplitter().splitLines(component, 148, Style.EMPTY);

        errorComps.add(Component.translatable("tooltip.cozycafe.cafe_manager.error").withStyle(ChatFormatting.RED));
        for (FormattedText line : lines) {
            if (line instanceof Component comp) {
                errorComps.add(comp);
            } else {
                errorComps.add(Component.literal(line.getString()));
            }
        }

        return errorComps;
    }
}