package io.github.chakyl.cozycafe.tags;

import io.github.chakyl.cozycafe.CozyCafe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class CozyTags {
    public static final TagKey<Block> CLEANS_DISHES = blockTag("cleans_dishes");

    public static TagKey<Block> blockTag(String name) {
        return BlockTags.create(ResourceLocation.fromNamespaceAndPath(CozyCafe.MODID, name));
    }
}
