package io.github.chakyl.cozycafe.client;


import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

// Stolen and adapted to AABB from Torchmaster by Xalcon (MIT licensed)
public class VolumeRendererOverlay {
    // TODO: Perhaps use a more cozier texture?
    private static final ResourceLocation FORCEFIELD_LOCATION = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/misc/forcefield.png");

    private static BoundingBox createVolume(Vec3i pos, int halfRange) {
        Vec3i min = pos.offset(-halfRange, -halfRange, -halfRange);
        Vec3i max = pos.offset(halfRange + 1, halfRange + 1, halfRange + 1);
        return BoundingBox.fromCorners(min, max);
    }

    public static BoundingBox convertAABBtoBoundingBox(AABB aabb) {
        return new BoundingBox(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ));
    }

    private static void renderWireframeCube(AABB aabb, int color, Camera cam) {
        var mc = Minecraft.getInstance();
        var blockRenderDistance = mc.options.getEffectiveRenderDistance() * 16;
        var areaVol = convertAABBtoBoundingBox(aabb);
        var playerVolume = createVolume(cam.getBlockPosition(), blockRenderDistance);
        if (!playerVolume.intersects(areaVol)) return;

        var camX = cam.getPosition().x;
        var camZ = cam.getPosition().z;
        var camY = cam.getPosition().y;

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.depthMask(Minecraft.useShaderTransparency());

        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        RenderSystem.setShaderColor(red, green, blue, 1f);
        RenderSystem.setShader(GameRenderer::getPositionShader);

        RenderSystem.polygonOffset(-3.0F, -3.0F);
        RenderSystem.enablePolygonOffset();
        RenderSystem.disableCull();

        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        var vMinX = (float) (areaVol.minX() - camX);
        var vMaxX = (float) (areaVol.maxX() - camX);
        var vMinY = (float) (areaVol.minY() - camY);
        var vMaxY = (float) (areaVol.maxY() - camY);
        var vMinZ = (float) (areaVol.minZ() - camZ);
        var vMaxZ = (float) (areaVol.maxZ() - camZ);

        color = 0xFFFFFFFF;
        // Bottom face
        bufferbuilder.addVertex(vMinX, vMinY, vMinZ).setColor(color);
        bufferbuilder.addVertex(vMaxX, vMinY, vMinZ).setColor(color);
        bufferbuilder.addVertex(vMaxX, vMinY, vMinZ).setColor(color);
        bufferbuilder.addVertex(vMaxX, vMinY, vMaxZ).setColor(color);
        bufferbuilder.addVertex(vMaxX, vMinY, vMaxZ).setColor(color);
        bufferbuilder.addVertex(vMinX, vMinY, vMaxZ).setColor(color);
        bufferbuilder.addVertex(vMinX, vMinY, vMaxZ).setColor(color);
        bufferbuilder.addVertex(vMinX, vMinY, vMinZ).setColor(color);

        // Top face
        bufferbuilder.addVertex(vMinX, vMaxY, vMinZ).setColor(color);
        bufferbuilder.addVertex(vMaxX, vMaxY, vMinZ).setColor(color);
        bufferbuilder.addVertex(vMaxX, vMaxY, vMinZ).setColor(color);
        bufferbuilder.addVertex(vMaxX, vMaxY, vMaxZ).setColor(color);
        bufferbuilder.addVertex(vMaxX, vMaxY, vMaxZ).setColor(color);
        bufferbuilder.addVertex(vMinX, vMaxY, vMaxZ).setColor(color);
        bufferbuilder.addVertex(vMinX, vMaxY, vMaxZ).setColor(color);
        bufferbuilder.addVertex(vMinX, vMaxY, vMinZ).setColor(color);

        // Vertical edges
        bufferbuilder.addVertex(vMinX, vMinY, vMinZ).setColor(color);
        bufferbuilder.addVertex(vMinX, vMaxY, vMinZ).setColor(color);
        bufferbuilder.addVertex(vMaxX, vMinY, vMinZ).setColor(color);
        bufferbuilder.addVertex(vMaxX, vMaxY, vMinZ).setColor(color);
        bufferbuilder.addVertex(vMaxX, vMinY, vMaxZ).setColor(color);
        bufferbuilder.addVertex(vMaxX, vMaxY, vMaxZ).setColor(color);
        bufferbuilder.addVertex(vMinX, vMinY, vMaxZ).setColor(color);
        bufferbuilder.addVertex(vMinX, vMaxY, vMaxZ).setColor(color);

        var meshdata = bufferbuilder.build();
        if (meshdata != null) {
            BufferUploader.drawWithShader(meshdata);
        }

        RenderSystem.enableCull();
        RenderSystem.polygonOffset(0.0F, 0.0F);
        RenderSystem.disablePolygonOffset();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
    }

    private static void renderLightVolume(AABB aabb, int color, Camera cam) {
        var mc = Minecraft.getInstance();
        var blockRenderDistance = mc.options.getEffectiveRenderDistance() * 16;
        var areaVol = convertAABBtoBoundingBox(aabb);
        var playerVolume = createVolume(cam.getBlockPosition(), blockRenderDistance);
        if (!playerVolume.intersects(areaVol)) return;

        var camX = cam.getPosition().x;
        var camZ = cam.getPosition().z;
        var camY = cam.getPosition().y;

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.setShaderTexture(0, FORCEFIELD_LOCATION);
        RenderSystem.depthMask(Minecraft.useShaderTransparency());

        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        RenderSystem.setShaderColor(red, green, blue, 1f);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        RenderSystem.polygonOffset(-3.0F, -3.0F);
        RenderSystem.enablePolygonOffset();

        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        var vMinX = (float) (areaVol.minX() - camX);
        var vMaxX = (float) (areaVol.maxX() - camX);
        var vMinY = (float) (areaVol.minY() - camY);
        var vMaxY = (float) (areaVol.maxY() - camY);
        var vMinZ = (float) (areaVol.minZ() - camZ);
        var vMaxZ = (float) (areaVol.maxZ() - camZ);

        var uv0 = (float) (Util.getMillis() % 3000L) / 3000.0F;
        var uv1 = 5 + uv0;

        // +X
        bufferbuilder.addVertex(vMaxX, vMinY, vMinZ).setUv(uv1, uv1);
        bufferbuilder.addVertex(vMaxX, vMinY, vMaxZ).setUv(uv1, uv0);
        bufferbuilder.addVertex(vMaxX, vMaxY, vMaxZ).setUv(uv0, uv0);
        bufferbuilder.addVertex(vMaxX, vMaxY, vMinZ).setUv(uv0, uv1);

        // -X
        bufferbuilder.addVertex(vMinX, vMinY, vMinZ).setUv(uv1, uv1);
        bufferbuilder.addVertex(vMinX, vMinY, vMaxZ).setUv(uv1, uv0);
        bufferbuilder.addVertex(vMinX, vMaxY, vMaxZ).setUv(uv0, uv0);
        bufferbuilder.addVertex(vMinX, vMaxY, vMinZ).setUv(uv0, uv1);

        // +Z
        bufferbuilder.addVertex(vMinX, vMinY, vMaxZ).setUv(uv1, uv1);
        bufferbuilder.addVertex(vMaxX, vMinY, vMaxZ).setUv(uv1, uv0);
        bufferbuilder.addVertex(vMaxX, vMaxY, vMaxZ).setUv(uv0, uv0);
        bufferbuilder.addVertex(vMinX, vMaxY, vMaxZ).setUv(uv0, uv1);

        // -Z
        bufferbuilder.addVertex(vMinX, vMinY, vMinZ).setUv(uv1, uv1);
        bufferbuilder.addVertex(vMaxX, vMinY, vMinZ).setUv(uv1, uv0);
        bufferbuilder.addVertex(vMaxX, vMaxY, vMinZ).setUv(uv0, uv0);
        bufferbuilder.addVertex(vMinX, vMaxY, vMinZ).setUv(uv0, uv1);

        // +Y
        bufferbuilder.addVertex(vMinX, vMaxY, vMinZ).setUv(uv1, uv1);
        bufferbuilder.addVertex(vMaxX, vMaxY, vMinZ).setUv(uv1, uv0);
        bufferbuilder.addVertex(vMaxX, vMaxY, vMaxZ).setUv(uv0, uv0);
        bufferbuilder.addVertex(vMinX, vMaxY, vMaxZ).setUv(uv0, uv1);

        // -Y
        bufferbuilder.addVertex(vMinX, vMinY, vMinZ).setUv(uv1, uv1);
        bufferbuilder.addVertex(vMaxX, vMinY, vMinZ).setUv(uv1, uv0);
        bufferbuilder.addVertex(vMaxX, vMinY, vMaxZ).setUv(uv0, uv0);
        bufferbuilder.addVertex(vMinX, vMinY, vMaxZ).setUv(uv0, uv1);

        var meshdata = bufferbuilder.build();
        if (meshdata != null) {
            BufferUploader.drawWithShader(meshdata);
        }

        RenderSystem.enableCull();
        RenderSystem.polygonOffset(0.0F, 0.0F);
        RenderSystem.disablePolygonOffset();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
    }

    public static void onRenderLevel(Camera camera, AABB outline) {
        int color = 0xFFA0FF20;
        renderLightVolume(outline, color, camera);
        renderWireframeCube(outline, color, camera);
    }
}