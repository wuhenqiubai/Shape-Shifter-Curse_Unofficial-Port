package net.onixary.shapeShifterCurseFabric.screen_effect;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

public final class TransformOverlay {
    public static final TransformOverlay INSTANCE = new TransformOverlay();
    private final ResourceLocation nausea_texture = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/overlay/nausea_black.png");
    private final ResourceLocation black_texture = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/overlay/black.png");

    private boolean enableOverlay = false;
    private float strength_nausea = 0.0f;
    private float strength_black = 0.0f;

    public void init() {
        enableOverlay = false;
        strength_nausea = 0.0f;
        strength_black = 0.0f;
    }

    @Environment(EnvType.CLIENT)
    public void render()  {
        if(!enableOverlay){
            return;
        }

        Minecraft client = Minecraft.getInstance();
        int i = client.getWindow().getGuiScaledWidth();
        int j = client.getWindow().getGuiScaledHeight();
        Matrix4f matrix4f = new Matrix4f().identity();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, strength_nausea);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, nausea_texture);
        Tesselator tessellator_nausea = Tesselator.getInstance();
        BufferBuilder bufferBuilder_nausea = tessellator_nausea.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder_nausea.addVertex(matrix4f, 0, j, -90.0F).setUv(0.0F, 1.0F);
        bufferBuilder_nausea.addVertex(matrix4f, i, j, -90.0F).setUv(1.0F, 1.0F);
        bufferBuilder_nausea.addVertex(matrix4f, i, 0, -90.0F).setUv(1.0F, 0.0F);
        bufferBuilder_nausea.addVertex(matrix4f, 0, 0, -90.0F).setUv(0.0F, 0.0F);
        BufferUploader.drawWithShader(bufferBuilder_nausea.buildOrThrow());
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, strength_black);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, black_texture);
        Tesselator tessellator_black = Tesselator.getInstance();
        BufferBuilder bufferBuilder_black = tessellator_black.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder_black.addVertex(matrix4f, 0, j, -90.0F).setUv(0.0F, 1.0F);
        bufferBuilder_black.addVertex(matrix4f, i, j, -90.0F).setUv(1.0F, 1.0F);
        bufferBuilder_black.addVertex(matrix4f, i, 0, -90.0F).setUv(1.0F, 0.0F);
        bufferBuilder_black.addVertex(matrix4f, 0, 0, -90.0F).setUv(0.0F, 0.0F);
        BufferUploader.drawWithShader(bufferBuilder_black.buildOrThrow());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    public void setEnableOverlay(boolean enableOverlay) {
        this.enableOverlay = enableOverlay;
    }

    public void setNauesaStrength(float strength) {
        this.strength_nausea = Mth.clamp(strength, 0.0f, 1.0f);
    }

    public void setBlackStrength(float strength) {
        this.strength_black = Mth.clamp(strength, 0.0f, 1.0f);
    }
}