package io.github.chakyl.cozycafe.blockentities;

import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.data.CafeMenuItemRegistry;
import io.github.chakyl.cozycafe.gui.CafeManagerMenu;
import io.github.chakyl.cozycafe.registry.CozyRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CafeManagerBlockEntity extends BlockEntity implements MenuProvider {
    // Temporary data, only relevant when cafe open
    private int servedCustomers = 0;
    // Persistent Data
    private boolean open = false;
    private int dayLastOpened = 0;
    private int stars = 0;
    private String cafeName = "My Cafe";
    private List<ItemStack> menu;

    public CafeManagerBlockEntity(BlockPos pos, BlockState state) {
        super(CozyRegistry.BlockEntityRegistry.CAFE_MANAGER.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) {
        }
    }

    //
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.cozycafe.cafe_manager");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CafeManagerMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putInt("servedCustomers", this.servedCustomers);
        nbt.putBoolean("open", this.open);
        nbt.putInt("dayLastOpened", this.dayLastOpened);
        nbt.putInt("stars", this.stars);
        nbt.putString("cafeName", this.cafeName);
        if (this.menu != null && !this.menu.isEmpty()) {
            ListTag menuList = new ListTag();
            for (ItemStack stack : this.menu) {
                CompoundTag itemTag = new CompoundTag();
                stack.save(itemTag);
                menuList.add(itemTag);
            }
            nbt.put("menu", menuList);
        }
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.servedCustomers = nbt.getInt("servedCustomers");
        this.open = nbt.getBoolean("open");
        this.dayLastOpened = nbt.getInt("dayLastOpened");
        this.stars = nbt.getInt("stars");
        if (nbt.contains("cafeName", Tag.TAG_STRING)) {
            this.cafeName = nbt.getString("cafeName");
        }
        this.menu = new ArrayList<>();
        if (nbt.contains("menu", Tag.TAG_LIST)) {
            ListTag menuList = nbt.getList("menu", Tag.TAG_COMPOUND);
            for (Tag item : menuList) {
                ItemStack stack = ItemStack.of((CompoundTag) item);
                if (!stack.isEmpty()) {
                    this.menu.add(stack);
                }
            }
        }
    }

    public int getServedCustomers() {
        return servedCustomers;
    }

    public void setServedCustomers(int servedCustomers) {
        this.servedCustomers = servedCustomers;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public String getCafeName() {
        return cafeName;
    }

    public void setCafeName(String cafeName) {
        this.cafeName = cafeName;
    }

    public List<ItemStack> getMenu() {

        return this.menu;
    }

    public boolean addToMenu(ItemStack itemToAdd) {
        if (itemToAdd == null || itemToAdd.isEmpty()) return false;

        if (this.menu == null) {
            this.menu = new ArrayList<>();
        }

        if (!CafeMenuItemRegistry.INSTANCE.isMenuItem(itemToAdd.getItem())) return false;
        ItemStack defaultInstance = itemToAdd.getItem().getDefaultInstance();
        for (ItemStack existingItem : this.menu) {
            if (ItemStack.isSameItem(existingItem, defaultInstance)) {
                return false;
            }
        }
        this.menu.add(defaultInstance.copy());
        this.setChanged();
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        return true;
    }

    public void removeFromMenu(int index) {
        if (index < this.menu.size()) this.menu.remove(index);
    }

}
