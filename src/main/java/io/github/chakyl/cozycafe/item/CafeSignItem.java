package io.github.chakyl.cozycafe.item;

import io.github.chakyl.cozycafe.blockentities.CafeManagerBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CafeSignItem extends BlockItem {
    public CafeSignItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockEntity clickedBlockEntity = level.getBlockEntity(clickedPos);

        if (clickedBlockEntity instanceof CafeManagerBlockEntity) {
            if (!level.isClientSide) {
                context.getItemInHand().getOrCreateTag().put("linkedManager", NbtUtils.writeBlockPos(clickedPos));

                if (context.getPlayer() != null) {
                    context.getPlayer().sendSystemMessage( Component.translatable("item.cozycafe.cafe_sign.linked").withStyle(ChatFormatting.GREEN));
                    if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                        serverPlayer.containerMenu.broadcastChanges();
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.useOn(context);
    }
}