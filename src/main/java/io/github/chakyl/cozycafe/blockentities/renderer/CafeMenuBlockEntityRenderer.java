package io.github.chakyl.cozycafe.blockentities.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.blockentities.CafeMenuBlockEntity;
import io.github.chakyl.cozycafe.blocks.CafeMenuBlock;
import io.github.chakyl.cozycafe.client.SkinCache;
import io.github.chakyl.cozycafe.data.CafeMenuItemRegistry;
import io.github.chakyl.cozycafe.util.GeneralUtils;
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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;


public class CafeMenuBlockEntityRenderer implements BlockEntityRenderer<CafeMenuBlockEntity> {
    private static final int WAIT_TIME = CozyCafe.CONFIG.customerWaitTime.get();
    private final ItemRenderer itemRenderer;

    private final PlayerModel<?> wideModel;
    private final PlayerModel<?> slimModel;

    public CafeMenuBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();

        this.wideModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false);
        this.slimModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
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

            final float foodSize = 1f;

            if (blockEntity.getCurrentCourse() == 1 + 1) {
                // Main
                poseStack.translate(0.5f, 0.1f, 0.5f);
                poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
                poseStack.scale(foodSize, foodSize, foodSize);
            } else  {
                // Drink
                poseStack.translate(
                        0.45f + (facing.getStepX() * 0.35f) + (facing.getClockWise().getStepX() * 0.3f),
                        0f,
                        0.45f + (facing.getStepZ() * 0.35f) + (facing.getClockWise().getStepZ() * 0.3f)
                );

                poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot() - 20.0f));

                if (!(blockEntity.getEatingItem().getItem() instanceof BlockItem)) {
                    poseStack.translate(0f, 0.25f, 0f);
                    poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f));
                }

                poseStack.scale(foodSize * 0.65f, foodSize *  0.65f, foodSize *  0.65f);
            }


            GeneralUtils.renderFood(
                    CafeMenuItemRegistry.INSTANCE.getForItem(blockEntity.getEatingItem().getItem()),
                    poseStack,
                    bufferSource,
                    blockEntity,
                    packedLight,
                    packedOverlay
            );

            poseStack.popPose();
        }
        if (blockEntity.getHasCustomer()) {
            poseStack.pushPose();
            String targetUser = blockEntity.getCustomerSkin();
            SkinCache.SkinData skinData = SkinCache.getSkin(targetUser);

            PlayerModel<?> playerModel = skinData.isSlim() ? slimModel : wideModel;

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

            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(skinData.texture()));
            playerModel.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, 0xFFFFFFFF);
            poseStack.popPose();
        }
    }
}