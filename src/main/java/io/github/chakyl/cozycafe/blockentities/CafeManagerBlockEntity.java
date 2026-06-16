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
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
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
import java.util.Collections;
import java.util.List;

public class CafeManagerBlockEntity extends BlockEntity implements MenuProvider {
    // Temporary data, only relevant when cafe open
    private int servedCustomers = 0;
    // Persistent Data
    private boolean open = false;
    private int dayLastOpened = 0;
    private int reputation = 0;
    private BlockPos linkedSign;
    private String cafeName = "My Cafe";
    private List<ItemStack> menu;

    public CafeManagerBlockEntity(BlockPos pos, BlockState state) {
        super(CozyRegistry.BlockEntityRegistry.CAFE_MANAGER.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide() && this.open) {
            if (level.getGameTime() % 20 == 0) {
                assignCustomersInArea(level, pos, state);
            }
        }
    }

    private BlockPos getFirstPos(BlockState state, BlockPos pos) {
        int addedRange = this.getStarsFromReputation() * 2;
        Direction facing = state.getValue(CafeManagerBlock.FACING);
        return pos.relative(facing.getOpposite(), 1).above(-1).relative(facing.getClockWise().getOpposite(), (3 + addedRange) / 2);
    }

    private BlockPos getSecondPos(BlockState state, BlockPos pos) {
        int stars = this.getStarsFromReputation();
        int addedRange = stars * 2;
        Direction facing = state.getValue(CafeManagerBlock.FACING);
        return pos.relative(facing.getOpposite(), 6 + addedRange).above(stars > 2 ? Math.min(3, stars - 1) : 1).relative(facing.getClockWise(), (3 + addedRange) / 2);
    }

    public void assignCustomersInArea(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        List<BlockPos> availableMenuPositions = new ArrayList<>();

        for (BlockPos scannedPos : BlockPos.betweenClosedStream(this.getFirstPos(state, pos), this.getSecondPos(state, pos)).map(BlockPos::immutable).toList()) {
            if (!serverLevel.isLoaded(scannedPos)) continue;
            BlockState scannedState = serverLevel.getBlockState(scannedPos);
            if (false) {
                serverLevel.sendParticles(
                        ParticleTypes.HAPPY_VILLAGER,
                        scannedPos.getX() + 0.5, scannedPos.getY() + 0.5, scannedPos.getZ() + 0.5,
                        1, 0, 0, 0, 0.0
                );
            }
            if (scannedState.is(CozyRegistry.BlockRegistry.CAFE_MENU.get())) {
                if (serverLevel.getBlockEntity(scannedPos) instanceof CafeMenuBlockEntity cafeMenuBlockEntity) {
                    if (serverLevel.random.nextFloat() < 0.1f && cafeMenuBlockEntity.canReceiveNewCustomer()) {
                        availableMenuPositions.add(scannedPos);
                        cafeMenuBlockEntity.setCustomerTravelTime(0);
                        cafeMenuBlockEntity.setCafeManager(pos);
                    }
                }
            }
        }

        Collections.shuffle(availableMenuPositions);
        if (!serverLevel.isLoaded(this.linkedSign) || availableMenuPositions.isEmpty()) return;
        if (serverLevel.getBlockState(this.linkedSign).is(CozyRegistry.BlockRegistry.CAFE_SIGN.get())) {
            CustomerEntity customer = CozyRegistry.EntityRegistry.CUSTOMER.get().create(serverLevel);
            if (customer != null) {
                BlockPos targetMenuPos = availableMenuPositions.remove(0);
                customer.moveTo(this.linkedSign.getX() + 0.5, this.linkedSign.getY() + 1.0, this.linkedSign.getZ() + 0.5, serverLevel.random.nextFloat() * 360.0F, 0.0F);
                serverLevel.addFreshEntity(customer);
                customer.setTargetMenuPos(targetMenuPos);
            }

        }
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

    public void rollMenuCourse(CafeMenuBlockEntity cafeMenuBlockEntity) {
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

    public BlockPos getLinkedSign() {
        return linkedSign;
    }

    public void setLinkedSign(BlockPos linkedSign) {
        this.linkedSign = linkedSign;
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

    public int getStarsFromReputation() {
        return Mth.clamp((int) Math.floor((double) reputation / 1000), 0, 5);
    }

    public int getReputation() {
        return reputation;
    }

    public void setReputation(int reputation) {
        this.reputation = reputation;
    }

    public void increaseReputation(int reputation) {
        this.reputation += reputation;
        this.reputation = Mth.clamp(this.reputation, 0, 5000);
    }

    public void decreaseReputation(int reputation) {
        this.reputation -= reputation;
        this.reputation = Mth.clamp(this.reputation, 0, 5000);
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
        nbt.putInt("reputation", this.reputation);
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
        if (this.linkedSign != null) {
            nbt.put("LinkedSign", NbtUtils.writeBlockPos(this.linkedSign));
        }
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.servedCustomers = nbt.getInt("servedCustomers");
        this.open = nbt.getBoolean("open");
        this.dayLastOpened = nbt.getInt("dayLastOpened");
        this.reputation = nbt.getInt("reputation");
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
        if (nbt.contains("LinkedSign")) {
            this.linkedSign = NbtUtils.readBlockPos(nbt.getCompound("LinkedSign"));
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
