package io.github.chakyl.cozycafe.data;

import dev.shadowsoffire.placebo.codec.CodecProvider;
import net.minecraft.world.item.Item;

import java.util.List;

public sealed interface AbstractCafeMenuItem extends CodecProvider<CafeMenuItem> permits CafeMenuItem {

    Item item();

    CafeMenuItem.MenuItemCategory category();

    String multAttribute();

    int price();

    Item bowl();

    boolean bowlFood();

    boolean bottleDrink();

    Item bottle();

    List<String> flavors();

}