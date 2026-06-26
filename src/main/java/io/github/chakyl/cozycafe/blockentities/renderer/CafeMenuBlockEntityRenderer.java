package io.github.chakyl.cozycafe.blockentities.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.blockentities.CafeMenuBlockEntity;
import io.github.chakyl.cozycafe.blocks.CafeMenuBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;


public class CafeMenuBlockEntityRenderer implements BlockEntityRenderer<CafeMenuBlockEntity> {
    private static final int WAIT_TIME = CozyCafe.CONFIG.customerWaitTime.get();
    private final ItemRenderer itemRenderer;
    private final PlayerModel<?> playerModel;
    // TODO: Match blockentityData
    private static final ResourceLocation PLAYER_TEXTURE = new ResourceLocation("minecraft", "textures/entity/player/wide/steve.png");

    public CafeMenuBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
        this.playerModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false);
    }

    @Override
    public void render(CafeMenuBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        float ticks = blockEntity.getLevel().getGameTime() + partialTick;
        if (!blockEntity.getRequestedItem().isEmpty()) {
            poseStack.pushPose();
            float yFloating = (float) Math.sin(ticks * 0.06f) * 0.05f;
            poseStack.translate(0.5, 1.0 + yFloating, 0.5);
            EntityRenderDispatcher renderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            poseStack.mulPose(Axis.YP.rotationDegrees(-renderDispatcher.camera.getYRot()));

            if (blockEntity.getWaitTime() >= WAIT_TIME) {
                float wiggleStage = (blockEntity.getWaitTime() - WAIT_TIME) / 100.0f;
                poseStack.mulPose(Axis.ZP.rotationDegrees((float) Math.sin(ticks * (0.2f + (wiggleStage))) * (wiggleStage * 0.8f)));
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

        if (blockEntity.getHasCustomer()) {
            poseStack.pushPose();
            Direction facing = blockEntity.getBlockState().getValue(CafeMenuBlock.FACING);
            poseStack.translate(0.5F + facing.getStepX(), 1.7F, 0.5F + facing.getStepZ());
            poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));

            float modelScale = 1.85F;
            poseStack.scale(modelScale, modelScale, modelScale);
            poseStack.scale(-1.0F, -1.0F, 1.0F);

            // idk why the head is so big without this
            playerModel.head.xScale = 0.7F;
            playerModel.head.yScale = 0.7F;
            playerModel.head.zScale = 0.7F;

            playerModel.rightLeg.xRot = -1.5F;
            playerModel.rightLeg.yRot = 0.3F;
            playerModel.leftLeg.xRot = -1.5F;
            playerModel.leftLeg.yRot = -0.3F;

            float sway = (float) Math.cos(ticks * 0.09f) * 0.05f;
            playerModel.rightArm.xRot = -0.5f + sway;
            playerModel.rightArm.zRot = 0.05f + (sway * 0.2f);
            playerModel.leftArm.xRot = -0.5f + sway;
            playerModel.leftArm.zRot = -0.05f - (sway * 0.2f);

            playerModel.jacket.copyFrom(playerModel.body);
            playerModel.rightPants.copyFrom(playerModel.rightLeg);
            playerModel.leftPants.copyFrom(playerModel.leftLeg);
            playerModel.rightSleeve.copyFrom(playerModel.rightArm);
            playerModel.leftSleeve.copyFrom(playerModel.leftArm);

            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(PLAYER_TEXTURE));
            playerModel.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
            poseStack.popPose();
        }
    }
}