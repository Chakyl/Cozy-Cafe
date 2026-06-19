package io.github.chakyl.cozycafe.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.QuadTransformers;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.data.ModelData;
import com.mojang.math.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ServingPlateModel implements IDynamicBakedModel {
    private final BakedModel plateModel;
    private final ItemStack food;

    public ServingPlateModel(BakedModel plateModel, ItemStack food) {
        this.plateModel = plateModel;
        this.food = food;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
        List<BakedQuad> quads = new ArrayList<>(plateModel.getQuads(state, side, rand, extraData, renderType));
        if (food != null && !food.isEmpty()) {
            BakedModel foodModel = Minecraft.getInstance().getItemRenderer().getModel(food, null, null, 0);
            if (!(foodModel instanceof ServingPlateModel)) {
                PoseStack poseStack = new PoseStack();
                poseStack.translate(0.5, 3f/16.0f, 0.5);
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
                poseStack.translate(-0.5, -0.5, -0.5);
                IQuadTransformer transformer = QuadTransformers.applying(new Transformation(poseStack.last().pose()));
                for (BakedQuad quad : foodModel.getQuads(state, side, rand, extraData, renderType)) {
                    quads.add(transformer.process(quad));
                }
            }
        }
        return quads;
    }

    @Override
    public boolean useAmbientOcclusion() { return plateModel.useAmbientOcclusion(); }

    @Override
    public boolean isGui3d() { return plateModel.isGui3d(); }

    @Override
    public boolean usesBlockLight() { return plateModel.usesBlockLight(); }

    @Override
    public boolean isCustomRenderer() { return false; }

    @Override
    public TextureAtlasSprite getParticleIcon() { return plateModel.getParticleIcon(); }

    @Override
    public ItemTransforms getTransforms() { return plateModel.getTransforms(); }

    @Override
    public ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }
}