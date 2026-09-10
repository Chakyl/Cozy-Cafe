package io.github.chakyl.cozycafe.util;

import io.github.chakyl.cozycafe.cafemodifiers.*;
import io.github.chakyl.cozycafe.data.CafeMenuItem;
import io.github.chakyl.cozycafe.data.CafeModifier;
import io.github.chakyl.cozycafe.data.CafeThemeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ModifierUtils {

    public static HashMap<String, Integer> getModifierCounts(CafeModifiers modifiers) {
        HashMap<String, Integer> themeStats = new HashMap<>();
        for (CafeModifier cafeModifier : modifiers) {
            themeStats.merge(cafeModifier.modifierId(), 1, Integer::sum);
        }
        return themeStats;
    }

    public static @NotNull List<Component> getModifierTooltips(CafeModifier modifier) {
        return getModifierTooltips(modifier,  1);
    }


    public static @NotNull List<Component> getModifierTooltips(CafeModifier modifier, int mult) {
        List<Component> tooltipList = new ArrayList<>();
        tooltipList.add(Component.translatable("gui.cozycafe.modifier.name", modifier.modifierName().plainCopy().withStyle(ChatFormatting.AQUA, ChatFormatting.UNDERLINE)));
        CafeModifierActions cafeModifierActions = modifier.modifierActions();
        if (!cafeModifierActions.getTip().isEmpty()) {
            tooltipList.add(Component.translatable("gui.cozycafe.modifier.tip", cafeModifierActions.getTip().getTextRepresentation(mult)));
        }
        if (!cafeModifierActions.getPrice().isEmpty()) {
            tooltipList.add(Component.translatable("gui.cozycafe.modifier.price", cafeModifierActions.getPrice().getTextRepresentation(mult)));
        }
        if (!cafeModifierActions.getCustomerImpact().isEmpty()) {
            tooltipList.add(Component.translatable("gui.cozycafe.modifier.customer_impact", cafeModifierActions.getCustomerImpact().getTextRepresentation(mult)));
        }
        if (!cafeModifierActions.getPatience().isEmpty()) {
            tooltipList.add(Component.translatable("gui.cozycafe.modifier.patience", cafeModifierActions.getPatience().getTextRepresentationInSeconds(mult)));
        }

        tooltipList.add(Component.translatable("gui.cozycafe.modifier.max", modifier.maxModifierCount()).withStyle(ChatFormatting.RED));
        return tooltipList;
    }

    public static Component getDecorThemes(DecorBlock decorBlock) {
        StringBuilder decorList = new StringBuilder();
        for (String decor : decorBlock.getDecorThemes()) {
            if (CafeThemeRegistry.INSTANCE.getForID(decor) != null) {
                if (!decorList.isEmpty()) {
                    decorList.append(", ");
                }
                decorList.append(CafeThemeRegistry.INSTANCE.getForID(decor).themeName().getString());
            }
        }
        if (decorList.isEmpty()) return null;
        return Component.translatable("gui.cozycafe.decor.themes", decorList.toString()).withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    public static @NotNull List<Component> getModifierFlavorsTooltips(CafeModifier modifier, boolean itemTooltip) {
        List<Component> tooltipList = new ArrayList<>();
        if (!itemTooltip) tooltipList.add(modifier.modifierName().plainCopy().withStyle(ChatFormatting.AQUA));
        if (modifier.getFlavorsImpacted().isEmpty()) {
            if (!itemTooltip)
                tooltipList.add(Component.translatable("gui.cozycafe.modifier.affects_all_flavors", modifier.maxModifierCount()).withStyle(ChatFormatting.GREEN));
        } else {
            StringBuilder flavorList = new StringBuilder();
            for (String flavor : modifier.getFlavorsImpacted()) {
                if (!flavorList.isEmpty()) {
                    flavorList.append(", ");
                }
                flavorList.append(Component.translatable("flavor.cozycafe." + flavor).getString());
            }
            tooltipList.add(Component.translatable("gui.cozycafe.modifier.affects_flavor", flavorList.toString()).withStyle(ChatFormatting.GRAY));

        }
        return tooltipList;
    }

    public static int getModifierResolvedCustomers(CafeModifier cafeModifier, int customers) {
        ModifierAction customerAction = cafeModifier.modifierActions().getCustomerImpact();
        if (!customerAction.isEmpty()) {
            if (customerAction.action() == NumberAction.ADD) {
                customers += (int) customerAction.value();
            } else {
                customers = (int) Math.floor(customers * customerAction.value());
            }
        }
        return customers;
    }

    public static int getModifierResolvedWaitTimes(CafeModifier cafeModifier, int waitTime) {
        ModifierAction patience = cafeModifier.modifierActions().getPatience();
        if (!patience.isEmpty()) {
            if (patience.action() == NumberAction.ADD) {
                waitTime += (int) patience.value();
            } else {
                waitTime = (int) Math.floor(waitTime * patience.value());
            }
        }
        return waitTime;
    }

    public static int getModifierResolvedPrice(CafeMenuItem menuItem, Boolean tipPhase, CafeModifier cafeModifier, int resolvedPrice) {
        if (cafeModifier.canAffectDish(menuItem)) {
            if (!tipPhase) {
                ModifierAction priceAction = cafeModifier.modifierActions().getPrice();
                if (!priceAction.isEmpty()) {
                    if (priceAction.action() == NumberAction.ADD) {
                        resolvedPrice += (int) priceAction.value();
                    } else {
                        resolvedPrice = (int) Math.floor(resolvedPrice * priceAction.value());
                    }
                }
            } else {
                ModifierAction tipAction = cafeModifier.modifierActions().getTip();
                if (!tipAction.isEmpty()) {
                    if (tipAction.action() == NumberAction.ADD) {
                        resolvedPrice += (int) tipAction.value();
                    } else {
                        resolvedPrice = (int) Math.floor(resolvedPrice * tipAction.value());
                    }
                }
            }
        }
        return resolvedPrice;
    }
}
