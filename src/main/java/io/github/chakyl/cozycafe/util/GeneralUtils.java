package io.github.chakyl.cozycafe.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.chakyl.cozycafe.data.CafeMenuItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.client.gui.screens.Screen.getTooltipFromItem;

public class GeneralUtils {

    public static int getDay(Level level) {
        return (int) (Math.floor((double) level.dayTime() / 24000) + 1);
    }

    public static String formatPrice(int number) {
        return formatPrice(String.valueOf(number), true);
    }

    public static String formatPrice(String number, boolean truncateMillionsBillions) {
        if (truncateMillionsBillions) {
            if (number.length() < 4) return number;
            if (number.length() > 9) return number.charAt(0) + "." + number.charAt(1) + "B";
            if (number.length() > 6) {
                StringBuilder out = new StringBuilder(3);
                for (int i = 0; i < number.length() - 6; i++) {
                    out.append(number.charAt(i));
                }
                if (number.length() == 7) {
                    out.append('.');
                    out.append(number.charAt(1));
                }
                out.append("M");
                return out.toString();
            }
        }
        int start = number.length() % 3;
        StringBuilder out = new StringBuilder(number.length() + (number.length() / 3));
        out.append(number, 0, start);
        for (int i = 0; i < number.length() / 3; i++) {
            if (i != 0 || start != 0) out.append(",");
            out.append(number, i * 3 + start, i * 3 + start + 3);
        }
        return out.toString();
    }

    public static @NotNull List<Component> getMenuItemTooltip(ItemStack itemStack, CafeMenuItem cafeMenuItem) {
        return getMenuItemTooltip(getTooltipFromItem(Minecraft.getInstance(), itemStack), itemStack, cafeMenuItem);
    }

    public static @NotNull List<Component> getMenuItemTooltip(List<Component> currentTooltips, ItemStack itemStack, CafeMenuItem cafeMenuItem) {
        List<Component> tooltipList = new ArrayList<>(currentTooltips);
        tooltipList.add(Component.literal(Component.translatable("gui.cozycafe.menu_selector.price", cafeMenuItem.price()).getString() + " | " + Component.translatable("category.cozycafe." + cafeMenuItem.category().toString().toLowerCase()).getString()));
        if (!cafeMenuItem.flavors().isEmpty()) {
            tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.flavors", cafeMenuItem.flavors().stream()
                    .map(flavor -> Component.translatable("flavor.cozycafe." + flavor).getString())
                    .collect(Collectors.joining(", "))).withStyle(ChatFormatting.GRAY));
        }
        if (!cafeMenuItem.themes().isEmpty()) {
            tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.themes", cafeMenuItem.themes().stream()
                    .map(flavor -> Component.translatable("theme.cozycafe." + flavor).getString())
                    .collect(Collectors.joining(", "))).withStyle(ChatFormatting.GRAY));
        }
        if (cafeMenuItem.bowlFood()) {
            tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.bowl_food").withStyle(ChatFormatting.GREEN));
        }
        if (cafeMenuItem.bottleDrink()) {
            tooltipList.add(Component.translatable("gui.cozycafe.menu_selector.bottle_drink").withStyle(ChatFormatting.RED));
        }
        return tooltipList;
    }


    // TODO: Find a way to merge renderFood and getFoodModel into just one public method?

    // Nullability here is a bit scuffed
    public static void renderFood(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, @Nullable BlockEntity blockEnt, int light, int overlay) {
        Minecraft minecraft = Minecraft.getInstance();

        if (stack.getItem() instanceof BlockItem blockItem) {
            if (blockEnt == null || blockEnt.getLevel() == null) {
                return;
            }

            poseStack.pushPose();

            BlockState blockState = blockItem.getBlock().defaultBlockState();
            BakedModel model = minecraft.getBlockRenderer().getBlockModel(blockState);

            RandomSource random = RandomSource.create();
            ModelData modelData = ModelData.EMPTY;

            ChunkRenderTypeSet renderTypes = model.getRenderTypes(
                    blockState,
                    random,
                    modelData
            );

            poseStack.translate(-0.5D, 0.0D, -0.5D); // Block models have different coordinates, adjust

            for (RenderType renderType : renderTypes) {
                VertexConsumer consumer = buffer.getBuffer(renderType);

                minecraft.getBlockRenderer().getModelRenderer().tesselateBlock(
                        blockEnt.getLevel(),
                        model,
                        blockState,
                        blockEnt.getBlockPos(),
                        poseStack,
                        consumer,
                        false,
                        random,
                        42L,
                        overlay,
                        modelData,
                        renderType
                );
            }

            poseStack.popPose();

        } else {
            poseStack.pushPose();

            poseStack.translate(0f, 0.05f, 0f);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));

            minecraft.getItemRenderer().renderStatic(
                    stack,
                    ItemDisplayContext.FIXED,
                    light,
                    overlay,
                    poseStack,
                    buffer,
                    null,
                    0
            );

            poseStack.popPose();
        }
    }

    public static BakedModel getFoodModel(ItemStack foodItem) {
        if (foodItem.getItem() instanceof BlockItem blockItem) {
            BlockState foodBlockState = blockItem.getBlock().defaultBlockState();
            return Minecraft.getInstance().getBlockRenderer().getBlockModel(foodBlockState);
        }

        return Minecraft.getInstance().getItemRenderer().getModel(foodItem, null, null, 0);
    }
}
