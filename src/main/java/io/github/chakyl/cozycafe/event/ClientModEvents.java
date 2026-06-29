package io.github.chakyl.cozycafe.event;

import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.blockentities.renderer.CafeMenuBlockEntityRenderer;
import io.github.chakyl.cozycafe.blockentities.renderer.PlatingStationBlockEntityRenderer;
import io.github.chakyl.cozycafe.client.model.ServingPlateItemOverrides;
import io.github.chakyl.cozycafe.entities.renderer.CustomerRenderer;
import io.github.chakyl.cozycafe.gui.CafeManagerScreen;
import io.github.chakyl.cozycafe.gui.MenuSelectorScreen;
import io.github.chakyl.cozycafe.registry.CozyRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT, modid = CozyCafe.MODID)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(CozyRegistry.MenuRegistry.CAFE_MANAGER.get(), CafeManagerScreen::new);
            MenuScreens.register(CozyRegistry.MenuRegistry.MENU_SELECTOR.get(), MenuSelectorScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CozyRegistry.BlockEntityRegistry.CAFE_MENU.get(), CafeMenuBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(CozyRegistry.BlockEntityRegistry.PLATING_STATION.get(), PlatingStationBlockEntityRenderer::new);
        event.registerEntityRenderer(CozyRegistry.EntityRegistry.CUSTOMER.get(), CustomerRenderer::new);
    }

    @SubscribeEvent
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        ModelResourceLocation plateLocation = new ModelResourceLocation("cozycafe", "serving_plate", "inventory");
        BakedModel originalPlateModel = event.getModels().get(plateLocation);

        if (originalPlateModel != null) {
            event.getModels().put(plateLocation, new BakedModelWrapper(originalPlateModel) {
                private final ItemOverrides overrides = new ServingPlateItemOverrides(originalPlateModel);

                @Override
                public ItemOverrides getOverrides() {
                    return overrides;
                }
            });
        }
    }
}
