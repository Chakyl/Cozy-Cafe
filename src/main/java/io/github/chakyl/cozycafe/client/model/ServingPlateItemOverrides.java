package io.github.chakyl.cozycafe.client.model;

import io.github.chakyl.cozycafe.item.ServingPlateItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ServingPlateItemOverrides extends ItemOverrides {
    private final BakedModel plateModel;

    public ServingPlateItemOverrides(BakedModel plateModel) {
        this.plateModel = plateModel;
    }

    @Nullable
    @Override
    public BakedModel resolve(BakedModel pModel, ItemStack pStack, @Nullable ClientLevel pLevel, @Nullable LivingEntity pEntity, int pSeed) {
        ItemStack food = ServingPlateItem.getStoredFood(pStack);
        if (!food.isEmpty()) {
            return new ServingPlateModel(plateModel, food);
        }
        return plateModel;
    }
}