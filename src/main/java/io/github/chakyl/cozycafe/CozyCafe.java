package io.github.chakyl.cozycafe;

import io.github.chakyl.cozycafe.cafemodifiers.CafeModifiers;
import io.github.chakyl.cozycafe.data.CafeMenuItemRegistry;
import io.github.chakyl.cozycafe.data.CafeModifierRegistry;
import io.github.chakyl.cozycafe.data.CafeThemeRegistry;
import io.github.chakyl.cozycafe.util.PaymentUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CozyCafe.MODID)
public class CozyCafe {
    public static final String MODID = "cozycafe";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    private static final ModConfigSpec.Builder CONFIG_BUILDER = new ModConfigSpec.Builder();
    public static final CozyConfig CONFIG = new CozyConfig(CONFIG_BUILDER);
    public static boolean QUALITY_FOOD_INSTALLED = false;
    public static boolean KUBEJS_INSTALLED = false;
    public static boolean NUMISMATICS_INSTALLED = false;
    public static boolean NUMISMATICS_UTILS_INSTALLED = false;
    public static boolean EMI_INSTALLED = false;

    public CozyCafe(ModContainer container) {
        IEventBus bus = container.getEventBus();
        bus.register(this);
        CozyRegistry.register(bus);
        container.registerConfig(ModConfig.Type.COMMON, CONFIG_BUILDER.build());

    }

    @SubscribeEvent
    public void setup(FMLCommonSetupEvent e) {
        CafeMenuItemRegistry.INSTANCE.registerToBus();
        CafeModifierRegistry.INSTANCE.registerToBus();
        CafeThemeRegistry.INSTANCE.registerToBus();
    }

    public static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private void onConfigLoadOrReload(final ModConfigEvent event) {
        PaymentUtils.invalidateCoinCache();
    }
}