package io.github.chakyl.cozycafe.data;

import dev.shadowsoffire.placebo.codec.CodecProvider;
import net.minecraft.network.chat.Component;

public sealed interface AbstractCafeTheme extends CodecProvider<CafeTheme> permits CafeTheme {

    Component themeName();

    CafeModifier modifier();

    int minDecorItems();

}