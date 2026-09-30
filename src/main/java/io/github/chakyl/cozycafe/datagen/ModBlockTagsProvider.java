package io.github.chakyl.cozycafe.datagen;

import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.CozyRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper exFileHelper) {
        super(output, lookupProvider, CozyCafe.MODID, exFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        this.registerMinecraftTags();
    }

    protected void registerMinecraftTags() {
        generateBlockTags(CozyRegistry.BlockRegistry.CAFE_MANAGER);
        generateBlockTagsWood(CozyRegistry.BlockRegistry.PLATING_STATION);
        generateBlockTags(CozyRegistry.BlockRegistry.CAFE_SIGN);
        generateBlockTagsWood(CozyRegistry.BlockRegistry.CAFE_SIGN);
        generateBlockTagsWood(CozyRegistry.BlockRegistry.CAFE_MENU);


    }

    public void generateBlockTags(DeferredHolder<Block, Block> block) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get());
    }

    public void generateBlockTagsWood(DeferredHolder<Block, Block> block) {
        tag(BlockTags.MINEABLE_WITH_AXE).add(block.get());
    }

    public void generateBlockTagsSoft(DeferredHolder<Block, Block> block) {
//        tag(net.minecraft.tags.BlockTags.WOO).add(block.get());
    }
}