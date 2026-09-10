package io.github.chakyl.cozycafe;

import io.github.chakyl.cozycafe.cafemodifiers.DecorBlock;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import static io.github.chakyl.cozycafe.CozyCafe.loc;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;


public class DataMapRegistry {
    public static final DataMapType<Block, DecorBlock> DECOR = DataMapType.builder(
            loc("cafe_decor"),
            Registries.BLOCK,
            DecorBlock.CODEC
    ).build();
}