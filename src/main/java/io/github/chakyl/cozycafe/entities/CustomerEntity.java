package io.github.chakyl.cozycafe.entities;

import io.github.chakyl.cozycafe.blockentities.CafeMenuBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

public class CustomerEntity extends PathfinderMob {
    public CustomerEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new NavigateToMenuGoal(this, 0.9D, 24));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.7D));
    }

    public static class NavigateToMenuGoal extends MoveToBlockGoal {
        private final PathfinderMob mob;

        public NavigateToMenuGoal(PathfinderMob mob, double speed, int searchRange) {
            super(mob, speed, searchRange);
            this.mob = mob;
        }

        @Override
        protected boolean isValidTarget(LevelReader level, BlockPos pos) {
            return level.getBlockEntity(pos) instanceof CafeMenuBlockEntity cafeMenuBlockEntity && !cafeMenuBlockEntity.getHasCustomer();
        }

        @Override
        public boolean canUse() {
            if (this.nextStartTick > 0) {
                this.nextStartTick--;
                return false;
            } else {
                this.nextStartTick = 80;
            }
            for (BlockPos pos : BlockPos.betweenClosed(this.mob.blockPosition().offset(-24, -4, -24), this.mob.blockPosition().offset(24, 4, 24))) {
                if (this.mob.level().isLoaded(pos) && this.isValidTarget(this.mob.level(), pos)) {
                    this.blockPos = pos.immutable();
                    return true;
                }
            }

            return false;
        }

        @Override
        public void tick() {
            super.tick();
            if (this.isReachedTarget()) {
                if (this.mob.level().getBlockEntity(this.blockPos) instanceof CafeMenuBlockEntity cafeMenuBlockEntity) {
                    cafeMenuBlockEntity.onCustomerArrived(this.mob);
                    this.mob.getNavigation().stop();
                }
            }
        }

        @Override
        public double acceptedDistance() {
            return 2.0D;
        }
    }
}