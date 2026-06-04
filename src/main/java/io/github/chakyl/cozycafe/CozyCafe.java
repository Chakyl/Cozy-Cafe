package io.github.chakyl.cozycafe;

import com.mojang.logging.LogUtils;
import io.github.chakyl.cozycafe.registry.CozyRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CozyCafe.MODID)
public class CozyCafe {
    public static final String MODID = "cozycafe";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CozyCafe() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        CozyRegistry.register();

    }
}