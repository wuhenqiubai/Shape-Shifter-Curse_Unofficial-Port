package io.github.apace100.apoli.power;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class OverlayPower extends Power {

    private final ResourceLocation texture;
    private final float strength;
    private final float red;
    private final float green;
    private final float blue;
    private final DrawMode drawMode;
    private final DrawPhase drawPhase;
    private final boolean hideWithHud;
    private final boolean visibleInThirdPerson;

    public enum DrawMode {
        NAUSEA, TEXTURE
    }

    public enum DrawPhase {
        BELOW_HUD, ABOVE_HUD
    }

    public OverlayPower(PowerType<?> type, LivingEntity entity, ResourceLocation texture, float strength, float red, float green, float blue, DrawMode drawMode, DrawPhase drawPhase, boolean hideWithHud, boolean visibleInThirdPerson) {
        super(type, entity);
        this.texture = texture;
        this.strength = strength;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.drawMode = drawMode;
        this.drawPhase = drawPhase;
        this.hideWithHud = hideWithHud;
        this.visibleInThirdPerson = visibleInThirdPerson;
    }

    public DrawPhase getDrawPhase() {
        return drawPhase;
    }

    public boolean shouldBeVisibleInThirdPerson() {
        return visibleInThirdPerson;
    }

    public boolean doesHideWithHud() {
        return hideWithHud;
    }

    @Environment(EnvType.CLIENT)
    public void render() {
        Minecraft client = Minecraft.getInstance();
        int i = client.getWindow().getGuiScaledWidth();
        int j = client.getWindow().getGuiScaledHeight();

        float d, e, l, m, n;
        float g, h, k, a;

        switch(drawMode) {
            case NAUSEA:
                d = Mth.lerp(strength, 2.0f, 1.0f);
                g = red * strength;
                h = green * strength;
                k = blue * strength;
                e = i * d;
                l = j * d;
                m = (i - e) / 2.0f;
                n = (j - l) / 2.0f;
                a = 1.0F;
                break;
            case TEXTURE: default:
                g = red;
                h = green;
                k = blue;
                a = strength;
                e = i;
                l = j;
                m = 0;
                n = 0;
                break;
        }

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        switch (drawMode) {
            case NAUSEA:
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
                break;
            case TEXTURE: default:
                RenderSystem.defaultBlendFunc();
                break;
        }
        RenderSystem.setShaderColor(g, h, k, a);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.addVertex(m, n + l, -90.0f).setUv(0.0F, 1.0F);
        bufferBuilder.addVertex(m + e, n + l, -90.0f).setUv(1.0F, 1.0F);
        bufferBuilder.addVertex(m + e, n, -90.0f).setUv(1.0F, 0.0F);
        bufferBuilder.addVertex(m, n, -90.0f).setUv(0.0F, 0.0F);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("overlay"),
            new SerializableData()
                .add("texture", SerializableDataTypes.IDENTIFIER)
                .add("strength", SerializableDataTypes.FLOAT, 1.0F)
                .add("red", SerializableDataTypes.FLOAT, 1.0F)
                .add("green", SerializableDataTypes.FLOAT, 1.0F)
                .add("blue", SerializableDataTypes.FLOAT, 1.0F)
                .add("draw_mode", SerializableDataType.enumValue(OverlayPower.DrawMode.class))
                .add("draw_phase", SerializableDataType.enumValue(OverlayPower.DrawPhase.class))
                .add("hide_with_hud", SerializableDataTypes.BOOLEAN, true)
                .add("visible_in_third_person", SerializableDataTypes.BOOLEAN, false),
            data ->
                (type, player) -> new OverlayPower(type, player,
                    data.getId("texture"),
                    data.getFloat("strength"),
                    data.getFloat("red"),
                    data.getFloat("green"),
                    data.getFloat("blue"),
                    (OverlayPower.DrawMode) data.get("draw_mode"),
                    (OverlayPower.DrawPhase) data.get("draw_phase"),
                    data.getBoolean("hide_with_hud"),
                    data.getBoolean("visible_in_third_person")))
            .allowCondition();
    }
}