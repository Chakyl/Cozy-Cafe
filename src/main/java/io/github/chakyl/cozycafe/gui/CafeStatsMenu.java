package io.github.chakyl.cozycafe.gui;

import io.github.chakyl.cozycafe.CozyRegistry;
import io.github.chakyl.cozycafe.blockentities.CafeManagerBlockEntity;
import io.github.chakyl.cozycafe.cafemodifiers.CafeModifiers;
import io.github.chakyl.cozycafe.data.CafeTheme;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CafeStatsMenu extends AbstractContainerMenu {
    public final CafeManagerBlockEntity blockEntity;
    private final Player player;
    private final Level level;
    private CafeTheme activeTheme;
    private CafeModifiers cafeModifiers;

    public CafeStatsMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, null, ByteBufCodecs.optional(ByteBufCodecs.fromCodec(CafeTheme.CODEC)).decode(extraData).orElse(null), ByteBufCodecs.optional(ByteBufCodecs.fromCodec(CafeModifiers.CODEC)).decode(extraData).orElse(null));
    }

    public CafeStatsMenu(int containerId, Inventory playerInventory, CafeManagerBlockEntity blockEntity) {
        this(containerId, playerInventory, blockEntity, blockEntity.getActiveTheme(), blockEntity.getCafeModifiers());
    }

    public CafeStatsMenu(int pContainerId, Inventory pPlayerInventory, BlockEntity entity, CafeTheme theme, CafeModifiers modifiers) {
        super(CozyRegistry.MenuRegistry.CAFE_STATS.get(), pContainerId);
        player = pPlayerInventory.player;
        this.level = pPlayerInventory.player.level();
        blockEntity = ((CafeManagerBlockEntity) entity);
        this.activeTheme = theme;
        this.cafeModifiers = modifiers;
        this.broadcastChanges();
    }

    public CafeModifiers getCafeModifiers() {
        return this.cafeModifiers;
    }

    public CafeTheme getCafeTheme() {
        return this.activeTheme;
    }

    public void addToClientMenu(CafeTheme theme, CafeModifiers cafeModifiers) {
        this.activeTheme = theme;
        this.cafeModifiers = cafeModifiers;
    }


    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, CozyRegistry.BlockRegistry.CAFE_MANAGER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }
}