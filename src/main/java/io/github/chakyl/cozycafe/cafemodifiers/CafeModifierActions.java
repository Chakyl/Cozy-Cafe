package io.github.chakyl.cozycafe.cafemodifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

import static io.github.chakyl.cozycafe.cafemodifiers.ModifierAction.emptyModifierAction;

public class CafeModifierActions {
    private ModifierAction tip;
    private ModifierAction price;
    private ModifierAction customerImpact;
    private ModifierAction patience;

    public CafeModifierActions(ModifierAction tip, ModifierAction price, ModifierAction customerImpact, ModifierAction patience) {
        this.tip = tip;
        this.price = price;
        this.customerImpact = customerImpact;
        this.patience = patience;
    }


    public void setTip(ModifierAction tip) {
        this.tip = tip;
    }

    public void setPrice(ModifierAction price) {
        this.price = price;
    }

    public void setCustomerImpact(ModifierAction customerImpact) {
        this.customerImpact = customerImpact;
    }

    public void setPatience(ModifierAction patience) {
        this.patience = patience;
    }

    public ModifierAction getTip() {
        return tip;
    }

    public ModifierAction getPrice() {
        return price;
    }

    public ModifierAction getCustomerImpact() {
        return customerImpact;
    }

    public ModifierAction getPatience() {
        return patience;
    }

    public static final Codec<CafeModifierActions> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ModifierAction.CODEC.optionalFieldOf("tip", emptyModifierAction()).forGetter(CafeModifierActions::getTip),
            ModifierAction.CODEC.optionalFieldOf("price", emptyModifierAction()).forGetter(CafeModifierActions::getPrice),
            ModifierAction.CODEC.optionalFieldOf("customer_impact", emptyModifierAction()).forGetter(CafeModifierActions::getCustomerImpact),
            ModifierAction.CODEC.optionalFieldOf("patience", emptyModifierAction()).forGetter(CafeModifierActions::getPatience)
    ).apply(inst, CafeModifierActions::new));


}
