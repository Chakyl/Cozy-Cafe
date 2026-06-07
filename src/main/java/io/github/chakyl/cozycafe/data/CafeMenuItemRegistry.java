package io.github.chakyl.cozycafe.data;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import io.github.chakyl.cozycafe.CozyCafe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

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

    @Override
    public Map<ResourceLocation, JsonElement> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        return super.prepare(pResourceManager, pProfiler);
    }

}