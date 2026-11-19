package io.github.apace100.apoli.power;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import io.github.apace100.apoli.Apoli;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;

@Environment(EnvType.CLIENT)
public class OverlayPowerPipelines {
    @Environment(EnvType.CLIENT)
    public static final RenderPipeline.Snippet OVERLAY_SNIPPET = RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
        .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
        .withFragmentShader("core/position_tex_color")
        .withVertexShader("core/position_tex_color")
        .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
        .withPrimitiveTopology(PrimitiveTopology.QUADS)
        .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
        .withColorTargetState(new ColorTargetState(BlendFunction.OVERLAY))
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
