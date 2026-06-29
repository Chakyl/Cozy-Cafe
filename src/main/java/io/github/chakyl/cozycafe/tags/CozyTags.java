package io.github.chakyl.cozycafe.tags;

import io.github.chakyl.cozycafe.CozyCafe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class CozyTags {
    public static final TagKey<Block> CLEANS_DISHES = blockTag("cleans_dishes");
    public static final TagKey<Item> PICKLE = itemTag("pickle");

    public static TagKey<Block> blockTag(String name) {
        return BlockTags.create(ResourceLocation.fromNamespaceAndPath(CozyCafe.MODID, name));
    }

    public static TagKey<Item> itemTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath(CozyCafe.MODID, name));
    }
}
