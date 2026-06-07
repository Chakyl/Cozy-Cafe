package io.github.chakyl.cozycafe.gui;

import io.github.chakyl.cozycafe.blockentities.CafeManagerBlockEntity;
import io.github.chakyl.cozycafe.network.ClientBoundAddMenuItemPacket;
import io.github.chakyl.cozycafe.network.EvilPacketsIHateThem;
import io.github.chakyl.cozycafe.registry.CozyRegistry;
import io.github.chakyl.cozycafe.util.MenuItemSelectionState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public class MenuSelectorMenu extends AbstractContainerMenu {
    public final CafeManagerBlockEntity blockEntity;
    private final Player player;
    private final Level level;
    private final DataSlot menuItemAdditionStatus = DataSlot.standalone();
    private List<ItemStack> clientCafeMenu;

    public MenuSelectorMenu(int pContainerId, Inventory pPlayerInventory, FriendlyByteBuf buf) {
        this(pContainerId, pPlayerInventory, pPlayerInventory.player.level().getBlockEntity(buf.readBlockPos()));
        int size = buf.readInt();
        this.clientCafeMenu = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            this.clientCafeMenu.add(buf.readItem());
        }
    }

    public MenuSelectorMenu(int pContainerId, Inventory pPlayerInventory, BlockEntity entity) {
        super(CozyRegistry.MenuRegistry.MENU_SELECTOR.get(), pContainerId);
        player = pPlayerInventory.player;
        this.level = pPlayerInventory.player.level();
        blockEntity = ((CafeManagerBlockEntity) entity);
        this.addDataSlot(menuItemAdditionStatus);
        menuItemAdditionStatus.set(MenuItemSelectionState.UNSET.getCode());
        this.addSlot(new Slot(new SimpleContainer(1), 0, 26, 71) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player playerIn) {
                return false;
            }
        });

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(pPlayerInventory, j + i * 9 + 9, 8 + j * 18, 94 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(pPlayerInventory, i, 8 + i * 18, 152));
        }
        this.broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId == 0) {
            ItemStack carriedStack = this.getCarried();
            if (!level.isClientSide() && !carriedStack.isEmpty()) {
                boolean success = blockEntity.addToMenu(carriedStack);
                toggleMenuItemAdditionStatus(success);
                if (success) {
                    EvilPacketsIHateThem.sendToPlayer(new ClientBoundAddMenuItemPacket(carriedStack), (ServerPlayer) player);
                }
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot.hasItem()) {
            ItemStack slotItemStack = slot.getItem();
            itemstack = slotItemStack.copy();
            if (pIndex > 0) {
                if (!level.isClientSide()) {
                    boolean success = blockEntity.addToMenu(slotItemStack);
                    toggleMenuItemAdditionStatus(success);
                    if (success) {
                        EvilPacketsIHateThem.sendToPlayer(new ClientBoundAddMenuItemPacket(slotItemStack), (ServerPlayer) player);
                    }
                }
                return ItemStack.EMPTY;
            }
        }
        return itemstack;
    }

    private void toggleMenuItemAdditionStatus(boolean success) {
        if (success) {
            menuItemAdditionStatus.set(MenuItemSelectionState.VALID.getCode());
        } else {
            menuItemAdditionStatus.set(MenuItemSelectionState.INVALID.getCode());
        }
    }

    public int getMenuItemAdditionStatus() {
        return this.menuItemAdditionStatus.get();
    }

    public void resetMenuItemAdditionStatus() {
        this.menuItemAdditionStatus.set(MenuItemSelectionState.UNSET.getCode());
    }

    public List<ItemStack> getCafeMenu() {
        return !level.isClientSide && this.blockEntity != null && this.blockEntity.getMenu() != null ? this.blockEntity.getMenu() : this.clientCafeMenu;
    }

    public void addToClientMenu(ItemStack newItemStack) {
        this.clientCafeMenu.add(newItemStack);
    }

    public void removeFromMenu(int index) {
        this.blockEntity.removeFromMenu(index);
    }

    public void removeFromClientMenu(int index) {
        this.clientCafeMenu.remove(index);
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, CozyRegistry.BlockRegistry.CAFE_MANAGER.get());
    }

}