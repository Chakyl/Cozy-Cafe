package io.github.chakyl.cozycafe.gui;


import com.mojang.blaze3d.vertex.PoseStack;
import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.data.CafeMenuItem;
import io.github.chakyl.cozycafe.data.CafeMenuItemRegistry;
import io.github.chakyl.cozycafe.network.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;


@OnlyIn(Dist.CLIENT)
public class CafeManagerScreen extends AbstractContainerScreen<CafeManagerMenu> {

    private static final ResourceLocation GUI_LOCATION = new ResourceLocation(CozyCafe.MODID, "textures/gui/cafe_manager.png");
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    private int shopItem;
    int scrollOff;
    private boolean isDragging;
    private List<ItemStack> cafeMenu = new ArrayList<>();
    private Component errorMessage = null;
    private int errorDisplayTicks = 0;
    private Button toggleOpenButton;
    private EditBox nameField;
    private boolean isNameEditing = false;

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

        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.cozycafe.cafe_manager.edit_menu"),
                        (button) -> {
                            if (this.minecraft != null) {
                                EvilPacketsIHateThem.sendToServer(new ServerBoundOpenMenuSelectorMenuPacket(this.menu.blockEntity.getBlockPos()));
                            }
                        })
                .bounds(leftPos + 55, topPos + 60, 64, 20)
                .build()
        );

        this.toggleOpenButton = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.cozycafe.cafe_manager." + (this.menu.getIsCafeOpen() ? "close" : "open")),
                        (button) -> {
                            EvilPacketsIHateThem.sendToServer(new ServerBoundToggleCafeOpenPacket(this.menu.blockEntity.getBlockPos()));
                        })
                .bounds(leftPos + 62, topPos + 198, 52, 20)
                .build()
        );

        this.nameField = new EditBox(this.font, leftPos + 40, topPos + 16, 96, 20, Component.empty());
        this.nameField.setMaxLength(32);
        this.nameField.setEditable(false);
        this.nameField.setVisible(false);
        this.addRenderableWidget(this.nameField);

        ImageButton toggleEditButton = new ImageButton(leftPos + 121, topPos + 66, 15, 15, 176, 80, 16, GUI_LOCATION, 256, 256, (button) -> {
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

        ImageButton showAreaButton = new ImageButton(leftPos + this.imageWidth - 20, topPos + this.imageHeight - 20, 15, 15, 192, 80, 16, GUI_LOCATION, 256, 256, (button) -> {
            EvilPacketsIHateThem.sendToServer(new ServerBoundShowCafeAreaPacket(this.menu.blockEntity.getBlockPos()));
            this.onClose();
        });
        showAreaButton.setTooltip(Tooltip.create(Component.translatable("gui.cozycafe.cafe_manager.show_area")));
        this.addRenderableWidget(showAreaButton);

        ImageButton clearCafeButton = new ImageButton(leftPos + 5, topPos + this.imageHeight - 20, 15, 15, 208, 80, 16, GUI_LOCATION, 256, 256, (button) -> {
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
            int textWidth = this.font.width(titleText);
            float scale = Math.min(3.0f, (this.imageWidth - 32.0f) / (float) textWidth);

            PoseStack poseStack = gfx.pose();
            poseStack.pushPose();
            poseStack.translate(left + (this.imageWidth / 2.0f), top + 16, 0);
            poseStack.scale(scale, scale, 1.0f);
            gfx.drawString(this.font, titleText, -(textWidth / 2), 0, 0x181425, false);
            poseStack.popPose();
        }
        // Stars
        for (int i = 0; i < this.menu.getStars(); i++) {
            gfx.blit(GUI_LOCATION, this.leftPos + 48 + (i * 16), this.topPos + 40, 176, 32, 16, 16);
        }
        // Menu Items
        if (this.cafeMenu != null && !this.cafeMenu.isEmpty()) {
            int slotWidth = 22;
            int startX = this.leftPos + 12;
            int startY = this.topPos + 91;

            int maxColumns = 7;
            int hoveredIndex = -1;
            ItemStack hoveredItem = ItemStack.EMPTY;
            int index = 0;
            for (ItemStack menuItem : this.cafeMenu) {
                int col = index % maxColumns;
                int row = index / maxColumns;
                int itemX = startX + (col * slotWidth);
                int itemY = startY + (row * slotWidth) + (row * 2);

                CafeMenuItem cafeMenuItem = CafeMenuItemRegistry.INSTANCE.getForItem(menuItem.getItem());
                int backgroundOffset = switch (cafeMenuItem.category()) {
                    case MAIN -> 0;
                    case DRINK -> 20;
                    case DESSERT -> 40;
                };
                gfx.blit(GUI_LOCATION, itemX, itemY, 176 + backgroundOffset, 0, 20, 23);
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
                CafeMenuItem cafeMenuItem = CafeMenuItemRegistry.INSTANCE.getForItem(hoveredItem.getItem());
                if (cafeMenuItem != null) {
                    List<Component> tooltipList = new ArrayList<>(getTooltipFromItem(Minecraft.getInstance(), hoveredItem));
                    tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.price", cafeMenuItem.price()));
                    tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.item_category", Component.translatable("category.cozycafe." + cafeMenuItem.category().toString().toLowerCase()).getString()).withStyle(ChatFormatting.GRAY));
                    if (cafeMenuItem.bowlFood()) {
                        tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.bowl_food").withStyle(ChatFormatting.GRAY));
                    }
                    if (cafeMenuItem.bottleDrink()) {
                        tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.bottle_drink").withStyle(ChatFormatting.RED));
                    }
                    gfx.renderTooltip(this.font, tooltipList, hoveredItem.getTooltipImage(), pMouseX, pMouseY);
                } else {
                    super.renderTooltip(gfx, pMouseX, pMouseY);
                }
            }
        }
        if (this.errorMessage != null) {
            gfx.drawWordWrap(this.font, this.errorMessage, left + 14, top + 155, 160, 0xFF5555);
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        if (this.toggleOpenButton != null) {
            boolean open = this.menu.getIsCafeOpen();
            this.toggleOpenButton.setMessage(Component.translatable("gui.cozycafe.cafe_manager." + (open ? "close" : "open")));
        }
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }
}