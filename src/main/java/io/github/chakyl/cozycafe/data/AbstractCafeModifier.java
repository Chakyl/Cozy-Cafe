package io.github.chakyl.cozycafe.data;

import dev.shadowsoffire.placebo.codec.CodecProvider;
import io.github.chakyl.cozycafe.cafemodifiers.CafeModifierActions;
import io.github.chakyl.cozycafe.cafemodifiers.ModifierType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import java.util.List;

public sealed interface AbstractCafeModifier extends CodecProvider<CafeModifier> permits CafeModifier  {
    String modifierId();

    Component modifierName();

    Item modifierIcon();

    ModifierType type();

    List<String> flavorsImpacted();

    int maxModifierCount();

    CafeModifierActions modifierActions();

}
