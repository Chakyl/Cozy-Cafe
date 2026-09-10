package io.github.chakyl.cozycafe.cafemodifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public record ModifierAction(double value, NumberAction action) {
    private static final DecimalFormat FORMAT = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    public static double multMult(double x, int times) {
        double result = x;
        for (int i = 1; i < times; i++) {
            result *= x;
        }
        return result;
    }

    public String getTextRepresentation() {
        return getTextRepresentation(1);
    }

    public String getTextRepresentation(int mult) {
        return (this.action() == NumberAction.ADD ? ((this.value * mult) > 0 ? "+" : "") : "x") + (this.action() == NumberAction.ADD ?  FORMAT.format(this.value * mult) : FORMAT.format(multMult(this.value, mult)));
    }

    public String getTextRepresentationInSeconds() {
        return getTextRepresentationInSeconds(1);
    }

    public String getTextRepresentationInSeconds(int mult) {
        String output = (this.action() == NumberAction.ADD ? (((this.value * mult) / 20) > 0 ? "+" : "") : "x") + (this.action() == NumberAction.ADD ? FORMAT.format((this.value * mult) / 20) : FORMAT.format(multMult(this.value, mult)));
        return output;
    }

    public static ModifierAction emptyModifierAction() {
        return new ModifierAction(0, NumberAction.ADD);
    }

    public boolean isEmpty() {
        return this.value == 0 && this.action == NumberAction.ADD;
    }

    public static final Codec<ModifierAction> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("value").forGetter(ModifierAction::value),
                    NumberAction.CODEC.fieldOf("action").forGetter(ModifierAction::action)
            ).apply(instance, ModifierAction::new)
    );

    public static final StreamCodec<ByteBuf, ModifierAction> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, ModifierAction::value,
            NumberAction.STREAM_CODEC, ModifierAction::action,
            ModifierAction::new
    );

}