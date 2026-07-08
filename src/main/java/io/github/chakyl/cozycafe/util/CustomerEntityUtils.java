package io.github.chakyl.cozycafe.util;

import com.mojang.authlib.GameProfile;
import io.github.chakyl.cozycafe.entities.CustomerEntity;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.GameProfileCache;

public class CustomerEntityUtils {
    public static void spawnCustomerAndTarget(BlockPos pos, ServerLevel serverLevel, String skinUsername, BlockPos targetPos, CustomerEntity customer, CustomerTarget target) {
        MinecraftServer server = serverLevel.getServer();
        GameProfileCache cache = server.getProfileCache();
        if (cache != null && !net.minecraft.util.StringUtil.isNullOrEmpty(skinUsername)) {
            cache.getAsync(skinUsername, profile -> {
                if (profile != null && profile.isPresent()) {
                    Util.backgroundExecutor().execute(() -> {
                        GameProfile filledProfile = server.getSessionService().fillProfileProperties(profile.get(), true);
                        server.execute(() -> {
                            customer.getEntityData().set(CustomerEntity.SKIN_PROFILE, NbtUtils.writeGameProfile(new CompoundTag(), filledProfile));
                            finalizeSpawn(pos, serverLevel, targetPos, customer, target);
                        });
                    });
                } else {
                    server.execute(() -> finalizeSpawn(pos, serverLevel, targetPos, customer, target));
                }
            });
        } else {
            finalizeSpawn(pos, serverLevel, targetPos, customer, target);
        }
    }

    private static void finalizeSpawn(BlockPos pos, ServerLevel serverLevel, BlockPos targetPos, CustomerEntity customer, CustomerTarget target) {
        customer.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, serverLevel.random.nextFloat() * 360.0F, 0.0F);

        if (target == CustomerTarget.MENU) {
            customer.setTargetMenuPos(targetPos);
        } else if (target == CustomerTarget.SIGN) {
            customer.setTargetSignPos(targetPos);
        }

        serverLevel.addFreshEntity(customer);
    }
}
