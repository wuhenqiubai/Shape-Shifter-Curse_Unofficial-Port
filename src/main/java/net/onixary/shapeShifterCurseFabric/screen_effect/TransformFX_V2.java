package net.onixary.shapeShifterCurseFabric.screen_effect;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.data.StaticParams;

import java.io.IOException;
import java.util.List;

public final class TransformFX_V2 implements ClientTickEvents.EndTick {

    public static final TransformFX_V2 INSTANCE = new TransformFX_V2();

    private PostChain processor;
    private Uniform sliderUniform;
    private boolean initializationFailed = false;

    private static int transformEffectDuration = 0;
    private static boolean doEffectIn = false;
    private static boolean doEffectOut = false;

    private int lastWidth = -1;
    private int lastHeight = -1;

    public void registerCallbacks() {
        ClientTickEvents.END_CLIENT_TICK.register(this);
        WorldRenderEvents.LAST.register(ctx -> INSTANCE.render(ctx.tickCounter().getGameTimeDeltaPartialTick(false)));

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
                .registerReloadListener(new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public ResourceLocation getFabricId() {
                        return ShapeShifterCurseFabric.identifier("transform_fx_reload");
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager manager) {
                        INSTANCE.onResourceReload();
                    }
                });
    }

    @Override
    public void onEndTick(Minecraft client) {
        if (client.level == null) {
            doEffectIn = false;
            doEffectOut = false;
            transformEffectDuration = 0;
            return;
        }

        if (!doEffectIn && !doEffectOut) return;

        ensureProcessor(client);

        if (client.isPaused()) return;

        --transformEffectDuration;
        if (transformEffectDuration <= 0) {
            finishEffect();
        }
    }

    private void ensureProcessor(Minecraft client) {
        if (processor != null || initializationFailed) return;
        if (client.getResourceManager() == null) return;

        try {
            ResourceLocation id = ShapeShifterCurseFabric.identifier("shaders/post/transform_effect_v2.json");
            processor = new PostChain(
                    client.getTextureManager(),
                    client.getResourceManager(),
                    client.getMainRenderTarget(),
                    id
            );

            List<PostPass> passes = processor.passes;
            for (int i = 0; i < passes.size(); i++) {
                Uniform u = passes.get(i).getEffect().getUniform("Slider");
                if (u != null) {
                    sliderUniform = u;
                    break;
                }
            }
        } catch (IOException | RuntimeException e) {
            if (processor != null) {
                processor.close();
            }
            processor = null;
            sliderUniform = null;
            initializationFailed = true;
            ShapeShifterCurseFabric.LOGGER.error("Transform post effect disabled until resource reload; press F3+T to retry", e);
        } finally {
            client.getMainRenderTarget().bindWrite(true);
        }
    }

    public void render(float tickDelta) {
        if (processor == null || (!doEffectIn && !doEffectOut)) return;

        Minecraft client = Minecraft.getInstance();

        int w = client.getWindow().getWidth();
        int h = client.getWindow().getHeight();
        if (w != lastWidth || h != lastHeight) {
            processor.resize(w, h);
            lastWidth = w;
            lastHeight = h;
        }

        float progress;
        if (doEffectIn) {
            float d = StaticParams.TRANSFORM_FX_DURATION_IN;
            progress = 1.0f - (float) transformEffectDuration / d;
        } else {
            float d = StaticParams.TRANSFORM_FX_DURATION_OUT;
            progress = (float) transformEffectDuration / d;
        }
        progress = Math.max(0f, Math.min(1f, progress));

        if (sliderUniform != null) {
            sliderUniform.set(progress);
        }

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        try {
            processor.process(tickDelta);
        } finally {
            client.getMainRenderTarget().bindWrite(true);
            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        }
    }

    private void finishEffect() {
        if (doEffectIn) doEffectIn = false;
        else if (doEffectOut) doEffectOut = false;
        transformEffectDuration = 0;
    }

    public void onResourceReload() {
        initializationFailed = false;
        if (processor != null) {
            processor.close();
            processor = null;
        }
        sliderUniform = null;
        lastWidth = -1;
        lastHeight = -1;
    }

    public static void beginTransformEffect() {
        doEffectIn = true;
        doEffectOut = false;
        transformEffectDuration = StaticParams.TRANSFORM_FX_DURATION_IN;
    }

    public static void endTransformEffect() {
        doEffectIn = false;
        doEffectOut = true;
        transformEffectDuration = StaticParams.TRANSFORM_FX_DURATION_OUT;
    }
}