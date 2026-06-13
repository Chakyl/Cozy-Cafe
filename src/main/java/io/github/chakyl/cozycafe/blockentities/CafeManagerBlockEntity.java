package io.github.chakyl.cozycafe.blockentities;

import io.github.chakyl.cozycafe.blocks.CafeManagerBlock;
import io.github.chakyl.cozycafe.data.CafeMenuItem;
import io.github.chakyl.cozycafe.data.CafeMenuItemRegistry;
import io.github.chakyl.cozycafe.entities.CustomerEntity;
import io.github.chakyl.cozycafe.gui.CafeManagerMenu;
import io.github.chakyl.cozycafe.registry.CozyRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
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
        if (!level.isClientSide() && this.open) {
            if (level.getGameTime() % 20 == 0) {
                scanAreaInFront(level, pos, state);
            }
        }
    }

    private BlockPos getFirstPos(BlockState state, BlockPos pos) {
        int addedRange = this.stars * 2;
        Direction facing = state.getValue(CafeManagerBlock.FACING);
        return pos.relative(facing.getOpposite(), 1).above(-1).relative(facing.getClockWise().getOpposite(), (3 + addedRange) / 2);
    }

    private BlockPos getSecondPos(BlockState state, BlockPos pos) {
        int addedRange = this.stars * 2;
        Direction facing = state.getValue(CafeManagerBlock.FACING);
        return pos.relative(facing.getOpposite(),  6 + addedRange).above(this.stars > 2 ? Math.min(3, this.stars - 1) : 1).relative(facing.getClockWise(), (3 + addedRange) / 2);
    }

    public void scanAreaInFront(Level level, BlockPos pos, BlockState state) {
        BlockPos.betweenClosedStream(this.getFirstPos(state, pos), this.getSecondPos(state, pos)).forEach(scannedPos -> {
            if (!level.isLoaded(scannedPos)) return;
            BlockState scannedState = level.getBlockState(scannedPos);
            // TODO: Add way to view area
            if (false) {
                ((ServerLevel) level).sendParticles(
                        ParticleTypes.HAPPY_VILLAGER,
                        scannedPos.getX() + 0.5, scannedPos.getY() + 0.5, scannedPos.getZ() + 0.5,
                        0,
                        0, 0, 0,
                        0.0
                );
            }
            if (!scannedState.isAir() && scannedState.is(CozyRegistry.BlockRegistry.CAFE_MENU.get())) {
                BlockEntity entity = level.getBlockEntity(scannedPos);
                if (entity instanceof CafeMenuBlockEntity cafeMenuBlockEntity) {
                    // TODO: Store this blockpos to menu for signaling
                    if (cafeMenuBlockEntity.canReceiveNewChoice()) rollMenuCourse(cafeMenuBlockEntity);
                }
            }
            if (!scannedState.isAir() && scannedState.is(Blocks.DIAMOND_BLOCK)) {
                if (Math.random() < 0.1) {
                    CustomerEntity customer = CozyRegistry.EntityRegistry.CUSTOMER.get().create(this.level);
                    if (customer != null) {
                        customer.moveTo(scannedPos.getX(), scannedPos.getY() + 1, scannedPos.getZ(), 0.0f, 0.0f);
                        level.addFreshEntity(customer);
                    }
                }
            }
        });
    }
    public void sendCloseCommandToMenus(Level level, BlockPos pos, BlockState state) {
        BlockPos.betweenClosedStream(this.getFirstPos(state, pos), this.getSecondPos(state, pos)).forEach(scannedPos -> {
            if (!level.isLoaded(scannedPos)) return;
            BlockState scannedState = level.getBlockState(scannedPos);
            if (!scannedState.isAir() && scannedState.is(CozyRegistry.BlockRegistry.CAFE_MENU.get())) {
                BlockEntity entity = level.getBlockEntity(scannedPos);
                if (entity instanceof CafeMenuBlockEntity cafeMenuBlockEntity) {
                    cafeMenuBlockEntity.closeMenu();
                }
            }
        });
    }
    private List<ItemStack> getMenuItemsByCategory(CafeMenuItem.MenuItemCategory category) {
        List<ItemStack> sortedMenuItems = new ArrayList<>();
        for (ItemStack menuItem : this.menu) {
            if (CafeMenuItemRegistry.INSTANCE.getForItem(menuItem.getItem()).category() == category) {
                sortedMenuItems.add(menuItem);
            }
        }
        return sortedMenuItems;
    }

    private void rollMenuCourse(CafeMenuBlockEntity cafeMenuBlockEntity) {
        CafeMenuItem.MenuItemCategory category = CafeMenuItem.MenuItemCategory.MAIN;
        switch (cafeMenuBlockEntity.getCurrentCourse()) {
            case 0:
                category = CafeMenuItem.MenuItemCategory.DRINK;
                break;
            case 2:
                category = CafeMenuItem.MenuItemCategory.DESSERT;
                break;
            case 1:
            default:
                break;
        }
        List<ItemStack> sortedMenuItems = getMenuItemsByCategory(category);
        if (sortedMenuItems.isEmpty()) {
            cafeMenuBlockEntity.setCurrentCourse(cafeMenuBlockEntity.getCurrentCourse() + 1);
        } else if (this.level != null) {
            cafeMenuBlockEntity.setRequestedItem(sortedMenuItems.get(this.level.random.nextInt(sortedMenuItems.size())));

        }

    }
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.cozycafe.cafe_manager");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CafeManagerMenu(containerId, inventory, this);
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
        if (!open) {
            sendCloseCommandToMenus(this.level, this.worldPosition, this.getBlockState());
        }
        this.setChanged();
        level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
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

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }
}
