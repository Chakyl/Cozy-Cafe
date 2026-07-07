package io.github.chakyl.cozycafe.blockentities.renderer;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.chakyl.cozycafe.blockentities.PlatingStationBlockEntity;
import io.github.chakyl.cozycafe.item.ServingPlateItem;
import io.github.chakyl.cozycafe.registry.CozyRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

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

    private void renderFoodItem(ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 1.19f, 0.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
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
            ItemStack plateInTheFoodOfEatTheHotFoodOut = ServingPlateItem.getStoredFood(stationItem);
            if (!plateInTheFoodOfEatTheHotFoodOut.isEmpty()) {
                renderFoodItem(plateInTheFoodOfEatTheHotFoodOut, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
            }
        } else if (stationItem.is(CozyRegistry.ItemRegistry.SERVING_PLATE.get())) {
            renderPlate(stationItem, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        }
    }
}