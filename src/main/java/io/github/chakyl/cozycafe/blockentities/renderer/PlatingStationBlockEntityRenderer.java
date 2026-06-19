package io.github.chakyl.cozycafe.blockentities.renderer;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.chakyl.cozycafe.blockentities.PlatingStationBlockEntity;
import io.github.chakyl.cozycafe.item.ServingPlateItem;
import io.github.chakyl.cozycafe.registry.CozyRegistry;
import net.minecraft.client.Minecraft;
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

    private void renderItem(ItemStack stack, float yOffset, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, yOffset, 0.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(1.0f, 1.0f, 1.0f);
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, poseStack, bufferSource, null, 0);
        poseStack.popPose();
    }

    @Override
    public void render(PlatingStationBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        ItemStack stationItem = entity.getPlateItem();
        if (stationItem == null || stationItem.isEmpty()) return;
        if (!ServingPlateItem.getStoredFood(stationItem).isEmpty()) {
            renderItem(new ItemStack(CozyRegistry.ItemRegistry.SERVING_PLATE.get()), 1.05f, poseStack, bufferSource, combinedLight, combinedOverlay);
            ItemStack plateInTheFoodOfEatTheHotFoodOut = ServingPlateItem.getStoredFood(stationItem);
            if (!plateInTheFoodOfEatTheHotFoodOut.isEmpty()) {
                renderItem(plateInTheFoodOfEatTheHotFoodOut, 1.125f, poseStack, bufferSource, combinedLight, combinedOverlay);
            }
        } else if (stationItem.is(CozyRegistry.ItemRegistry.SERVING_PLATE.get())) {
            renderItem(stationItem, 1.05f, poseStack, bufferSource, combinedLight, combinedOverlay);
        }
    }
}