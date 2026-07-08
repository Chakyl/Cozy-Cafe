package io.github.chakyl.cozycafe.blockentities.renderer;

import com.mojang.authlib.GameProfile;
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
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;


public class CafeMenuBlockEntityRenderer implements BlockEntityRenderer<CafeMenuBlockEntity> {
    private static final int WAIT_TIME = CozyCafe.CONFIG.customerWaitTime.get();
    private final ItemRenderer itemRenderer;
    private final PlayerModel<?> playerModel;

    public CafeMenuBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
        this.playerModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false);
    }

    @Override
    public void render(CafeMenuBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        float ticks = blockEntity.getLevel().getGameTime() + partialTick;
        if (!blockEntity.getRequestedItem().isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5, 1.0 + (float) Math.sin(ticks * 0.06f) * 0.05f, 0.5);
            EntityRenderDispatcher renderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            poseStack.mulPose(Axis.YP.rotationDegrees(-renderDispatcher.camera.getYRot()));

            float progress = WAIT_TIME > 0 ? (float) blockEntity.getWaitTime() / WAIT_TIME : 0.0f;
            if (progress >= 0.5f) {
                float speedMultiplier = 1.0f;
                float amplitude = 4.0f;

                if (progress >= 0.85f) {
                    speedMultiplier = 3.5f;
                    amplitude = 12.0f;
                } else if (progress >= 0.7f) {
                    speedMultiplier = 2.2f;
                    amplitude = 8.0f;
                }
                poseStack.mulPose(Axis.ZP.rotationDegrees((float) Math.sin(ticks * 0.2f * speedMultiplier) * amplitude));
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
        } else if (!blockEntity.getEatingItem().isEmpty()) {
            poseStack.pushPose();
            Direction facing = blockEntity.getBlockState().getValue(CafeMenuBlock.FACING).getOpposite();
            final float foodSize = 1.25f;
            if (blockEntity.getCurrentCourse() == 1 + 1) {
                poseStack.translate(0.5f, 0.15f, 0.5f);
                poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
                poseStack.scale(foodSize, foodSize, foodSize);
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.translate(0.0f, -0.1f, 0.0f);
            } else if (blockEntity.getCurrentCourse() == 0 + 1) {
                poseStack.translate(0.5f + (facing.getStepX() * 0.35f) + (facing.getClockWise().getStepX() * 0.3f), 0.4f, 0.5f + (facing.getStepZ() * 0.35f) + (facing.getClockWise().getStepZ() * 0.3f));
                poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot() - 20.0f));
                poseStack.scale(foodSize, foodSize, foodSize);
                poseStack.translate(0.0f, -0.22f, 0.0f);
            } else {
                // TODO: figure out wtf to do here
                poseStack.translate(0.5f + (facing.getStepX() * 0.35f) + (facing.getClockWise().getStepX() * 0.3f), 0.4f, 0.5f + (facing.getStepZ() * 0.35f) + (facing.getClockWise().getStepZ() * 0.3f));
                poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot() - 20.0f));
                poseStack.scale(foodSize, foodSize, foodSize);
                poseStack.translate(0.0f, -0.22f, 0.0f);
            }
            this.itemRenderer.renderStatic(
                    blockEntity.getEatingItem(),
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

            GameProfile profile = blockEntity.getGameProfile();
            ResourceLocation textureLocation = DefaultPlayerSkin.getDefaultSkin();
            if (profile != null) {
                textureLocation = Minecraft.getInstance().getSkinManager().getInsecureSkinLocation(profile);
            }

            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(textureLocation));
            playerModel.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
            poseStack.popPose();
        }
    }
}