package io.github.chakyl.cozycafe.data;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import io.github.chakyl.cozycafe.CozyCafe;
import io.github.chakyl.cozycafe.tags.CozyTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static io.github.chakyl.cozycafe.CozyCafe.loc;
import static io.github.chakyl.cozycafe.util.FoodClassificationUtils.dropsBottle;
import static io.github.chakyl.cozycafe.util.FoodClassificationUtils.dropsBowl;

public class CafeThemeRegistry extends DynamicRegistry<CafeTheme> {
    public static final CafeThemeRegistry INSTANCE = new CafeThemeRegistry();
    private Map<String, CafeTheme> themesByID = new HashMap<>();

    public CafeThemeRegistry() {
        super(CozyCafe.LOGGER, "theme", true, false);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(loc("theme"), CafeTheme.CODEC);
    }

    @Override
    protected void beginReload(ReloadType type) {
        super.beginReload(type);
        this.themesByID = new HashMap<>();
    }

    @Override
    protected void onReload(ReloadType type) {
        super.onReload(type);
        this.themesByID = ImmutableMap.copyOf(this.themesByID);
    }


    @Override
    public Map<ResourceLocation, JsonElement> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        return super.prepare(pResourceManager, pProfiler);
    }

    public CafeTheme getForID(String id) {
        if (id == null) return null;
        return this.themesByID.get(id);
    }

    @Override
    protected void validateItem(ResourceLocation key, CafeTheme theme) {
        theme.validate(key);
        if (this.themesByID.containsKey(theme.themeId())) {
            String msg = "Attempted to register two themes (%s and %s) with the same id: %s!";
            throw new UnsupportedOperationException(String.format(msg, key, this.getKey(this.themesByID.get(theme.themeId())), theme.themeId()));
        }
        this.themesByID.put(theme.themeId(), theme);
    }

}