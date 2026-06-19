package io.github.chakyl.cozycafe.blocks;

import io.github.chakyl.cozycafe.blockentities.PlatingStationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class PlatingStationBlock extends Block implements EntityBlock {
    public PlatingStationBlock(Properties properties) {
        super(properties);
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PlatingStationBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof PlatingStationBlockEntity platingStationBlockEntity) {
            InteractionResult interactionResult = platingStationBlockEntity.handlePlating(level, player, hand);
            if (interactionResult != null) return interactionResult;
        }
        return InteractionResult.PASS;
    }
}