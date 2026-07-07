package io.github.chakyl.cozycafe.gui;

import io.github.chakyl.cozycafe.blockentities.CafeManagerBlockEntity;
import io.github.chakyl.cozycafe.registry.CozyRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public class CafeManagerMenu extends AbstractContainerMenu {
    public final CafeManagerBlockEntity blockEntity;
    private final Level level;
    private List<ItemStack> clientCafeMenu;

    public CafeManagerMenu(int pContainerId, Inventory pPlayerInventory, FriendlyByteBuf buf) {
        this(pContainerId, pPlayerInventory, pPlayerInventory.player.level().getBlockEntity(buf.readBlockPos()));
        int size = buf.readInt();
        this.clientCafeMenu = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            this.clientCafeMenu.add(buf.readItem());
        }
    }

    public CafeManagerMenu(int pContainerId, Inventory pPlayerInventory, BlockEntity entity) {
        super(CozyRegistry.MenuRegistry.CAFE_MANAGER.get(), pContainerId);
        this.level = pPlayerInventory.player.level();
        blockEntity = ((CafeManagerBlockEntity) entity);
        this.broadcastChanges();
    }

    public void setName(String name) {
        this.blockEntity.setCafeName(name);
    }

    public boolean getIsCafeOpen() {
        if (this.blockEntity.getLevel() != null && this.blockEntity.getLevel().isClientSide()) {
            if (this.blockEntity.getLevel().getBlockEntity(this.blockEntity.getBlockPos()) instanceof CafeManagerBlockEntity cafeManagerBlockEntity) {
                return cafeManagerBlockEntity.isOpen();
            }
        }
        return this.blockEntity.isOpen();
    }

    public int getStars() {
        return this.blockEntity.getStarsFromReputation();
    }

    public String getCafeName() {
        return this.blockEntity.getCafeName();
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                pPlayer, CozyRegistry.BlockRegistry.CAFE_MANAGER.get());
    }

    public List<ItemStack> getCafeMenu() {
        return this.clientCafeMenu;
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }


}