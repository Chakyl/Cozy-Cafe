package io.github.chakyl.cozycafe.blockentities.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.chakyl.cozycafe.blockentities.CafeMenuBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;

public class CafeMenuBlockEntityRenderer implements BlockEntityRenderer<CafeMenuBlockEntity> {
    private final ItemRenderer itemRenderer;

    public CafeMenuBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(CafeMenuBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.getRequestedItem().isEmpty()) {
            poseStack.pushPose();
            float ticks = blockEntity.getLevel().getGameTime() + partialTick;
            float yFloating = (float) Math.sin(ticks * 0.06f) * 0.05f;
            poseStack.translate(0.5, 1.0 + yFloating, 0.5);
            EntityRenderDispatcher renderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            poseStack.mulPose(Axis.YP.rotationDegrees(-renderDispatcher.camera.getYRot()));
            // TODO: Scale off config option
            if (blockEntity.getWaitTime() >= 200) {
                float wiggleStage = (blockEntity.getWaitTime() - 200) / 100.0f;
                float zTilt = (float) Math.sin(ticks * (0.2f + (wiggleStage))) * (wiggleStage * 0.8f);
                poseStack.mulPose(Axis.ZP.rotationDegrees(zTilt));
            }
            poseStack.scale(1.5f, 1.5f, 1.5f);
            this.itemRenderer.renderStatic(
                    blockEntity.getRequestedItem(),
                    ItemDisplayContext.GROUND,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    bufferSource,
                    blockEntity.getLevel(),
                    (int) blockEntity.getBlockPos().asLong()
            );
            poseStack.popPose();
        }
    }
}