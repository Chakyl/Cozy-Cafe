package io.github.chakyl.cozycafe.modevents;

import io.github.chakyl.cozycafe.blockentities.CafeManagerBlockEntity;
import net.neoforged.bus.api.Event;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;

public class CafeClosedEvent extends Event implements ICancellableEvent {
    private final Player player;
    private final CafeManagerBlockEntity cafeManagerBlockEntity;

    public CafeClosedEvent(Player player, CafeManagerBlockEntity cafeManagerBlockEntity) {
        this.player = player;
        this.cafeManagerBlockEntity = cafeManagerBlockEntity;
    }

    public Player getPlayer() {
        return player;
    }

    public CafeManagerBlockEntity getCafeManagerBlockEntity() {
        return cafeManagerBlockEntity;
    }
}