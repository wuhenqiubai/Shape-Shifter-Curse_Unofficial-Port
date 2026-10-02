package io.github.apace100.apoli.power;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.apace100.apoli.Apoli;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderPipelines;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public class OverlayPowerPipelines {
    @Environment(EnvType.CLIENT)
    public static final RenderPipeline.Snippet OVERLAY_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
        .withFragmentShader("core/position_tex_color")
        .withVertexShader("core/position_tex_color")
        .withSampler("Sampler0")
        .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
        .withUniform("ColorModulator", UniformType.UNIFORM_BUFFER) // We don't need this, but also, for some reason it will spam the logs if we don't use it?
        .withColorTargetState(new ColorTargetState(Optional.of(BlendFunction.OVERLAY), ColorTargetState.WRITE_ALL))
        .withDepthStencilState(new DepthStencilState(CompareOp.NEVER_PASS, false))
        .buildSnippet();

    @Environment(EnvType.CLIENT)
    public static final RenderPipeline OVERLAY_PIPELINE = RenderPipeline.builder(OVERLAY_SNIPPET)
        .withLocation(Apoli.identifier("pipeline/overlay"))
        .build();

    @Environment(EnvType.CLIENT)
    public static final RenderPipeline NAUSEA_PIPELINE = RenderPipeline.builder(OVERLAY_SNIPPET)
        .withLocation(Apoli.identifier("pipeline/overlay_nausea"))
        .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
        .build();
}
