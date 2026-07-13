package io.github.chakyl.cozycafe.data;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.tags.CozyTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static io.github.chakyl.cozycafe.util.FoodClassificationUtils.dropsBottle;
import static io.github.chakyl.cozycafe.util.FoodClassificationUtils.dropsBowl;

public class CafeMenuItemRegistry extends DynamicRegistry<CafeMenuItem> {

    public static final CafeMenuItemRegistry INSTANCE = new CafeMenuItemRegistry();
    private Map<String, CafeMenuItem> menuItemsByID = new HashMap<>();

    public CafeMenuItemRegistry() {
        super(CozyCafe.LOGGER, "menu", true, false);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(new ResourceLocation(CozyCafe.MODID, "menu"), CafeMenuItem.CODEC);
    }

    @Override
    protected void beginReload() {
        super.beginReload();
        this.menuItemsByID = new HashMap<>();
    }

    @Override
    protected void onReload() {
        super.onReload();
        this.menuItemsByID = ImmutableMap.copyOf(this.menuItemsByID);
    }

    private static final String[] DENIED_KEYWORDS = {"raw", "rotten", "poisonous", "spider_eye"};

    @Override
    public Map<ResourceLocation, JsonElement> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        if (!CozyCafe.CONFIG.dynamicMenuItems.get()) return super.prepare(pResourceManager, pProfiler);
        Map<ResourceLocation, JsonElement> loadedResources = new HashMap<>(super.prepare(pResourceManager, pProfiler));
        CozyCafe.LOGGER.info("[COZYCAFE] Beginning Dynamic Menu Items");
        /**
         * Autogeneration is denied in these cirucmstatnces:
         * - Config turned off
         * - Isn't edible
         * - has bad word
         * - Is in the cozycafe:not_served tag
         */
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR || !item.isEdible()) continue;
            if (item.getDefaultInstance().is(CozyTags.NOT_SERVED)) continue;
            ResourceLocation itemRL = BuiltInRegistries.ITEM.getKey(item);
            String itemPath = itemRL.getPath();
            if (Arrays.stream(DENIED_KEYWORDS).anyMatch(itemPath::contains)) continue;
            ResourceLocation virtualRegistryKey = new ResourceLocation(CozyCafe.MODID, itemRL.getNamespace() + "/" + itemPath);
            if (loadedResources.containsKey(virtualRegistryKey)) continue;
            String menuItemCategory = determineCategoryByTagsKeywords(itemPath);
            JsonObject virtualJson = new JsonObject();
            virtualJson.addProperty("item", itemRL.toString());
            virtualJson.addProperty("category", menuItemCategory);
            if (menuItemCategory.equals("main") && dropsBowl(item.getDefaultInstance())) {
                virtualJson.addProperty("bowl_food", true);
            }
            if (menuItemCategory.equals("drink") && dropsBottle(item.getDefaultInstance())) {
                virtualJson.addProperty("bottle_drink", true);
            }
            virtualJson.addProperty("mult_attribute", "");
            virtualJson.add("themes", new JsonArray());
            virtualJson.add("flavors", new JsonArray());

            loadedResources.put(virtualRegistryKey, virtualJson);
        }
        CozyCafe.LOGGER.info("[COZYCAFE] Dynamic Menu Registration complete");
        return loadedResources;
    }

    private static final String[] DRINK_KEYWORDS = {"drink", "juice", "soda", "tea", "coffee", "smoothie", "cider", "milkshake", "beer", "wine"};

    private static final String[] DESSERT_KEYWORDS = {"dessert", "cake", "cookie", "pie", "donut", "icecream", "ice_cream", "pastry", "sweet", "candy", "chocolate", "pudding"};

    private String determineCategoryByTagsKeywords(String path) {
        if (Arrays.stream(DRINK_KEYWORDS).anyMatch(path::contains)) return "drink";
        if (Arrays.stream(DESSERT_KEYWORDS).anyMatch(path::contains)) return "dessert";
        return "main";
    }

    public boolean isMenuItem(Item item) {
        if (item == null) return false;
        return this.menuItemsByID.get(BuiltInRegistries.ITEM.getKey(item).toString()) != null;
    }

    public CafeMenuItem getForItem(Item item) {
        if (item == null) return null;
        return this.menuItemsByID.get(BuiltInRegistries.ITEM.getKey(item).toString());
    }

    @Override
    protected void validateItem(ResourceLocation key, CafeMenuItem menuItem) {
        menuItem.validate(key);
        String itemId = BuiltInRegistries.ITEM.getKey(menuItem.item()).toString();
        if (this.menuItemsByID.containsKey(itemId)) {
            String msg = "Attempted to register two menu items (%s and %s) with the same id: %s!";
            throw new UnsupportedOperationException(String.format(msg, key, this.getKey(this.menuItemsByID.get(itemId)), itemId));
        }
        this.menuItemsByID.put(itemId, menuItem);
    }

}