package io.github.chakyl.cozycafe.cafemodifiers;

import com.mojang.serialization.Codec;
import io.github.chakyl.cozycafe.data.CafeModifier;

import java.util.ArrayList;
import java.util.List;

public class CafeModifiers extends ArrayList<CafeModifier> {

    public static final Codec<CafeModifiers> CODEC = CafeModifier.CODEC.listOf().xmap(
            CafeModifiers::new,
            list -> list
    );

    public CafeModifiers() {
    }

    public CafeModifiers(List<CafeModifier> list) {
        super(list);
    }
}
