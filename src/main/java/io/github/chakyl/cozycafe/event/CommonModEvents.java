package io.github.chakyl.cozycafe.event;

import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.entities.CustomerEntity;
import io.github.chakyl.cozycafe.network.EvilPacketsIHateThem;
import io.github.chakyl.cozycafe.registry.CozyRegistry;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = CozyCafe.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonModEvents {
    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> EvilPacketsIHateThem.register());
        event.enqueueWork(() -> CozyCafe.QUALITY_FOOD_INSTALLED = ModList.get().isLoaded("quality_food"));
        event.enqueueWork(() -> CozyCafe.KUBEJS_INSTALLED = ModList.get().isLoaded("kubejs"));
        event.enqueueWork(() -> CozyCafe.NUMISMATICS_INSTALLED = ModList.get().isLoaded("numismatics"));
        event.enqueueWork(() -> CozyCafe.NUMISMATICS_UTILS_INSTALLED = ModList.get().isLoaded("numismatics_utils"));
        event.enqueueWork(() -> CozyCafe.EMI_INSTALLED = ModList.get().isLoaded("emi"));

    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(CozyRegistry.EntityRegistry.CUSTOMER.get(), CustomerEntity.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0D).add(Attributes.MOVEMENT_SPEED, 0.25D).add(Attributes.FOLLOW_RANGE, 32.0D).build());
    }
}