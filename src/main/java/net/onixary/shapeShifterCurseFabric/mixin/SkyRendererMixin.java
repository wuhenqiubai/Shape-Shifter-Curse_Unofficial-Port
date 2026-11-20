package net.onixary.shapeShifterCurseFabric.mixin;

import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.world.level.MoonPhase;
import net.onixary.shapeShifterCurseFabric.cursed_moon.CursedMoon;
import net.onixary.shapeShifterCurseFabric.cursed_moon.CursedMoonSkyTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
    @Unique
    private GpuBufferSlice ssc$cursedMoonBuffer;

    @Unique
    private boolean ssc$isCursedMoon() {
        Minecraft client = Minecraft.getInstance();
        return client.level != null && CursedMoon.isCursedMoonDay(client.level);
    }

    @Unique
    private AbstractTexture ssc$getCursedMoonTexture() {
        return CursedMoonSkyTextures.getCursedMoonTexture();
    }

    @Unique
    private GpuBufferSlice ssc$getCursedMoonBuffer() {
        if (this.ssc$cursedMoonBuffer == null) {
            this.ssc$cursedMoonBuffer = ssc$buildCursedMoonPhases();
        }
        return this.ssc$cursedMoonBuffer;
    }

    @Unique
    private static GpuBufferSlice ssc$buildCursedMoonPhases() {
        MoonPhase[] phases = MoonPhase.values();
        VertexFormat vf = DefaultVertexFormat.POSITION_TEX;
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(phases.length * 4 * vf.getVertexSize())) {
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, com.mojang.renderpearl.api.pipeline.PrimitiveTopology.QUADS, vf);
            for (MoonPhase phase : phases) {
                int idx = phase.index();
                float u0 = idx * 32.0F / 256.0F;
                float u1 = (idx + 1) * 32.0F / 256.0F;
                bufferBuilder.addVertex(-1.0F, 0.0F, -1.0F).setUv(u1, 1.0F);
                bufferBuilder.addVertex(1.0F, 0.0F, -1.0F).setUv(u0, 1.0F);
                bufferBuilder.addVertex(1.0F, 0.0F, 1.0F).setUv(u0, 0.0F);
                bufferBuilder.addVertex(-1.0F, 0.0F, 1.0F).setUv(u1, 0.0F);
            }
            try (MeshData meshData = bufferBuilder.buildOrThrow()) {
                GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "Cursed Moon phases", 32, meshData.vertexBuffer());
                return new GpuBufferSlice(buffer, 0L, meshData.vertexBuffer().remaining());
            }
        }
    }

    @Unique
    private boolean ssc$renderingMoon;

    @Inject(method = "renderMoon", at = @At("HEAD"))
    private void ssc$beginMoon(com.mojang.renderpearl.api.commands.RenderPass renderPass, net.minecraft.world.level.MoonPhase moonPhase, float rainBrightness, PoseStack poseStack, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        this.ssc$renderingMoon = true;
    }

    @Inject(method = "renderMoon", at = @At("RETURN"))
    private void ssc$endMoon(com.mojang.renderpearl.api.commands.RenderPass renderPass, net.minecraft.world.level.MoonPhase moonPhase, float rainBrightness, PoseStack poseStack, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        this.ssc$renderingMoon = false;
    }

    @ModifyArg(method = "drawCelestialBody", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/RenderPass;setUniform(Ljava/lang/String;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Lcom/mojang/renderpearl/api/textures/GpuSampler;)V"), index = 1)
    private GpuTextureView ssc$modifyMoonTexture(GpuTextureView view) {
        return ssc$isCursedMoon() && this.ssc$renderingMoon ? ssc$getCursedMoonTexture().getTextureView() : view;
    }

    @ModifyArg(method = "drawCelestialBody", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/RenderPass;setUniform(Ljava/lang/String;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Lcom/mojang/renderpearl/api/textures/GpuSampler;)V"), index = 2)
    private GpuSampler ssc$modifyMoonSampler(GpuSampler sampler) {
        return ssc$isCursedMoon() && this.ssc$renderingMoon ? ssc$getCursedMoonTexture().getSampler() : sampler;
    }

    @ModifyArg(method = "drawCelestialBody", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/RenderPass;setVertexBuffer(ILcom/mojang/renderpearl/api/buffers/GpuBufferSlice;)V"), index = 1)
    private GpuBufferSlice ssc$modifyMoonBuffer(GpuBufferSlice buffer) {
        return ssc$isCursedMoon() && this.ssc$renderingMoon ? ssc$getCursedMoonBuffer() : buffer;
    }
}