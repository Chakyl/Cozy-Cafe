package io.github.chakyl.cozycafe.util;

import io.github.chakyl.cozycafe.CozyCafe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PaymentUtils {
    private static final Map<Integer, Supplier<String>> COIN_CONFIG_MAP = new LinkedHashMap<>();
    private static final ResourceLocation CROP_MULTIPLIER_KEY = new ResourceLocation("shippingbin", "crop_sell_multiplier");
    private static Map<Item, Integer> cachedValidCoinMap = null;

    static {
        COIN_CONFIG_MAP.put(4096, CozyCafe.CONFIG.currency_4096::get);
        COIN_CONFIG_MAP.put(512, CozyCafe.CONFIG.currency_512::get);
        COIN_CONFIG_MAP.put(64, CozyCafe.CONFIG.currency_64::get);
        COIN_CONFIG_MAP.put(16, CozyCafe.CONFIG.currency_16::get);
        COIN_CONFIG_MAP.put(8, CozyCafe.CONFIG.currency_8::get);
        COIN_CONFIG_MAP.put(1, CozyCafe.CONFIG.currency_1::get);
    }

    public static void invalidateCoinCache() {
        cachedValidCoinMap = null;
    }

    private static Map<Item, Integer> getValidCoinMap() {
        if (cachedValidCoinMap != null) return cachedValidCoinMap;

        cachedValidCoinMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, Supplier<String>> entry : COIN_CONFIG_MAP.entrySet()) {
            String itemRegistryName = entry.getValue().get();

            if (itemRegistryName != null && !itemRegistryName.trim().isEmpty()) {
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemRegistryName));
                if (item != null && item != net.minecraft.world.item.Items.AIR) {
                    cachedValidCoinMap.put(item, entry.getKey());
                }
            }
        }
        return cachedValidCoinMap;
    }

    /**
     * global.coinMap = [
     * { coin: "numismatics:sun", value: 4096 },
     * { coin: "numismatics:crown", value: 512 },
     * { coin: "numismatics:cog", value: 64 },
     * { coin: "numismatics:sprocket", value: 16 },
     * { coin: "numismatics:bevel", value: 8 },
     * { coin: "numismatics:spur", value: 1 },
     * ];
     * // From SSV
     * // Returns array of coins from price, prioritizing high value coins
     * const calculateCoinsFromValue = (price) => {
     * let output = []
     * for (let i = 0; i < coinMap.length; i++) {
     * let { coin, value } = coinMap[i];
     * if (value <= price) {
     * if (price % value === 0) {
     * output.push({ coin: coin, count: price / value });
     * return output;
     * } else {
     * output.push({ coin: coin, count: Math.floor(price / value) });
     * calculateCoinsFromValue(price % value, output, coinMap);
     * }
     * return output;
     * }
     * }
     * };
     */
    public static List<ItemStack> getPaymentItems(int payment) {
        List<ItemStack> output = new ArrayList<>();
        if (payment <= 0) return output;

        Map<Item, Integer> validCoinMap = getValidCoinMap();
        int remainingPrice = payment;
        for (Map.Entry<Item, Integer> entry : validCoinMap.entrySet()) {
            Item coinItem = entry.getKey();
            int coinValue = entry.getValue();
            if (coinValue <= remainingPrice) {
                int count = remainingPrice / coinValue;

                while (count > 0) {
                    int stackSize = Math.min(count, coinItem.getMaxStackSize(ItemStack.EMPTY));
                    output.add(new ItemStack(coinItem, stackSize));
                    count -= stackSize;
                }
                remainingPrice %= coinValue;
                if (remainingPrice == 0) {
                    break;
                }
            }
        }
        return output;
    }

    // SSV specific code
    public static double getCropSellMultiplier(Player player) {
        if (player == null) return 1.0;
        Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(CROP_MULTIPLIER_KEY);
        if (attribute != null) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) return instance.getValue();
        }
        return 1.0;
    }
}
