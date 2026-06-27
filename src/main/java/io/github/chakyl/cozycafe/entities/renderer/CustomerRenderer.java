package io.github.chakyl.cozycafe.entities.renderer;

import com.mojang.authlib.GameProfile;
import io.github.chakyl.cozycafe.entities.CustomerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;

public class CustomerRenderer extends MobRenderer<CustomerEntity, PlayerModel<CustomerEntity>> {

    public CustomerRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
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
}