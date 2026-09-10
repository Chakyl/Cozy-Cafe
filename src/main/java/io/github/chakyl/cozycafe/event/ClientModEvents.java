package io.github.chakyl.cozycafe.event;

import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.CozyRegistry;
import io.github.chakyl.cozycafe.blockentities.renderer.CafeMenuBlockEntityRenderer;
import io.github.chakyl.cozycafe.blockentities.renderer.PlatingStationBlockEntityRenderer;
import io.github.chakyl.cozycafe.client.SkinCache;
import io.github.chakyl.cozycafe.client.model.ServingPlateItemOverrides;
import io.github.chakyl.cozycafe.entities.renderer.CustomerRenderer;
import io.github.chakyl.cozycafe.gui.CafeManagerScreen;
import io.github.chakyl.cozycafe.gui.CafeStatsScreen;
import io.github.chakyl.cozycafe.gui.MenuSelectorScreen;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;

import static io.github.chakyl.cozycafe.CozyCafe.loc;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, modid = CozyCafe.MODID)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(CozyRegistry.MenuRegistry.CAFE_MANAGER.value(), CafeManagerScreen::new);
        event.register(CozyRegistry.MenuRegistry.MENU_SELECTOR.value(), MenuSelectorScreen::new);
        event.register(CozyRegistry.MenuRegistry.CAFE_STATS.value(), CafeStatsScreen::new);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CozyRegistry.BlockEntityRegistry.CAFE_MENU.value(), CafeMenuBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(CozyRegistry.BlockEntityRegistry.PLATING_STATION.get(), PlatingStationBlockEntityRenderer::new);
        event.registerEntityRenderer(CozyRegistry.EntityRegistry.CUSTOMER.get(), CustomerRenderer::new);
    }

    @SubscribeEvent
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        ModelResourceLocation plateLocation = new ModelResourceLocation(loc("serving_plate"), "inventory");
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

    @SubscribeEvent
    public static void onClientConnect(ClientPlayerNetworkEvent.LoggingIn event) {
        SkinCache.preloadSkins();
    }
}
