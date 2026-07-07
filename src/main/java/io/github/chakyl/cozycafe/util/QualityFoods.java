package io.github.chakyl.cozycafe.util;

import de.cadentem.quality_food.util.QualityUtils;
import dev.latvian.mods.kubejs.stages.Stages;
import io.github.chakyl.cozycafe.CozyCafe;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class QualityFoods {
    public static double getQualityPriceIncrease(Player pPlayer, ItemStack handStack, double resolvedPrice) {
        if (QualityUtils.hasQuality(handStack)) {
            boolean doubled = false;
            if (CozyCafe.KUBEJS_INSTALLED) {
                if (Stages.get(pPlayer).has(CozyCafe.CONFIG.quality_bonus_stage.get())) {
                    doubled = true;
                }
            }
            double mult = switch (QualityUtils.getQuality(handStack)) {
                case GOLD -> doubled ? 2.0 : 1.5;
                case DIAMOND -> doubled ? 3.0 : 2.0;
                case UNDEFINED, IRON -> doubled ? 1.5 : 1.25;
                default -> 1;
            };
            resolvedPrice *= mult;
        }
        return resolvedPrice;
    }


    public static double getQualityIncrease(ItemStack handStack, double increase) {
        if (QualityUtils.hasQuality(handStack)) {
            increase += switch (QualityUtils.getQuality(handStack)) {
                case GOLD -> 4;
                case DIAMOND -> 8;
                case UNDEFINED, IRON -> 2;
                default -> 1;
            };
        }
        return increase;
    }
}
