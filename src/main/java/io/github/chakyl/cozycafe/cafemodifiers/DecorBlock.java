package io.github.chakyl.cozycafe.cafemodifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.data.CafeModifier;
import io.github.chakyl.cozycafe.data.CafeModifierRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.util.List;

public class DecorBlock {
    int maxUsages;
    List<String> decorThemes;
    List<String> modifiers;
    Component decorName;
    CafeModifiers dynamicModifiers;
    CafeModifiers allModifiers;

    public DecorBlock(int maxUsages, List<String> decorThemes, Component decorName, List<String> modifiers, CafeModifiers dynamicModifiers) {
        this.maxUsages = maxUsages;
        this.decorThemes = decorThemes;
        this.decorName = decorName;
        this.modifiers = modifiers;
        this.dynamicModifiers = dynamicModifiers;
    }

    public List<String> getDecorThemes() {
        return decorThemes;
    }

    public void setDecorThemes(List<String> decorThemes) {
        this.decorThemes = decorThemes;
    }

    public int getMaxUsages() {
        return maxUsages;
    }

    public void setMaxUsages(int maxUsages) {
        this.maxUsages = maxUsages;
    }

    public void gatherModifiers() {
        CafeModifiers newModifiers = new CafeModifiers();
        for (String modifier : modifiers) {
            CafeModifier newModifier = CafeModifierRegistry.INSTANCE.getForID(modifier);
            if (newModifier != null) newModifiers.add(newModifier);
            else {
                CozyCafe.LOGGER.warn("[Cozy Cafe] Decor asked for modifier " + modifier + " that doesn't exist...");
            }
        }
        allModifiers = newModifiers;
        allModifiers.addAll(dynamicModifiers);
    }

    public CafeModifiers getCafeModifiers() {
        if ((this.allModifiers == null || allModifiers.isEmpty()) && (!dynamicModifiers.isEmpty() || !modifiers.isEmpty()))
            gatherModifiers();
        return allModifiers;
    }

    public List<String> getCafeModifierIds() {
        return modifiers;
    }


    public Component getDecorName() {
        return decorName;
    }

    public void setDecorName(Component decorName) {
        this.decorName = decorName;
    }

    public CafeModifiers getDynamicModifiers() {
        return dynamicModifiers;
    }

    public static final Codec<DecorBlock> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.optionalFieldOf("max_usages", 3).forGetter(DecorBlock::getMaxUsages),
            Codec.STRING.listOf().optionalFieldOf("decor_themes", List.of()).forGetter(DecorBlock::getDecorThemes),
            ComponentSerialization.CODEC.optionalFieldOf("decor_name", Component.empty()).forGetter(DecorBlock::getDecorName),
            Codec.STRING.listOf().optionalFieldOf("modifiers", List.of()).forGetter(DecorBlock::getCafeModifierIds),
            CafeModifiers.CODEC.optionalFieldOf("dynamic_modifiers", new CafeModifiers()).forGetter(DecorBlock::getDynamicModifiers)
    ).apply(inst, DecorBlock::new));
}
