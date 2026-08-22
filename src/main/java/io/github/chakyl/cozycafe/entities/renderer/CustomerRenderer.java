package io.github.chakyl.cozycafe.entities.renderer;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.chakyl.cozycafe.entities.CustomerEntity;
import io.github.chakyl.cozycafe.util.GeneralUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;

public class CustomerRenderer extends MobRenderer<CustomerEntity, PlayerModel<CustomerEntity>> {

    private final PlayerModel<CustomerEntity> slimModel;
    private final PlayerModel<CustomerEntity> wideModel;

    public CustomerRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);

        this.wideModel = this.model;
        this.slimModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
    }

    @Override
    public ResourceLocation getTextureLocation(CustomerEntity entity) {
        GameProfile profile = entity.getOrCreateProfile();

        if (profile != null) {
            SkinManager skinManager = Minecraft.getInstance().getSkinManager();
            return skinManager.getInsecureSkinLocation(profile);
        }

        return DefaultPlayerSkin.getDefaultSkin();
    }

    @Override
    public void render(CustomerEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        this.model = GeneralUtils.isSlimSkin(pEntity.getOrCreateProfile()) ? this.slimModel : this.wideModel;

        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }
}