package io.github.chakyl.cozycafe.datagen;

import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.registry.CozyRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
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


    }

    public void generateBlockTags(RegistryObject<Block> block) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get());
    }

    public void generateBlockTagsWood(RegistryObject<Block> block) {
        tag(BlockTags.MINEABLE_WITH_AXE).add(block.get());
    }

    public void generateBlockTagsSoft(RegistryObject<Block> block) {
//        tag(net.minecraft.tags.BlockTags.WOO).add(block.get());
    }
}