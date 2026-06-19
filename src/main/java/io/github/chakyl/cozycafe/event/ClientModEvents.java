package io.github.chakyl.cozycafe.event;

import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.blockentities.renderer.CafeMenuBlockEntityRenderer;
import io.github.chakyl.cozycafe.blockentities.renderer.PlatingStationBlockEntityRenderer;
import io.github.chakyl.cozycafe.entities.renderer.CustomerRenderer;
import io.github.chakyl.cozycafe.gui.CafeManagerScreen;
import io.github.chakyl.cozycafe.gui.MenuSelectorScreen;
import io.github.chakyl.cozycafe.item.ServingPlateItem;
import io.github.chakyl.cozycafe.registry.CozyRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
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
    public static void registerItemDecorators(RegisterItemDecorationsEvent event) {
        event.register(CozyRegistry.ItemRegistry.SERVING_PLATE.get(), (guiGraphics, font, stack, xOffset, yOffset) -> {
            ItemStack food = ServingPlateItem.getStoredFood(stack);
            if (!food.isEmpty()) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(xOffset + 2, yOffset + 2, 100);
                guiGraphics.pose().scale(0.8f, 0.8f, 0.8f);
                guiGraphics.renderItem(food, 0, 0);
                guiGraphics.pose().popPose();
                return true;
            }
            return false;
        });
    }
}
