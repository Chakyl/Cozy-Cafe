package io.github.chakyl.cozycafe;

import io.github.chakyl.cozycafe.data.CafeMenuItemRegistry;
import io.github.chakyl.cozycafe.registry.CozyRegistry;
import io.github.chakyl.cozycafe.util.PaymentUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CozyCafe.MODID)
public class CozyCafe {
    public static final String MODID = "cozycafe";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    private static final ForgeConfigSpec.Builder CONFIG_BUILDER = new ForgeConfigSpec.Builder();
    public static final CozyConfig CONFIG = new CozyConfig(CONFIG_BUILDER);
    public static boolean QUALITY_FOOD_INSTALLED = false;
    public static boolean KUBEJS_INSTALLED = false;
    public static boolean NUMISMATICS_INSTALLED = false;
    public static boolean NUMISMATICS_UTILS_INSTALLED = false;
    public static boolean EMI_INSTALLED = false;

    public CozyCafe() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.register(this);
        CozyRegistry.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CONFIG_BUILDER.build());
        modEventBus.addListener(this::onConfigLoadOrReload);
    }

    @SubscribeEvent
    public void setup(FMLCommonSetupEvent e) {
        CafeMenuItemRegistry.INSTANCE.registerToBus();
    }

    private void onConfigLoadOrReload(final ModConfigEvent event) {
        PaymentUtils.invalidateCoinCache();
    }
}