package io.github.chakyl.cozycafe.blockentities;

import de.cadentem.quality_food.util.QualityUtils;
import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.blocks.CafeManagerBlock;
import io.github.chakyl.cozycafe.data.CafeMenuItem;
import io.github.chakyl.cozycafe.data.CafeMenuItemRegistry;
import io.github.chakyl.cozycafe.entities.CustomerEntity;
import io.github.chakyl.cozycafe.gui.CafeManagerMenu;
import io.github.chakyl.cozycafe.network.ClientBoundCafeCannotOpenPacket;
import io.github.chakyl.cozycafe.network.EvilPacketsIHateThem;
import io.github.chakyl.cozycafe.registry.CozyRegistry;
import io.github.chakyl.cozycafe.util.GeneralUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

import static java.util.Comparator.comparingInt;

public class CafeManagerBlockEntity extends BlockEntity implements MenuProvider {
    private final boolean EVENT_LOGGING = false;
    // Temporary data, only relevant when cafe open
    private int attemptedCustomers = 0;
    // Persistent Data
    private boolean open = false;
    private int dayLastOpened = 0;
    private int reputation = 0;
    private BlockPos linkedSign;
    private String cafeName = "Cozy Cafe";
    private List<ItemStack> menu;

    public CafeManagerBlockEntity(BlockPos pos, BlockState state) {
        super(CozyRegistry.BlockEntityRegistry.CAFE_MANAGER.get(), pos, state);

    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide() && this.open) {
            if (level.getGameTime() % 20 == 0 && this.attemptedCustomers >= this.getMaxCustomers()) {
                if (EVENT_LOGGING) CozyCafe.LOGGER.info("[CAFE] Closing Cafe due to reaching max attempted customers");
                this.setOpen(!open, false);
                this.setChanged();
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                return;
            }

            if ((level.getGameTime() % (20 * ((long) CozyCafe.CONFIG.customerSpawnInterval.get() * (1 - (Math.max(0, this.reputation - 1) / 5000))))) == 0) {
                if (this.dayLastOpened != GeneralUtils.getDay(this.level)) {
                    if (EVENT_LOGGING) CozyCafe.LOGGER.info("[CAFE] Force-Closing Cafe due to reaching next day");
                    this.setOpen(!open, false);
                } else {
                    assignCustomersInArea(level, pos, state);
                }
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
        if (EVENT_LOGGING) CozyCafe.LOGGER.info("[CAFE] Running menu assignment");
        List<BlockPos> availableMenuPositions = new ArrayList<>();

        for (BlockPos scannedPos : BlockPos.betweenClosedStream(this.getFirstPos(state, pos), this.getSecondPos(state, pos)).map(BlockPos::immutable).toList()) {
            if (!serverLevel.isLoaded(scannedPos)) continue;
            BlockState scannedState = serverLevel.getBlockState(scannedPos);
            if (scannedState.is(CozyRegistry.BlockRegistry.CAFE_MENU.get())) {
                if (serverLevel.getBlockEntity(scannedPos) instanceof CafeMenuBlockEntity cafeMenuBlockEntity) {
                    if (cafeMenuBlockEntity.canReceiveNewCustomer()) {
                        availableMenuPositions.add(scannedPos);
                    }
                }
            }
        }

        Collections.shuffle(availableMenuPositions);
        if (EVENT_LOGGING) CozyCafe.LOGGER.info("[CAFE] Max Potential: " + availableMenuPositions.size());
        if (!serverLevel.isLoaded(this.linkedSign) || availableMenuPositions.isEmpty()) return;
        int i = 0;
        for (BlockPos menuPos : availableMenuPositions) {
            if (level.getBlockEntity(menuPos) instanceof CafeMenuBlockEntity cafeMenuBlockEntity && serverLevel.getBlockState(this.linkedSign).is(CozyRegistry.BlockRegistry.CAFE_SIGN.get())) {
                CustomerEntity customer = CozyRegistry.EntityRegistry.CUSTOMER.get().create(serverLevel);
                if (customer != null) {
                    cafeMenuBlockEntity.setCustomerTravelTime(0);
                    cafeMenuBlockEntity.setCafeManager(pos);
                    customer.moveTo(this.linkedSign.getX() + 0.5, this.linkedSign.getY(), this.linkedSign.getZ() + 0.5, serverLevel.random.nextFloat() * 360.0F, 0.0F);
                    serverLevel.addFreshEntity(customer);
                    customer.setTargetMenuPos(menuPos);
                    String skinUsername = rollCustomerSkin();
                    customer.setCustomerSkinFromUsername(skinUsername);
                    cafeMenuBlockEntity.setCustomerSkinFromUsername(skinUsername);
                    this.attemptedCustomers++;
                    i++;
                    if (i == 4 || serverLevel.random.nextFloat() < CozyCafe.CONFIG.groupCustomerChance.get()) break;
                }
            }
        }
    }

    private String rollCustomerSkin() {
        List<? extends String> usernames = CozyCafe.CONFIG.customerUsernames.get();
        if (!usernames.isEmpty()) {
            return usernames.get(level.getRandom().nextInt(usernames.size()));
        }
        return "MHF_Steve";
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
        int currentCourse = cafeMenuBlockEntity.getCurrentCourse();
        switch (currentCourse) {
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
            cafeMenuBlockEntity.setCurrentCourse(currentCourse + 1, false);
            if (currentCourse + 1 < 3) {
                this.rollMenuCourse(cafeMenuBlockEntity);
            }
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

    public void clearCafeData() {
        if (!this.level.isClientSide) {
            this.attemptedCustomers = 0;
            this.open = false;
            this.dayLastOpened = 0;
            this.reputation = 0;
            this.linkedSign = null;
            this.cafeName = "Cozy Cafe";
            this.menu = null;
            this.setChanged();
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void showCafeArea() {
        if (!(this.level instanceof ServerLevel serverLevel)) return;
        BlockState state = this.level.getBlockState(this.getBlockPos());
        BlockPos pos = this.getBlockPos();
        for (BlockPos scannedPos : BlockPos.betweenClosedStream(this.getFirstPos(state, pos), this.getSecondPos(state, pos)).map(BlockPos::immutable).toList()) {
            if (!serverLevel.isLoaded(scannedPos)) continue;
            serverLevel.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    scannedPos.getX() + 0.5, scannedPos.getY() + 0.5, scannedPos.getZ() + 0.5,
                    1, 0, 0, 0, 0.0
            );
        }
    }

    public int getAttemptedCustomers() {
        return attemptedCustomers;
    }

    public void setAttemptedCustomers(int attemptedCustomers) {
        this.attemptedCustomers = attemptedCustomers;
    }

    private int getMaxCustomers() {
        int stars = this.getStarsFromReputation();
        return (int) ((stars == 5 ? 40 : ((stars + 1) * 5)) + Mth.randomBetween(this.getLevel().getRandom(), 0, 5));
    }

    public BlockPos getLinkedSign() {
        return linkedSign;
    }

    public void setLinkedSign(BlockPos linkedSign) {
        this.linkedSign = linkedSign;
    }

    public boolean isOpen() {
        return this.open;
    }

    public int getDayLastOpened() {
        return this.dayLastOpened;
    }

    public void toggleOpenFromMenu(ServerPlayer player) {
        if (!this.open && !this.canBeOpened(player)) return;
        this.toggleOpen();
    }

    private boolean canBeOpened(ServerPlayer player) {
        int stars = this.getStarsFromReputation();
        int menuSizePerStar = CozyCafe.CONFIG.menuSizePerStar.get();
        if (this.linkedSign == null || !this.level.isLoaded(this.linkedSign) || (this.level.isLoaded(this.linkedSign) && !(this.level.getBlockEntity(this.linkedSign) instanceof CafeSignBlockEntity))) {
            EvilPacketsIHateThem.sendToPlayer(new ClientBoundCafeCannotOpenPacket((byte) 0), player);
            return false;
        }
        if (this.menu == null || stars == 0 && this.menu.size() < menuSizePerStar) {
            EvilPacketsIHateThem.sendToPlayer(new ClientBoundCafeCannotOpenPacket((byte) 1), player);
            return false;
        }
        if (this.menu.size() < menuSizePerStar * stars) {
            EvilPacketsIHateThem.sendToPlayer(new ClientBoundCafeCannotOpenPacket((byte) 2), player);
            return false;
        }
        if (this.dayLastOpened == GeneralUtils.getDay(this.level)) {
            EvilPacketsIHateThem.sendToPlayer(new ClientBoundCafeCannotOpenPacket((byte) 3), player);
            return false;
        }
        if (this.hasNearbyOpenManagers()) {
            EvilPacketsIHateThem.sendToPlayer(new ClientBoundCafeCannotOpenPacket((byte) 4), player);
            return false;
        }
        return true;
    }


    private boolean hasNearbyOpenManagers() {
        if (this.level == null) return false;
        for (BlockPos targetPos : BlockPos.betweenClosed(this.worldPosition.offset(-4, -4, -4), this.worldPosition.offset(4, 4, 4))) {
            if (!targetPos.equals(this.worldPosition)) {
                if (this.level.getBlockEntity(targetPos) instanceof CafeManagerBlockEntity cafeManagerBlockEntity) {
                    if (cafeManagerBlockEntity.isOpen()) return true;
                }
            }
        }

        return false;
    }

    public void toggleOpen() {
        this.setOpen(!this.open, true);
    }

    public void setOpen(boolean open, boolean forceClose) {
        this.open = open;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
        CafeSignBlockEntity cafeSignBlockEntity = null;
        if (this.level.isLoaded(this.linkedSign) && this.level.getBlockEntity(this.linkedSign) instanceof CafeSignBlockEntity sign) {
            cafeSignBlockEntity = sign;
        }
        if (cafeSignBlockEntity == null) return;
        if (!open) {
            if (forceClose) sendCloseCommandToMenus(this.level, this.worldPosition, this.getBlockState());
            this.attemptedCustomers = 0;
        } else {
            this.dayLastOpened = GeneralUtils.getDay(this.level);
        }
        cafeSignBlockEntity.setOpen(open);

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

        this.setChanged();
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    public List<ItemStack> getMenu() {
        return this.menu;
    }

    public Tag serializeMenuNBT() {
        if (this.menu != null && !this.menu.isEmpty()) {
            this.sortMenuByCategory();
            ListTag menuList = new ListTag();
            for (ItemStack stack : this.menu) {
                CompoundTag itemTag = new CompoundTag();
                stack.save(itemTag);
                menuList.add(itemTag);
            }
            return menuList;
        }
        return new ListTag();
    }

    public boolean onlyHasDesserts() {
        for (ItemStack menuItem : this.menu) {
            CafeMenuItem.MenuItemCategory category = CafeMenuItemRegistry.INSTANCE.getForItem(menuItem.getItem()).category();
            if (CafeMenuItemRegistry.INSTANCE.getForItem(menuItem.getItem()).category() != CafeMenuItem.MenuItemCategory.DESSERT)
                return false;
        }
        return true;
    }

    public void sortMenuByCategory() {
        this.menu.sort(comparingInt(item -> {
            CafeMenuItem.MenuItemCategory category = CafeMenuItemRegistry.INSTANCE.getForItem(item.getItem()).category();
            return switch (category) {
                case DRINK -> 0;
                case MAIN -> 1;
                case DESSERT -> 2;
            };
        }));
    }

    public boolean addToMenu(ItemStack itemToAdd) {
        if (itemToAdd == null || itemToAdd.isEmpty()) return false;

        if (this.menu == null) {
            this.menu = new ArrayList<>();
        }
        if (this.menu.size() >= 25) return false;
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


    private void handleReputation(CafeMenuItem menuItem, ItemStack handStack, double waitTimeDiff) {
        double increase = switch (menuItem.category()) {
            case DRINK -> 2;
            case MAIN -> 10;
            case DESSERT -> 4;
        };
        if (CozyCafe.QUALITY_FOOD_INSTALLED && QualityUtils.hasQuality(handStack)) {
            increase += switch (QualityUtils.getQuality(handStack)) {
                case GOLD -> 4;
                case DIAMOND -> 8;
                case UNDEFINED, IRON -> 2;
                default -> 1;
            };
        }
        if (waitTimeDiff >= 0.95) increase *= 2;
        if (waitTimeDiff >= 0.75) increase *= 1.5;
        if (waitTimeDiff >= 5) increase *= 1.25;
        if (EVENT_LOGGING) CozyCafe.LOGGER.info("[CAFE] Reputation: +" + increase);
        this.increaseReputation((int) Math.floor(increase));
    }

    public void handleSuccessfulServe(CafeMenuItem menuItem, ItemStack handStack, double waitTimeDiff) {
        this.handleReputation(menuItem, handStack, waitTimeDiff);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putInt("attemptedCustomers", this.attemptedCustomers);
        nbt.putBoolean("open", this.open);
        nbt.putInt("dayLastOpened", this.dayLastOpened);
        nbt.putInt("reputation", this.reputation);
        nbt.putString("cafeName", this.cafeName);
        if (this.menu != null && !this.menu.isEmpty()) {
            this.sortMenuByCategory();
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
        this.attemptedCustomers = nbt.getInt("attemptedCustomers");
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

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            this.load(tag);
        }
    }
}
