package io.github.chakyl.cozycafe.data;

import dev.shadowsoffire.placebo.codec.CodecProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;

public sealed interface AbstractCafeMenuItem extends CodecProvider<CafeMenuItem> permits CafeMenuItem {

    Item item();

    Item itemRender();

    Block blockRender();

    CafeMenuItem.MenuItemCategory category();

    String multAttribute();

    int price();

    Item bowl();

    boolean bowlFood();

    boolean bottleDrink();

    Item bottle();

    List<String> flavors();

}