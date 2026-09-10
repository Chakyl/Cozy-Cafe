package io.github.chakyl.cozycafe.data;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

import java.util.*;

import static io.github.chakyl.cozycafe.util.FoodClassificationUtils.dropsBottle;
import static io.github.chakyl.cozycafe.util.FoodClassificationUtils.dropsBowl;

/**
 * @param item
 * @param category
 * @param price
 * @param bowlFood
 * @param bottleDrink
 * @param themes
 * @param flavors
 */
public record CafeMenuItem(Item item, MenuItemCategory category, String multAttribute, int price, boolean bowlFood,
                           Item bowl, boolean bottleDrink, Item bottle,
                           List<String> themes,
                           List<String> flavors) implements AbstractCafeMenuItem {
    public static final Codec<CafeMenuItem> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(CafeMenuItem::item),
                    Codec.STRING.optionalFieldOf("category", ",main").xmap(
                            s -> switch (s.toLowerCase()) {
                                case "dessert" -> MenuItemCategory.DESSERT;
                                case "drink" -> MenuItemCategory.DRINK;
                                default -> MenuItemCategory.MAIN;
                            },
                            menuItemCategory -> switch (menuItemCategory) {
                                case DESSERT -> "dessert";
                                case DRINK -> "drink";
                                default -> "main";
                            }
                    ).forGetter(CafeMenuItem::category),
                    Codec.STRING.optionalFieldOf("mult_attribute", "").forGetter(CafeMenuItem::multAttribute),
                    Codec.intRange(1, Integer.MAX_VALUE).fieldOf("price").orElse(-1).forGetter(CafeMenuItem::price),
                    Codec.BOOL.optionalFieldOf("bowl_food").forGetter(CMI -> Optional.of(CMI.bowlFood())),
                    BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("bowl", Items.BOWL).forGetter(CafeMenuItem::bowl),
                    Codec.BOOL.optionalFieldOf("bottle_drink").forGetter(CMI -> Optional.of(CMI.bottleDrink())),
                    BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("bottle", Items.GLASS_BOTTLE).forGetter(CafeMenuItem::bottle),
                    Codec.STRING.listOf().optionalFieldOf("themes").forGetter(CMI -> CMI.themes().isEmpty() ? Optional.empty() : Optional.of(CMI.themes())),
                    Codec.STRING.listOf().optionalFieldOf("flavors").forGetter(CMI -> CMI.flavors().isEmpty() ? Optional.empty() : Optional.of(CMI.flavors()))
            )
            .apply(inst, (item, category, multAttribute, price, bowlFood, bowl, bottleDrink, bottle, themes, flavors) -> {
                ItemStack defaultInstance = item.getDefaultInstance();
                int resolvedPrice = price == -1 ? getGeneratedPrice(defaultInstance) : price;
                boolean resolvedBottleDrink = bottleDrink.orElseGet(() -> category == MenuItemCategory.DRINK && dropsBottle(defaultInstance));
                boolean resolvedBowlFood = bowlFood.orElseGet(() -> dropsBowl(defaultInstance));
                List<String> resolvedThemes = addTagBasedThemes(defaultInstance, themes.orElse(List.of()));
                List<String> resolvedFlavors = addTagBasedFlavors(defaultInstance, flavors.orElse(List.of()));
                return new CafeMenuItem(item, category, multAttribute, resolvedPrice, resolvedBowlFood, bowl, resolvedBottleDrink, bottle, resolvedThemes, resolvedFlavors);
            }));


    public CafeMenuItem(CafeMenuItem other) {
        this(other.item, other.category, other.multAttribute, other.price, other.bowlFood, other.bowl, other.bottleDrink, other.bottle, other.themes, other.flavors);
    }


    public CafeMenuItem validate(ResourceLocation key) {
        Preconditions.checkNotNull(this.item, "Invalid item ID!");
        Preconditions.checkNotNull(this.category, "Invalid category!");
        return this;
    }

    private static List<String> addTagBasedFlavors(ItemStack item, List<String> flavors) {
        Set<String> newFlavors = new LinkedHashSet<>(flavors);
        if (item.is(Tags.Items.FOODS_COOKED_MEAT)) {
            newFlavors.add("meat");
            newFlavors.add("hot");
        }
        if (item.is(Tags.Items.FOODS_COOKED_FISH)) {
            newFlavors.add("seafood");
            newFlavors.add("hot");
        }
        if (item.is(Tags.Items.FOODS_BREAD)) {
            newFlavors.add("bread");
        }
        if (item.is(Tags.Items.FOODS_GOLDEN)) {
            newFlavors.add("metallic");
        }
        if (item.is(Tags.Items.FOODS_SOUP)) {
            newFlavors.add("hot");
            newFlavors.add("cold");
        }
        if (item.is(Tags.Items.FOODS_VEGETABLE)) {
            newFlavors.add("vegan");
        }
        if (item.is(Tags.Items.FOODS_PIE)) {
            newFlavors.add("sweet");
            newFlavors.add("hot");
        }
        if (item.is(Tags.Items.FOODS_CANDY)) {
            newFlavors.add("sweet");
        }
        if (item.is(Tags.Items.FOODS_BERRY) || item.is(Tags.Items.FOODS_FRUIT) || item.is(Tags.Items.DRINKS_JUICE)) {
            newFlavors.add("fruity");
        }
        return new ArrayList<>(newFlavors);
    }

    private static List<String> addTagBasedThemes(ItemStack item, List<String> themes) {
        Set<String> newThemes = new LinkedHashSet<>(themes);
        if (item.is(Tags.Items.FOODS_COOKED_MEAT) || item.is(Tags.Items.FOODS_COOKED_FISH) || item.is(Tags.Items.FOODS_GOLDEN)) {
            newThemes.add("fancy");
        }
        if (item.is(Tags.Items.FOODS_BREAD) || item.is(Tags.Items.FOODS_PIE)) {
            newThemes.add("bakery");
            newThemes.add("casual");
        }
        if ( item.is(Tags.Items.FOODS_PIE)) {
            newThemes.add("diner");
        }
        if (item.is(Tags.Items.FOODS_CANDY)) {
            newThemes.add("fast_food");
        }
        return new ArrayList<>(newThemes);
    }

    private static int getGeneratedPrice(ItemStack food) {
        FoodProperties foodProperties = food.get(DataComponents.FOOD);
        if (foodProperties != null) {
            return Math.max(1, Math.round(foodProperties.nutrition() + (foodProperties.saturation() * 3)));
        }
        return 1;
    }

    @Override
    public Codec<? extends CafeMenuItem> getCodec() {
        return CODEC;
    }

    public enum MenuItemCategory {
        DRINK, MAIN, DESSERT
    }

}