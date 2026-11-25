package io.github.chakyl.cozycafe.blockentities.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.chakyl.cozycafe.CozyRegistry;
import io.github.chakyl.cozycafe.blockentities.PlatingStationBlockEntity;
import io.github.chakyl.cozycafe.data.CafeMenuItem;
import io.github.chakyl.cozycafe.item.ServingPlateItem;
import io.github.chakyl.cozycafe.util.GeneralUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class PlatingStationBlockEntityRenderer implements BlockEntityRenderer<PlatingStationBlockEntity> {
    private final ItemRenderer itemRenderer;

    public PlatingStationBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    private void renderPlate(ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.65f, 0.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.scale(1.0f, 1.0f, 1.0f);
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, poseStack, bufferSource, null, 0);
        poseStack.popPose();
    }


    @Override
    public void render(PlatingStationBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        ItemStack stationItem = pBlockEntity.getPlateItem();
        if (stationItem == null || stationItem.isEmpty()) return;
        if (!ServingPlateItem.getStoredFood(stationItem).isEmpty()) {
            renderPlate(new ItemStack(CozyRegistry.ItemRegistry.SERVING_PLATE.get()), pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
            CafeMenuItem plateInTheFoodOfEatTheHotFoodOut = ServingPlateItem.getCafeMenuItem(stationItem);
            if (plateInTheFoodOfEatTheHotFoodOut != null) {
                pPoseStack.pushPose();
                pPoseStack.translate(0.5D, 1.11D, 0.5D); // Adjust for the block height

                GeneralUtils.renderFood(plateInTheFoodOfEatTheHotFoodOut, pPoseStack, pBuffer, pBlockEntity, pPackedLight, pPackedOverlay);

                pPoseStack.popPose();
            }
        } else if (stationItem.is(CozyRegistry.ItemRegistry.SERVING_PLATE.get())) {
            renderPlate(stationItem, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        }
    }
}