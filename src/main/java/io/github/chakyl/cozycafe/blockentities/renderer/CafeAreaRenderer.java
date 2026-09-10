package io.github.chakyl.cozycafe.blockentities.renderer;


import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.client.VolumeRendererOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = CozyCafe.MODID, value = Dist.CLIENT)
public class CafeAreaRenderer {

    private static final Map<BlockPos, AABB> ACTIVE_AREAS = new ConcurrentHashMap<>();

    public static void addBox(BlockPos pos, AABB box) {
        ACTIVE_AREAS.put(pos, box);
    }

    public static void removeBox(BlockPos pos) {
        ACTIVE_AREAS.remove(pos);
    }

    @SubscribeEvent
    public static void onRenderLevelStageEvent(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        var mc = Minecraft.getInstance();
        if (mc.player == null) return;
        for (AABB box : ACTIVE_AREAS.values()) {
            VolumeRendererOverlay.onRenderLevel(event.getCamera(), box);
        }
    }
}