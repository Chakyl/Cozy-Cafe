package io.github.chakyl.cozycafe.item;

import io.github.chakyl.cozycafe.registry.CozyRegistry;
import io.github.chakyl.cozycafe.tags.CozyTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class DirtyServingPlateItem extends Item {
    private static final int CLEAN_DURATION = 30;

    public DirtyServingPlateItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BRUSH;
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        Player player = pContext.getPlayer();
        if (player != null && (level.getBlockState(pos).is(CozyTags.CLEANS_DISHES))) {
            if (player.distanceToSqr(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f) <= 2.0f) {
                player.startUsingItem(pContext.getHand());
                return InteractionResult.CONSUME;
            }
        }


        return InteractionResult.PASS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int count) {
        if (!(livingEntity instanceof Player player)) return;

        int useDuration = this.getUseDuration(stack) - count;
        if (useDuration % 5 == 0 && level.isClientSide) {
            HitResult hitResult = player.pick(5.0D, 0.0F, false);
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
                BlockState state = level.getBlockState(pos);
                if (state.is(CozyTags.CLEANS_DISHES)) {
                    for (int i = 0; i < 3; i++) {
                        level.addParticle(ParticleTypes.SPLASH,
                                pos.getX() + level.random.nextFloat(),
                                pos.getY() + 1.1D,
                                pos.getZ() + level.random.nextFloat(),
                                0.0D, 0.0D, 0.0D);
                    }
                }
            }
        }
        if (useDuration % 10 == 0) {
            level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BRUSH_SAND, SoundSource.PLAYERS, 1.0F, 1.2F);
            level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BOAT_PADDLE_WATER, SoundSource.PLAYERS, 0.8F, 1.2F);
            if (level.random.nextFloat() < 0.5f)
                level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.WATER_AMBIENT, SoundSource.PLAYERS, 0.8F, 1.2F);
        }
        if (useDuration >= CLEAN_DURATION) {
            level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_STEP, SoundSource.PLAYERS, 0.4F, 1.7F);
            if (!level.isClientSide) {
                stack.shrink(1);
                ItemStack cleanPlate = new ItemStack(CozyRegistry.ItemRegistry.SERVING_PLATE.get());
                if (!player.getInventory().add(cleanPlate)) player.drop(cleanPlate, false);
                player.stopUsingItem();
            }
        }
    }
}