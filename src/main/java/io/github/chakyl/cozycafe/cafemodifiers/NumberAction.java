package io.github.chakyl.cozycafe.cafemodifiers;


import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.BinaryOperator;
import java.util.function.IntFunction;

public enum NumberAction implements StringRepresentable {
    ADD("add", (a, b) -> a + b),
    MULTIPLY("multiply", (a, b) -> a * b);

    private final String name;
    private final BinaryOperator<Double> action;

    NumberAction(String name, BinaryOperator<Double> action) {
        this.name = name;
        this.action = action;
    }

    public double apply(double current, double value) {
        return this.action.apply(current, value);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public static final Codec<NumberAction> CODEC = StringRepresentable.fromEnum(NumberAction::values);

    private static final IntFunction<NumberAction> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);

    public static final StreamCodec<ByteBuf, NumberAction> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
}
