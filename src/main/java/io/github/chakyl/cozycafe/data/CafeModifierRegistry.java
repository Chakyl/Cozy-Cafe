package io.github.chakyl.cozycafe.data;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import io.github.chakyl.cozycafe.CozyCafe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

import static io.github.chakyl.cozycafe.CozyCafe.loc;

public class CafeModifierRegistry extends DynamicRegistry<CafeModifier> {
    public static final CafeModifierRegistry INSTANCE = new CafeModifierRegistry();
    private Map<String, CafeModifier> modifiersByID = new HashMap<>();

    public CafeModifierRegistry() {
        super(CozyCafe.LOGGER, "modifier", true, false);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(loc("modifier"), CafeModifier.CODEC);
    }

    @Override
    protected void beginReload(ReloadType type) {
        super.beginReload(type);
        this.modifiersByID = new HashMap<>();
    }

    @Override
    protected void onReload(ReloadType type) {
        super.onReload(type);
        this.modifiersByID = ImmutableMap.copyOf(this.modifiersByID);
    }


    @Override
    public Map<ResourceLocation, JsonElement> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        return super.prepare(pResourceManager, pProfiler);
    }

    public CafeModifier getForID(String id) {
        if (id == null) return null;
        return this.modifiersByID.get(id);
    }

    @Override
    protected void validateItem(ResourceLocation key, CafeModifier modifier) {
        modifier.validate(key);
        if (this.modifiersByID.containsKey(modifier.modifierId())) {
            String msg = "Attempted to register two modifiers (%s and %s) with the same id: %s!";
            throw new UnsupportedOperationException(String.format(msg, key, this.getKey(this.modifiersByID.get(modifier.modifierId())), modifier.modifierId()));
        }
        this.modifiersByID.put(modifier.modifierId(), modifier);
    }

}