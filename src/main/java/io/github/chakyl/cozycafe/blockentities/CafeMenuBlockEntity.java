package io.github.chakyl.cozycafe.blockentities;

import com.mojang.authlib.GameProfile;
import dev.latvian.mods.kubejs.stages.Stages;
import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.blocks.CafeMenuBlock;
import io.github.chakyl.cozycafe.data.CafeMenuItem;
import io.github.chakyl.cozycafe.data.CafeMenuItemRegistry;
import io.github.chakyl.cozycafe.entities.CustomerEntity;
import io.github.chakyl.cozycafe.item.ServingPlateItem;
import io.github.chakyl.cozycafe.registry.CozyRegistry;
import io.github.chakyl.cozycafe.util.PaymentUtils;
import io.github.chakyl.numismaticsutils.utils.CurioUtils;
import io.netty.util.internal.StringUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.Locale;

import static io.github.chakyl.cozycafe.tags.CozyTags.PICKLE;
import static io.github.chakyl.cozycafe.util.PaymentUtils.getCropSellMultiplier;
import static io.github.chakyl.cozycafe.util.QualityFoods.getQualityPriceIncrease;

public class CafeMenuBlockEntity extends BlockEntity {
    public static int MAX_TRAVEL_TIME = 600;
    private static int MAX_WAIT_TIME = CozyCafe.CONFIG.customerWaitTime.get();
    private static final int ORDER_TIME = CozyCafe.CONFIG.customerOrderTime.get();
    private int currentCourse = 0;
    private int waitTime = -1;
    private int orderTime = -1;
    private boolean dropItem = false;
    private boolean hasCustomer = false;
    private int customerTravelTime = -1;
    private BlockPos cafeManager;
    private ItemStack requestedItem = ItemStack.EMPTY;
    private String customerSkin = "";
    private GameProfile gameProfile;

    public CafeMenuBlockEntity(BlockPos pos, BlockState state) {
        super(CozyRegistry.BlockEntityRegistry.CAFE_MENU.get(), pos, state);
    }

    public static int getMaxWaitTime() {
        return MAX_WAIT_TIME;
    }

    public static void setMaxWaitTime(int maxWaitTime) {
        MAX_WAIT_TIME = maxWaitTime;
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (!pLevel.isClientSide()) {
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
                        this.closeMenu();
                        return;
                    } else {
                        boolean shouldClose = !this.orderedDessert(this.currentCourse);
                        if (!shouldClose) cafeManagerBlockEntity.rollMenuCourse(this);
                        this.orderTime = -1;
                        if (this.currentCourse > 1) {
                            if (this.dropItem) {
                                pLevel.addFreshEntity(new ItemEntity(pLevel, pPos.getX() + 0.5, pPos.getY() + 0.5, pPos.getZ() + 0.5, new ItemStack(Items.BOWL)));
                                this.dropItem = false;
                                this.level.setBlock(this.worldPosition, pState.setValue(CafeMenuBlock.DISH, false), 3);
                            } else {
                                this.level.setBlock(this.worldPosition, pState.setValue(CafeMenuBlock.DIRTY, true), 3);
                            }
                        } else if (currentCourse == 1 && this.dropItem) {
                            pLevel.addFreshEntity(new ItemEntity(pLevel, pPos.getX() + 0.5, pPos.getY() + 0.5, pPos.getZ() + 0.5, new ItemStack(Items.GLASS_BOTTLE)));
                            this.dropItem = false;
                        }
                        if (shouldClose) {
                            this.hasCustomer = false;
                            this.closeMenu();
                            // TODO: Make customer leave
                            this.currentCourse = 0;
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
                    if (this.waitTime == MAX_WAIT_TIME) {
                        getCafeManager(pLevel);
                        this.closeMenu();
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
        if (isMain || handStack.is(requestedItem.getItem())) {
            if (isMain) {
                boolean success = false;
                if (menuItem.bowlFood()) {
                    success = true;
                    this.dropItem = true;
                } else if (handStack.is(CozyRegistry.ItemRegistry.SERVING_PLATE.get()) && ServingPlateItem.getStoredFood(handStack).is(requestedItem.getItem())) {
                    success = true;
                } else if (handStack.is(requestedItem.getItem())) {
                    pPlayer.displayClientMessage(Component.translatable("block.cozycafe.cafe_menu.not_plated").withStyle(ChatFormatting.RED), true);
                }
                if (!success) {
                    pPlayer.playSound(SoundEvents.NOTE_BLOCK_BASS.get(), 1.0F, 1.0F);
                    return;
                }
            }
            if (menuItem.bottleDrink()) {
                this.dropItem = true;
            }
            if (!pPlayer.isCreative()) handStack.shrink(1);
            this.orderTime = 0;
            this.setCurrentCourse(this.currentCourse + 1, true);
            this.setRequestedItem(ItemStack.EMPTY);
            ((ServerLevel) level).sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    pPos.getX() + 0.5, pPos.getY() + 0.5, pPos.getZ() + 0.5,
                    5,
                    0.5, 0.5, 0.5,
                    1.0
            );
            pPlayer.level().playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.NOTE_BLOCK_CHIME.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            this.handlePayment(pPos, pPlayer, menuItem, handStack);
            CafeManagerBlockEntity cafeManagerBlockEntity = this.getCafeManager(this.level);
            if (cafeManagerBlockEntity != null) {
                cafeManagerBlockEntity.handleSuccessfulServe(menuItem, handStack, (double) MAX_WAIT_TIME / this.waitTime);
            }
            this.waitTime = -1;
            this.setChanged();
        } else {
            pPlayer.level().playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.NOTE_BLOCK_BASS.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    public void handlePayment(BlockPos pPos, Player pPlayer, CafeMenuItem cafeMenuItem, ItemStack handStack) {
        double resolvedPrice = cafeMenuItem.price();
        if (this.waitTime < MAX_WAIT_TIME) {
            // In theory this would make your mult start at 2x and then slowly reduce to 1 for service speed
            resolvedPrice *= (2 - ((double) this.waitTime / MAX_WAIT_TIME));
        }
        if (CozyCafe.KUBEJS_INSTALLED) {
            if (cafeMenuItem.item().getDefaultInstance().is(PICKLE) && Stages.get(pPlayer).has(CozyCafe.CONFIG.pickle_bonus_stage.get())) {
                resolvedPrice *= 2;
            }
        }
        if (CozyCafe.QUALITY_FOOD_INSTALLED) {
            resolvedPrice = getQualityPriceIncrease(pPlayer, handStack, resolvedPrice);
        }
        resolvedPrice *= getCropSellMultiplier(pPlayer);
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
        if (cafeManagerBlockEntity != null && cafeManagerBlockEntity.onlyHasDesserts()) {
            return true;
        }
        return Math.random() < CozyCafe.CONFIG.dessertChance.get();
    }

    public void setCurrentCourse(int currentCourse, boolean setPlate) {
        if (currentCourse < 3) {
            this.currentCourse = currentCourse;
            if (setPlate && currentCourse > 1) {
                this.level.setBlock(this.worldPosition, this.getBlockState().setValue(CafeMenuBlock.DISH, true), 3);
            }
            this.setChangedForRender();
        } else {
            this.hasCustomer = false;
            this.closeMenu();
            // TODO: Make customer leave
            this.currentCourse = 0;
        }
    }

    public void closeMenu() {
        if (this.hasCustomer) {
            CafeManagerBlockEntity cafeManagerBlockEntity = this.getCafeManager(this.level);
            if (cafeManagerBlockEntity != null) {
                cafeManagerBlockEntity.decreaseReputation(50);
            }
        }
        this.level.setBlock(this.worldPosition, this.getBlockState().setValue(CafeMenuBlock.DISH, false), 3);
        this.waitTime = -1;
        this.orderTime = -1;
        this.customerTravelTime = -1;
        this.currentCourse = 0;
        this.hasCustomer = false;
        this.customerSkin = "";
        this.gameProfile = null;
        this.setRequestedItem(ItemStack.EMPTY);
        this.setChangedForRender();
    }


    public void onCustomerArrived(PathfinderMob customer) {
        if (!this.level.isClientSide() && !this.hasCustomer) {
            if (customer instanceof CustomerEntity customerEntity) {
                customerEntity.setRemoved(Entity.RemovalReason.UNLOADED_TO_CHUNK);
            }
            CafeManagerBlockEntity cafeManagerBlockEntity = this.getCafeManager(this.level);
            if (cafeManagerBlockEntity == null || !cafeManagerBlockEntity.isOpen()) {
                this.closeMenu();
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
        return requestedItem;
    }

    public void setRequestedItem(ItemStack requestedItem) {
        this.requestedItem = requestedItem;
        this.setChangedForRender();
    }

    public boolean getHasCustomer() {
        return this.hasCustomer;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(this.worldPosition).expandTowards(0, 1.5, 0).inflate(0.5, 0, 0.5);
    }

    private void setChangedForRender() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public void setCustomerSkinFromUsername(String name) {
        if (StringUtil.isNullOrEmpty(name)) return;
        this.customerSkin = name;
        MinecraftServer server = this.level.getServer();
        if (server == null) return;

        GameProfileCache cache = server.getProfileCache();
        if (cache != null) {
            cache.getAsync(name, profileOpt -> {
                if (profileOpt != null && profileOpt.isPresent()) {
                    GameProfile profile = profileOpt.get();
                    GameProfile filledProfile = server.getSessionService().fillProfileProperties(profile, true);

                    server.execute(() -> {
                        this.gameProfile = filledProfile;
                        setChangedForRender();
                    });
                }
            });
        }
        setChangedForRender();
    }


    @Nullable
    public GameProfile getGameProfile() {
        return this.gameProfile;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("currentCourse", this.currentCourse);
        tag.putInt("waitTime", this.waitTime);
        tag.putInt("orderTime", this.orderTime);
        tag.putInt("customerTravelTime", this.customerTravelTime);
        tag.putBoolean("dropItem", this.dropItem);
        tag.putBoolean("hasCustomer", this.hasCustomer);
        if (this.cafeManager != null) {
            tag.put("cafeManager", NbtUtils.writeBlockPos(this.cafeManager));
        }

        if (!this.requestedItem.isEmpty()) {
            tag.put("requestedItem", this.requestedItem.save(new CompoundTag()));
        }
        tag.putString("customerSkin", this.customerSkin);
        if (this.gameProfile != null) {
            tag.put("customerProfile", NbtUtils.writeGameProfile(new CompoundTag(), this.gameProfile));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.currentCourse = tag.getInt("currentCourse");
        this.waitTime = tag.getInt("waitTime");
        this.orderTime = tag.getInt("orderTime");
        this.customerTravelTime = tag.getInt("customerTravelTime");
        this.hasCustomer = tag.getBoolean("hasCustomer");
        this.dropItem = tag.getBoolean("dropItem");
        if (tag.contains("cafeManager")) {
            this.cafeManager = NbtUtils.readBlockPos(tag.getCompound("cafeManager"));
        } else {
            this.cafeManager = null;
        }
        if (tag.contains("requestedItem")) {
            this.requestedItem = ItemStack.of(tag.getCompound("requestedItem"));
        } else {
            this.requestedItem = ItemStack.EMPTY;
        }
        this.customerSkin = tag.getString("customerSkin");
        if (tag.contains("customerProfile")) {
            this.gameProfile = NbtUtils.readGameProfile(tag.getCompound("customerProfile"));
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
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        this.load(tag);

    }
}
