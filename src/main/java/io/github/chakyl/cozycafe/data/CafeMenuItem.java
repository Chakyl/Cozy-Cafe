package io.github.chakyl.cozycafe.data;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;


/**
 * @param item
 * @param category
 * @param price
 * @param themes
 * @param flavors
 */
public record CafeMenuItem(Item item, MenuItemCategory category, int price, List<String> themes,
                           List<String> flavors) implements CodecProvider<CafeMenuItem> {
    public enum MenuItemCategory {
        DRINK, MAIN, DESSERT
    }
    public static final Codec<CafeMenuItem> CODEC = new CafeMenuItemCodec();

    public CafeMenuItem(CafeMenuItem other) {
        this(other.item, other.category, other.price, other.themes, other.flavors);
    }


    public CafeMenuItem validate(ResourceLocation key) {
        Preconditions.checkNotNull(this.item, "Invalid item ID!");
        Preconditions.checkNotNull(this.category, "Invalid category!");
        return this;
    }

    @Override
    public Codec<? extends CafeMenuItem> getCodec() {
        return CODEC;
    }

    public static class CafeMenuItemCodec implements Codec<CafeMenuItem> {

        @Override
        public <T> DataResult<T> encode(CafeMenuItem input, DynamicOps<T> ops, T prefix) {
            JsonObject obj = new JsonObject();

            obj.addProperty("item", BuiltInRegistries.ITEM.getKey(input.item).toString());
            obj.addProperty("category", switch (input.category) {
                case MAIN -> "main";
                case DESSERT -> "dessert";
                case DRINK -> "drink";
            });
            obj.addProperty("price", input.price);
            JsonArray themes = new JsonArray();
            obj.add("themes", themes);
            for (String theme : input.themes) {
                themes.add(theme.replace("\"", ""));
            }
            JsonArray flavors = new JsonArray();
            obj.add("flavors", flavors);
            for (String flavor : input.flavors) {
                flavors.add(flavor.replace("\"", ""));
            }
            return DataResult.success(JsonOps.INSTANCE.convertTo(ops, obj));
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public <T> DataResult<Pair<CafeMenuItem, T>> decode(DynamicOps<T> ops, T input) {
            JsonObject obj = ops.convertTo(JsonOps.INSTANCE, input).getAsJsonObject();

            Item food = Items.AIR;
            if (obj.has("item")) {
                food = BuiltInRegistries.ITEM.get(new ResourceLocation(GsonHelper.getAsString(obj,"item")));
            }
            MenuItemCategory menuItemCategory = MenuItemCategory.MAIN;
            if (obj.has("category")) {
                menuItemCategory = switch (GsonHelper.getAsString(obj, "category")) {
                    case "drink" -> MenuItemCategory.DRINK;
                    case "desert", "dessert" -> MenuItemCategory.DESSERT;
                    default -> MenuItemCategory.MAIN;
                };
            }
            int price = 1;
            if (obj.has("price")) {
                price = (GsonHelper.getAsInt(obj, "price"));
            }
            List<String> themes = new ArrayList<>();
            if (obj.has("themes")) {
                for (JsonElement json : GsonHelper.getAsJsonArray(obj, "themes")) {
                    themes.add(String.valueOf(json).replace("\"", ""));
                }
            }
            List<String> flavors = new ArrayList<>();
            if (obj.has("flavors")) {
                for (JsonElement json : GsonHelper.getAsJsonArray(obj, "flavors")) {
                    flavors.add(String.valueOf(json).replace("\"", ""));
                }
            }
            return DataResult.success(Pair.of(new CafeMenuItem(food, menuItemCategory, price, themes, flavors), input));
        }

    }

}

