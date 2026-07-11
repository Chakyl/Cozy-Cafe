package io.github.chakyl.cozycafe.event;

import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.data.CafeMenuItem;
import io.github.chakyl.cozycafe.data.CafeMenuItemRegistry;
import io.github.chakyl.cozycafe.tags.CozyTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = CozyCafe.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {
    // Janky code that makes sure menu items have tags in EMI
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        Map<TagKey<Item>, List<Holder<Item>>> tagsToAdd = new HashMap<>();
        for (CafeMenuItem menuItem : CafeMenuItemRegistry.INSTANCE.getValues()) {
            BuiltInRegistries.ITEM.getResourceKey(menuItem.item())
                    .flatMap(BuiltInRegistries.ITEM::getHolder)
                    .ifPresent(holder -> {
                        if (holder.is(CozyTags.NOT_SERVED)) return;
                        tagsToAdd.computeIfAbsent(CozyTags.MENU_ITEM, k -> new ArrayList<>()).add(holder);
                        switch (menuItem.category()) {
                            case DRINK -> tagsToAdd.computeIfAbsent(CozyTags.DRINK, k -> new ArrayList<>()).add(holder);
                            case DESSERT ->
                                    tagsToAdd.computeIfAbsent(CozyTags.DESSERT, k -> new ArrayList<>()).add(holder);
                            case MAIN -> tagsToAdd.computeIfAbsent(CozyTags.MAIN, k -> new ArrayList<>()).add(holder);
                        }
                    });
        }

        tagsToAdd.forEach((tagKey, newHolders) -> {
            HolderSet.Named<Item> namedTag = BuiltInRegistries.ITEM.getOrCreateTag(tagKey);
            List<Holder<Item>> combined = new ArrayList<>(namedTag.stream().toList());
            for (Holder<Item> holder : newHolders) {
                if (!combined.contains(holder)) {
                    combined.add(holder);
                }
            }
            namedTag.bind(combined);
        });
    }
}
