package io.github.chakyl.cozycafe.datagen;

import io.github.chakyl.cozycafe.CozyRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ModLootTableProvider extends BlockLootSubProvider {
    public ModLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(CozyRegistry.BlockRegistry.CAFE_MANAGER.get());
        dropSelf(CozyRegistry.BlockRegistry.CAFE_MENU.get());
        dropSelf(CozyRegistry.BlockRegistry.PLATING_STATION.get());
        dropSelf(CozyRegistry.BlockRegistry.CAFE_SIGN.get());

    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        List<Block> list = new ArrayList<>();
        list.add(CozyRegistry.BlockRegistry.CAFE_MENU.get());
        list.add(CozyRegistry.BlockRegistry.CAFE_SIGN.get());
        list.add(CozyRegistry.BlockRegistry.CAFE_MANAGER.get());
        list.add(CozyRegistry.BlockRegistry.PLATING_STATION.get());
        return list::iterator;
    }
}