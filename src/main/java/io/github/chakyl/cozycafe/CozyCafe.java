package io.github.chakyl.cozycafe;

import com.mojang.logging.LogUtils;
import dev.shadowsoffire.placebo.tabs.TabFillingRegistry;
import io.github.chakyl.cozycafe.data.CafeMenuItemRegistry;
import io.github.chakyl.cozycafe.registry.CozyRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CozyCafe.MODID)
public class CozyCafe {
    public static final String MODID = "cozycafe";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public CozyCafe() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.register(this);
        CozyRegistry.register();
    }
    @SubscribeEvent
    public void setup(FMLCommonSetupEvent e) {
        CafeMenuItemRegistry.INSTANCE.registerToBus();
    }

}