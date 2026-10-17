package net.onixary.shapeShifterCurseFabric.mixin;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
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

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
    @Unique
    private GpuBuffer ssc$cursedMoonBuffer;

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
    private GpuBuffer ssc$getCursedMoonBuffer() {
        if (this.ssc$cursedMoonBuffer == null) {
            this.ssc$cursedMoonBuffer = ssc$buildCursedMoonPhases();
        }
        return this.ssc$cursedMoonBuffer;
    }

    @Unique
    private static GpuBuffer ssc$buildCursedMoonPhases() {
        MoonPhase[] phases = MoonPhase.values();
        VertexFormat vf = DefaultVertexFormat.POSITION_TEX;
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(phases.length * 4 * vf.getVertexSize())) {
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, vf);
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
                return RenderSystem.getDevice().createBuffer(() -> "Cursed Moon phases", 32, meshData.vertexBuffer());
            }
        }
    }

    @ModifyArg(method = "renderMoon", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;bindTexture(Ljava/lang/String;Lcom/mojang/blaze3d/textures/GpuTextureView;Lcom/mojang/blaze3d/textures/GpuSampler;)V"), index = 1)
    private GpuTextureView ssc$modifyMoonTexture(GpuTextureView view) {
        return ssc$isCursedMoon() ? ssc$getCursedMoonTexture().getTextureView() : view;
    }

    @ModifyArg(method = "renderMoon", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;bindTexture(Ljava/lang/String;Lcom/mojang/blaze3d/textures/GpuTextureView;Lcom/mojang/blaze3d/textures/GpuSampler;)V"), index = 2)
    private GpuSampler ssc$modifyMoonSampler(GpuSampler sampler) {
        return ssc$isCursedMoon() ? ssc$getCursedMoonTexture().getSampler() : sampler;
    }

    @ModifyArg(method = "renderMoon", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;setVertexBuffer(ILcom/mojang/blaze3d/buffers/GpuBuffer;)V"), index = 1)
    private GpuBuffer ssc$modifyMoonBuffer(GpuBuffer buffer) {
        return ssc$isCursedMoon() ? ssc$getCursedMoonBuffer() : buffer;
    }
}