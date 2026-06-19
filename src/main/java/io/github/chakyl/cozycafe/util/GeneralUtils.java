package io.github.chakyl.cozycafe.util;

import net.minecraft.world.level.Level;

public class GeneralUtils {
    public static int getDay(Level level) {
        return (int) (Math.floor((double) level.dayTime() / 24000) + 1);
    }
}
