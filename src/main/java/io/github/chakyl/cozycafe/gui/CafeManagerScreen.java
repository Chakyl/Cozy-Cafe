package io.github.chakyl.cozycafe.gui;


import com.mojang.blaze3d.vertex.PoseStack;
import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.data.CafeMenuItem;
import io.github.chakyl.cozycafe.data.CafeMenuItemRegistry;
import io.github.chakyl.cozycafe.network.EvilPacketsIHateThem;
import io.github.chakyl.cozycafe.network.ServerBoundOpenMenuSelectorMenuPacket;
import io.github.chakyl.cozycafe.network.ServerBoundToggleCafeOpenPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
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
    private boolean isCafeOpen = false;
    private List<ItemStack> cafeMenu = new ArrayList<>();
    private Component errorMessage = null;
    private int errorDisplayTicks = 0;

    public CafeManagerScreen(CafeManagerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 176;
        this.imageHeight = 225;
    }

    public void setErrorMessage(Byte errorCode) {
        this.errorMessage = switch (errorCode) {
            case 0 -> Component.translatable("block.cozycafe.cafe_manager.menu_too_small");
            case 1 -> Component.translatable("block.cozycafe.cafe_manager.menu_too_small_for_stars");
            case 2 -> Component.translatable("block.cozycafe.cafe_manager.already_opened");
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
        this.isCafeOpen = this.menu.getIsCafeOpen();
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
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.cozycafe.cafe_manager." + (this.isCafeOpen ? "close" : "open")),
                        (button) -> {
                            EvilPacketsIHateThem.sendToServer(new ServerBoundToggleCafeOpenPacket(this.menu.blockEntity.getBlockPos()));
                        })
                .bounds(leftPos + 62, topPos + 198, 52, 20)
                .build()
        );
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

        PoseStack poseStack = gfx.pose();
        poseStack.pushPose();
        poseStack.translate(left + 42, top + 16, 0);
        poseStack.scale(2f, 2f, 1.0f);
        gfx.drawString(this.font, Component.translatable("Cafe Sun"), 0, 0, 0x181425, false);
        poseStack.popPose();
        // Stars
        for (int i = 0; i < this.menu.getStars(); i++) {

            gfx.blit(GUI_LOCATION, this.leftPos +48 + (i * 16), this.topPos +40, 176, 32, 16, 16);
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
                    tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.price", cafeMenuItem.price()).withStyle(ChatFormatting.GREEN));
                    tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.item_category", Component.translatable("category.cozycafe." + cafeMenuItem.category().toString().toLowerCase()).getString()).withStyle(ChatFormatting.GRAY));
                    gfx.renderTooltip(this.font, tooltipList, hoveredItem.getTooltipImage(), pMouseX, pMouseY);
                } else {
                    super.renderTooltip(gfx, pMouseX, pMouseY);
                }
            }
        }
        if (this.errorMessage != null) {
            gfx.drawWordWrap(this.font, this.errorMessage, left + 14, top + 155, 160, 0xFF5555);
        }

//        gfx.drawString(this.font, Component.translatable("gui.cozycafe.cafe_manager.edit_menu"), left + 64, top + 71, 0x181425, false);
//        gfx.drawString(this.font, Component.translatable("gui.cozycafe.cafe_manager.open"), left + 75, top + 185, 0xFFFFFF, false);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }
}