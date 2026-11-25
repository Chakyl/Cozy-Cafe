package io.github.chakyl.cozycafe.modevents;

import io.github.chakyl.cozycafe.blockentities.CafeManagerBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;

public class DishServedEvent extends Event implements ICancellableEvent {
    private final Player player;
    private final CafeManagerBlockEntity cafeManagerBlockEntity;
    private final ItemStack dish;
    private final int moneyEarned;

    public DishServedEvent(Player player, CafeManagerBlockEntity cafeManagerBlockEntity, ItemStack dish, int moneyEarned) {
        this.player = player;
        this.cafeManagerBlockEntity = cafeManagerBlockEntity;
        this.dish = dish;
        this.moneyEarned = moneyEarned;
    }

    public Player getPlayer() {
        return player;
    }

    public CafeManagerBlockEntity getCafeManagerBlockEntity() {
        return cafeManagerBlockEntity;
    }

    public ItemStack getDish() {
        return dish;
    }

    public int getMoneyEarned() {
        return moneyEarned;
    }
}