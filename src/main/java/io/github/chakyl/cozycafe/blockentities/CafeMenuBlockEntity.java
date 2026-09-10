package io.github.chakyl.cozycafe.blockentities;

import dev.latvian.mods.kubejs.core.PlayerKJS;
import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.CozyRegistry;
import io.github.chakyl.cozycafe.blocks.CafeMenuBlock;
import io.github.chakyl.cozycafe.cafemodifiers.CafeModifiers;
import io.github.chakyl.cozycafe.data.CafeMenuItem;
import io.github.chakyl.cozycafe.data.CafeMenuItemRegistry;
import io.github.chakyl.cozycafe.data.CafeModifier;
import io.github.chakyl.cozycafe.data.CafeTheme;
import io.github.chakyl.cozycafe.entities.CustomerEntity;
import io.github.chakyl.cozycafe.item.ServingPlateItem;
import io.github.chakyl.cozycafe.util.CustomerEntityUtils;
import io.github.chakyl.cozycafe.util.CustomerTarget;
import io.github.chakyl.cozycafe.util.ModifierUtils;
import io.github.chakyl.cozycafe.util.PaymentUtils;
import io.github.chakyl.numismaticsutils.utils.CurioUtils;
import io.netty.util.internal.StringUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.Locale;

import static io.github.chakyl.cozycafe.tags.CozyTags.PICKLE;
import static io.github.chakyl.cozycafe.util.PaymentUtils.getMultAttributeMultiplier;
import static io.github.chakyl.cozycafe.util.QualityFoods.getQualityPriceIncrease;

public class CafeMenuBlockEntity extends BlockEntity {
    public static int MAX_TRAVEL_TIME = 600;
    private static final int ORDER_TIME = CozyCafe.CONFIG.customerOrderTime.get();
    private static Integer maxWaitTime;
    private int currentCourse = 0;
    private int waitTime = -1;
    private int orderTime = -1;
    private ItemStack droppedItem = ItemStack.EMPTY;
    private boolean hasCustomer = false;
    private int customerTravelTime = -1;
    private BlockPos cafeManager;
    private ItemStack requestedItem = ItemStack.EMPTY;
    private ItemStack eatingItem = ItemStack.EMPTY;

    private String customerSkin = "";

    public CafeMenuBlockEntity(BlockPos pos, BlockState state) {
        super(CozyRegistry.BlockEntityRegistry.CAFE_MENU.get(), pos, state);
    }

    public static int getMaxWaitTime() {
        return maxWaitTime;
    }

    public static void setMaxWaitTime(int maxWaitTime) {
        CafeMenuBlockEntity.maxWaitTime = maxWaitTime;
    }

    private int getWaitTimeFromModifiers(int initialWaitTIme) {
        int resolvedWaitTime = initialWaitTIme;
        CafeManagerBlockEntity cafeManagerBlockEntity = this.getCafeManager(this.getLevel());
        if (cafeManagerBlockEntity == null) return initialWaitTIme;
        if (cafeManagerBlockEntity.getCafeModifiers() != null) {
            for (CafeModifier cafeModifier : cafeManagerBlockEntity.getCafeModifiers()) {
                resolvedWaitTime = ModifierUtils.getModifierResolvedWaitTimes(cafeModifier, resolvedWaitTime);
            }
        }
        if (cafeManagerBlockEntity.getActiveTheme() != null) {
            if (cafeManagerBlockEntity.getActiveTheme().modifier() != null) {
                resolvedWaitTime = ModifierUtils.getModifierResolvedWaitTimes(cafeManagerBlockEntity.getActiveTheme().modifier(), resolvedWaitTime);
            }
        }
        return (int) (double) resolvedWaitTime;
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (!pLevel.isClientSide()) {
            if (this.hasCustomer && maxWaitTime == null) {
                maxWaitTime = getWaitTimeFromModifiers(CozyCafe.CONFIG.customerWaitTime.get());
            }
            if (this.customerTravelTime > -1 && this.customerTravelTime < MAX_TRAVEL_TIME) {
                this.customerTravelTime++;
                this.setChanged();
            } else if (this.customerTravelTime == MAX_TRAVEL_TIME) {
                this.onCustomerArrived(null);
                this.setChanged();
            }
            if (this.canReceiveNewChoice()) {
                if (this.orderTime > -1 && this.orderTime < ORDER_TIME) {
                    this.orderTime++;
                    this.setChanged();
                } else if (this.orderTime == ORDER_TIME) {
                    CafeManagerBlockEntity cafeManagerBlockEntity = this.getCafeManager(this.level);
                    if (cafeManagerBlockEntity == null) {
                        this.closeMenu(true);
                        return;
                    } else {
                        boolean shouldClose = this.currentCourse > 2 || !this.orderedDessert(this.currentCourse) || this.currentCourse == 1 && (!cafeManagerBlockEntity.hasFoodType(CafeMenuItem.MenuItemCategory.MAIN) && !cafeManagerBlockEntity.hasFoodType(CafeMenuItem.MenuItemCategory.DESSERT));
                        if (!shouldClose) cafeManagerBlockEntity.rollMenuCourse(this);
                        this.orderTime = -1;
                        if (this.hasMains()) {
                            if (this.currentCourse == 2) {
                                if (!this.droppedItem.isEmpty()) {
                                    pLevel.addFreshEntity(new ItemEntity(pLevel, pPos.getX() + 0.5, pPos.getY() + 0.5, pPos.getZ() + 0.5, this.droppedItem));
                                    this.droppedItem = ItemStack.EMPTY;
                                    this.level.setBlock(this.worldPosition, pState.setValue(CafeMenuBlock.DISH, false), 3);
                                } else {
                                    if (CozyCafe.CONFIG.platingRequired.get()) {
                                        this.level.setBlock(this.worldPosition, pState.setValue(CafeMenuBlock.DIRTY, true), 3);
                                    } else {
                                        this.level.setBlock(this.worldPosition, pState.setValue(CafeMenuBlock.DISH, false), 3);
                                    }
                                }
                            } else if (currentCourse == 1 && !this.droppedItem.isEmpty()) {
                                pLevel.addFreshEntity(new ItemEntity(pLevel, pPos.getX() + 0.5, pPos.getY() + 0.5, pPos.getZ() + 0.5, this.droppedItem));
                                this.droppedItem = ItemStack.EMPTY;
                            }
                        }
                        if (shouldClose) {
                            this.closeMenu(true);
                        }
                        this.setChangedForRender();
                    }
                }
            }
            if (!requestedItem.isEmpty()) {
                if (this.waitTime == -1) {
                    this.waitTime = 0;
                    this.setChangedForRender();
                } else {
                    if (this.waitTime == maxWaitTime) {
                        getCafeManager(pLevel);
                        this.closeMenu(false);
                    } else {
                        this.waitTime++;
                        this.setChanged();
                        if (this.waitTime >= 300 && this.level.getGameTime() % 100 == 0) this.setChangedForRender();
                    }
                }
            }
        }
    }

    private CafeManagerBlockEntity getCafeManager(Level level) {
        if (this.cafeManager != null && level.isLoaded(this.cafeManager) && level.getBlockEntity(this.cafeManager) instanceof CafeManagerBlockEntity cafeManagerBlockEntity) {
            return cafeManagerBlockEntity;
        }
        return null;
    }

    public void handleServe(BlockPos pPos, Player pPlayer, ItemStack handStack) {
        CafeMenuItem menuItem = CafeMenuItemRegistry.INSTANCE.getForItem(requestedItem.getItem());
        boolean isMain = menuItem.category() == CafeMenuItem.MenuItemCategory.MAIN;
        ItemStack resolvedStack = handStack.copy();
        if (isMain || handStack.is(requestedItem.getItem())) {
            if (isMain) {
                boolean success = false;
                if (handStack.is(CozyRegistry.ItemRegistry.SERVING_PLATE.get()) && ServingPlateItem.getStoredFood(handStack).is(requestedItem.getItem())) {
                    resolvedStack = ServingPlateItem.getStoredFood(handStack).copy();
                    success = true;
                } else if (handStack.is(requestedItem.getItem())) {
                    if (CozyCafe.CONFIG.platingRequired.get() && !menuItem.bowlFood()) {
                        pPlayer.displayClientMessage(Component.translatable("block.cozycafe.cafe_menu.not_plated").withStyle(ChatFormatting.RED), true);
                    } else {
                        if (menuItem.bowlFood()) {
                            this.droppedItem = menuItem.bowl().getDefaultInstance().copy();
                        }
                        success = true;
                    }
                }
                if (!success) {
                    pPlayer.level().playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    return;
                }
            }
            if (menuItem.bottleDrink()) {
                this.droppedItem = menuItem.bottle().getDefaultInstance().copy();
            }
            if (!pPlayer.isCreative()) handStack.shrink(1);
            this.orderTime = 0;
            this.setCurrentCourse(this.currentCourse + 1, true);
            this.setEatingItem(this.requestedItem.copy());
            this.setRequestedItem(ItemStack.EMPTY);
            ((ServerLevel) level).sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    pPos.getX() + 0.5, pPos.getY() + 0.5, pPos.getZ() + 0.5,
                    5,
                    0.5, 0.5, 0.5,
                    1.0
            );
            pPlayer.level().playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
            CafeManagerBlockEntity cafeManagerBlockEntity = this.getCafeManager(this.level);
            if (cafeManagerBlockEntity != null) {
                cafeManagerBlockEntity.handleSuccessfulServe(menuItem, resolvedStack, (double) maxWaitTime / this.waitTime);
                this.handlePayment(pPos, pPlayer, cafeManagerBlockEntity, menuItem, resolvedStack);
            }
            this.waitTime = -1;
            this.setChanged();
        } else {
            pPlayer.level().playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    private int getPriceFromModifiers(int initialPrice, CafeMenuItem menuItem, CafeModifiers cafeModifiers, CafeTheme activeTheme, Boolean tipPhase) {
        int resolvedPrice = initialPrice;
        if (cafeModifiers != null) {
            for (CafeModifier cafeModifier : cafeModifiers) {
                resolvedPrice = ModifierUtils.getModifierResolvedPrice(menuItem, tipPhase, cafeModifier, resolvedPrice);
            }
        }
        if (activeTheme != null) {
            if (activeTheme.modifier() != null) {
                resolvedPrice = ModifierUtils.getModifierResolvedPrice(menuItem, tipPhase, activeTheme.modifier(), resolvedPrice);
            }
            if (menuItem.themes().contains(activeTheme.themeId())) resolvedPrice /= 2;

        }
        return (int) Math.floor(resolvedPrice);
    }

    public void handlePayment(BlockPos pPos, Player pPlayer, CafeManagerBlockEntity cafeManagerBlockEntity, CafeMenuItem cafeMenuItem, ItemStack handStack) {
        double resolvedPrice = cafeMenuItem.price();

        if (CozyCafe.KUBEJS_INSTALLED) {
            if (cafeMenuItem.item().getDefaultInstance().is(PICKLE) && ((PlayerKJS) pPlayer).kjs$getStages().has(CozyCafe.CONFIG.pickle_bonus_stage.get())) {
                resolvedPrice *= 2;
            }
        }
        if (CozyCafe.QUALITY_FOOD_INSTALLED) {
            resolvedPrice = getQualityPriceIncrease(pPlayer, handStack, resolvedPrice);
        }
        resolvedPrice = getPriceFromModifiers((int) Math.floor(resolvedPrice), cafeMenuItem, cafeManagerBlockEntity.getCafeModifiers(), cafeManagerBlockEntity.getActiveTheme(), false);
        if (this.waitTime < maxWaitTime) {
            // In theory this would make your mult start at 2x and then slowly reduce to 1 for service speed
            int tip = (int) Math.floor(resolvedPrice * (2 - ((double) this.waitTime / maxWaitTime)));
            tip = getPriceFromModifiers(tip, cafeMenuItem, cafeManagerBlockEntity.getCafeModifiers(), cafeManagerBlockEntity.getActiveTheme(), true);
            resolvedPrice += tip;
        }
        resolvedPrice *= getMultAttributeMultiplier(pPlayer, cafeMenuItem);
        int finalPrice = Mth.floor(resolvedPrice);
        if (CozyCafe.CONFIG.numismaticsUtilsPayment.get() && CozyCafe.NUMISMATICS_UTILS_INSTALLED) {
            CurioUtils.depositIntoPersonalOrCurio(this.level, pPlayer, finalPrice);
            pPlayer.displayClientMessage(Component.translatable("block.cozycafe.cafe_menu.payment", NumberFormat.getNumberInstance(Locale.US).format(finalPrice)), true);
        } else {
            for (ItemStack stack : PaymentUtils.getPaymentItems(finalPrice)) {
                if (!stack.isEmpty()) {
                    this.level.addFreshEntity(new ItemEntity(this.level, pPos.getX() + 0.5, pPos.getY() + 0.5, pPos.getZ() + 0.5, stack));
                }
            }
        }
    }


    public void handleClearDirtyIfPossible(BlockPos pPos, Player pPlayer, ItemStack handStack) {
        if (this.getBlockState().getValue(CafeMenuBlock.DIRTY)) {
            if (!pPlayer.getInventory().add(CozyRegistry.ItemRegistry.DIRTY_SERVING_PLATE.get().getDefaultInstance())) {
                pPlayer.drop(CozyRegistry.ItemRegistry.DIRTY_SERVING_PLATE.get().getDefaultInstance(), false);
            }

            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(CafeMenuBlock.DISH, false).setValue(CafeMenuBlock.DIRTY, false), 3);
        }
    }

    public boolean canServe() {
        return !requestedItem.isEmpty();
    }

    public boolean canReceiveNewChoice() {
        return this.hasCustomer && requestedItem.isEmpty() && waitTime == -1;
    }

    public boolean canReceiveNewCustomer() {
        return this.getCustomerTravelTime() == -1 && !this.getHasCustomer() && !this.getBlockState().getValue(CafeMenuBlock.DIRTY);
    }

    public int getCustomerTravelTime() {
        return customerTravelTime;
    }

    public void setCustomerTravelTime(int customerTravelTime) {
        this.customerTravelTime = customerTravelTime;
    }

    public BlockPos getCafeManager() {
        return cafeManager;
    }

    public void setCafeManager(BlockPos cafeManager) {
        this.cafeManager = cafeManager;
    }

    public int getCurrentCourse() {
        return currentCourse;
    }

    public boolean orderedDessert(int currentCourse) {
        if (currentCourse != 2) return true;
        CafeManagerBlockEntity cafeManagerBlockEntity = this.getCafeManager(this.level);
        if (cafeManagerBlockEntity != null && !cafeManagerBlockEntity.hasFoodType(CafeMenuItem.MenuItemCategory.DESSERT))
            return false;
        if (cafeManagerBlockEntity != null && cafeManagerBlockEntity.onlyHasCategory(CafeMenuItem.MenuItemCategory.DESSERT)) {
            return true;
        }
        return Math.random() < CozyCafe.CONFIG.dessertChance.get();
    }

    public boolean hasMains() {
        CafeManagerBlockEntity cafeManagerBlockEntity = this.getCafeManager(this.level);
        if (cafeManagerBlockEntity != null) {
            return cafeManagerBlockEntity.hasMenuCategory(CafeMenuItem.MenuItemCategory.MAIN);
        }
        return false;
    }

    public void setCurrentCourse(int currentCourse, boolean setPlate) {
        if (currentCourse <= 3) {
            this.currentCourse = currentCourse;
            if (setPlate && currentCourse == 2) {
                this.level.setBlock(this.worldPosition, this.getBlockState().setValue(CafeMenuBlock.DISH, true), 3);
            }
            this.setChangedForRender();
        }
    }

    public void closeMenu(boolean isGentleClose) {
        if (this.hasCustomer) {
            CustomerEntity customer = CozyRegistry.EntityRegistry.CUSTOMER.get().create(this.level);
            if (customer != null) {
                CustomerEntityUtils.spawnCustomerAndTarget(this.worldPosition.relative(this.getBlockState().getValue(CafeMenuBlock.FACING)), (ServerLevel) this.level, this.customerSkin, this.getCafeManager(this.level).getLinkedSign().below(), customer, CustomerTarget.SIGN);
            }
        }
        if (!isGentleClose) {
            CafeManagerBlockEntity cafeManagerBlockEntity = this.getCafeManager(this.level);
            if (cafeManagerBlockEntity != null) {
                cafeManagerBlockEntity.decreaseReputation(50);
            }
        }
        this.hasCustomer = false;
        this.level.setBlock(this.worldPosition, this.getBlockState().setValue(CafeMenuBlock.DISH, false), 3);
        this.waitTime = -1;

        this.orderTime = -1;
        this.customerTravelTime = -1;
        this.currentCourse = 0;
        this.hasCustomer = false;
        this.customerSkin = "";
        this.requestedItem = ItemStack.EMPTY;
        this.eatingItem = ItemStack.EMPTY;
        this.setChangedForRender();
    }


    public void onCustomerArrived(PathfinderMob customer) {
        if (!this.level.isClientSide() && !this.hasCustomer) {
            if (customer instanceof CustomerEntity customerEntity) {
                customerEntity.setRemoved(Entity.RemovalReason.UNLOADED_TO_CHUNK);
            }
            CafeManagerBlockEntity cafeManagerBlockEntity = this.getCafeManager(this.level);
            if (cafeManagerBlockEntity == null) {
                this.closeMenu(true);
                return;
            }
            this.hasCustomer = true;
            this.customerTravelTime = -1;
            this.orderTime = 0;
            this.setChangedForRender();
        }
    }

    public int getWaitTime() {
        return this.waitTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public ItemStack getRequestedItem() {
        return this.requestedItem;
    }

    public void setRequestedItem(ItemStack requestedItem) {
        this.requestedItem = requestedItem;
        this.setChangedForRender();
    }

    public ItemStack getEatingItem() {
        return this.eatingItem;
    }

    public void setEatingItem(ItemStack eatingItem) {
        this.eatingItem = eatingItem;
        this.setChangedForRender();
    }

    public boolean getHasCustomer() {
        return this.hasCustomer;
    }
//  TODO: Not 1.21.1?
//    @Override
//    public AABB getRenderBoundingBox() {
//        return new AABB(this.worldPosition).expandTowards(0, 1.5, 0).inflate(0.5, 0, 0.5);
//    }

    private void setChangedForRender() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public void setCustomerSkinFromUsername(String name) {
        if (StringUtil.isNullOrEmpty(name)) return;
        this.customerSkin = name;
        setChangedForRender();
    }

    public String getCustomerSkin() {
        return this.customerSkin;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("current_course", this.currentCourse);
        tag.putInt("wait_time", this.waitTime);
        tag.putInt("order_time", this.orderTime);
        tag.putInt("customer_travel_time", this.customerTravelTime);
        if (!this.droppedItem.isEmpty()) {
            tag.put("dropped_item", this.droppedItem.save(provider, new CompoundTag()));
        }
        tag.putBoolean("has_customer", this.hasCustomer);
        if (this.cafeManager != null) {
            BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, this.cafeManager).result().ifPresent(t -> tag.put("cafe_manager", t));
        }
        if (!this.requestedItem.isEmpty()) {
            tag.put("requested_item", this.requestedItem.save(provider, new CompoundTag()));
        }
        if (!this.eatingItem.isEmpty()) {
            tag.put("eating_item", this.eatingItem.save(provider, new CompoundTag()));
        }
        tag.putString("customer_skin", this.customerSkin);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.currentCourse = tag.getInt("current_course");
        this.waitTime = tag.getInt("wait_time");
        this.orderTime = tag.getInt("order_time");
        this.customerTravelTime = tag.getInt("customer_travel_time");
        this.hasCustomer = tag.getBoolean("has_customer");
        if (tag.contains("dropped_item")) {
            ItemStack.parse(provider, tag.getCompound("dropped_item")).ifPresentOrElse(stack -> this.droppedItem = stack, () -> this.droppedItem = ItemStack.EMPTY);
        } else {
            this.droppedItem = ItemStack.EMPTY;
        }
        if (tag.contains("cafe_manager")) {
            BlockPos.CODEC.parse(NbtOps.INSTANCE, tag.get("cafe_manager")).result().ifPresent(pos -> this.cafeManager = pos);
        } else {
            this.cafeManager = null;
        }
        if (tag.contains("requested_item")) {
            ItemStack.parse(provider, tag.getCompound("requested_item")).ifPresentOrElse(stack -> this.requestedItem = stack, () -> this.requestedItem = ItemStack.EMPTY);
        } else {
            this.requestedItem = ItemStack.EMPTY;
        }
        if (tag.contains("eating_item")) {
            ItemStack.parse(provider, tag.getCompound("eating_item")).ifPresentOrElse(s -> this.eatingItem = s, () -> this.eatingItem = ItemStack.EMPTY);
        } else {
            this.eatingItem = ItemStack.EMPTY;
        }
        this.customerSkin = tag.getString("customer_skin");
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, provider);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        this.loadAdditional(tag, provider);
    }

}
