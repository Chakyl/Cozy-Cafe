package io.github.chakyl.cozycafe.data;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.chakyl.cozycafe.cafemodifiers.CafeModifierActions;
import io.github.chakyl.cozycafe.cafemodifiers.ModifierType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 *
 * @param modifierId
 * @param modifierName
 * @param modifierIcon
 * @param type
 * @param flavorsImpacted
 * @param maxModifierCount
 * @param modifierActions
 */
public record CafeModifier(String modifierId, Component modifierName, Item modifierIcon, ModifierType type, List<String> flavorsImpacted, int maxModifierCount, CafeModifierActions modifierActions) implements AbstractCafeModifier {
    public static final Codec<CafeModifier> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("modifier_id").forGetter(CafeModifier::modifierId),
            ComponentSerialization.CODEC.optionalFieldOf("modifier_name", Component.empty()).forGetter(CafeModifier::modifierName),
            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("modifier_icon", Items.AIR).forGetter(CafeModifier::modifierIcon),
            Codec.STRING.optionalFieldOf("type", "decor").xmap(
                    s -> switch (s.toLowerCase()) {
                        case "thene" -> ModifierType.THEME;
                        default -> ModifierType.DECOR;
                    },
                    modifierType -> switch (modifierType) {
                        case THEME -> "theme";
                        default -> "decor";
                    }
            ).forGetter(CafeModifier::type),
            Codec.STRING.listOf().optionalFieldOf("flavors_impacted", List.of()).forGetter(CafeModifier::getFlavorsImpacted),
            Codec.intRange(1, 64).fieldOf("max_modifier_count").orElse(1).forGetter(CafeModifier::maxModifierCount),
            CafeModifierActions.CODEC.fieldOf("modifier_actions").forGetter(CafeModifier::modifierActions)
    ).apply(inst, CafeModifier::new));

    public CafeModifier(CafeModifier other) {
        this(other.modifierId, other.modifierName, other.modifierIcon, other.type, other.flavorsImpacted, other.maxModifierCount, other.modifierActions);
    }

    public List<String> getFlavorsImpacted() {
        return this.type == ModifierType.DECOR ? flavorsImpacted : List.of();
    }

    public boolean canAffectDish(CafeMenuItem menuItem) {
        if (this.flavorsImpacted.isEmpty()) return true;
        else return this.flavorsImpacted.stream().anyMatch(menuItem.flavors()::contains);
    }

    @Override
    public Codec<? extends CafeModifier> getCodec() {
        return CODEC;
    }

    public CafeModifier validate(ResourceLocation key) {
        Preconditions.checkNotNull(this.modifierId, "Missing modifier ID!");
        Preconditions.checkNotNull(this.modifierName, "Invalid modifier name!");
        Preconditions.checkNotNull(this.modifierActions, "Missing modifier actions!");
        return this;
    }
}
