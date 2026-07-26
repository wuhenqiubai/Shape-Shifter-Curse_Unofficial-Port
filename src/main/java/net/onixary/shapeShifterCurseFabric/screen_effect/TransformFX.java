package net.onixary.shapeShifterCurseFabric.screen_effect;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.onixary.shapeShifterCurseFabric.data.StaticParams;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.satin.api.event.ShaderEffectRenderCallback;
import org.ladysnake.satin.api.managed.ManagedShaderEffect;
import org.ladysnake.satin.api.managed.ShaderEffectManager;
import org.ladysnake.satin.api.managed.uniform.Uniform1f;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

public final class TransformFX implements ShaderEffectRenderCallback, ClientTickEvents.EndTick {
    public static final ResourceLocation TRANSFORM_EFFECT_SHADER_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "shaders/post/transform_effect.json");

    public static final TransformFX INSTANCE = new TransformFX();
    private final Minecraft mc = Minecraft.getInstance();
    private final ManagedShaderEffect transformFXShader = ShaderEffectManager.getInstance().manage(TRANSFORM_EFFECT_SHADER_ID);

    private Uniform1f uniformSlider = transformFXShader.findUniform1f("Slider");
    private static float transformEffectDuration = 0.0f;
    private static boolean doEffectIn = false;
    private static boolean doEffectOut = false;

    private int ticks = 0;
    @Nullable
    private RenderTarget framebuffer;

    public void registerCallbacks() {
        ShaderEffectRenderCallback.EVENT.register(this);
        ClientTickEvents.END_CLIENT_TICK.register(this);
    }

    @Override
    public void onEndTick(Minecraft client) {
        ++ticks;

    }

    @Override
    public void renderShaderEffects(float tickDelta) {
        if(doEffectIn || doEffectOut){
            if(doEffectIn){
                float duration = StaticParams.TRANSFORM_FX_DURATION_IN;
                transformEffectDuration -= tickDelta;
                //uniformSlider.set(1.0f - (transformEffectDuration / duration));
            }
            else {
                float duration = StaticParams.TRANSFORM_FX_DURATION_OUT;
                transformEffectDuration -= tickDelta;
                //uniformSlider.set(transformEffectDuration / duration);
            }
        }
        //uniformSlider.set(0.5f);
        //transformFXShader.render(tickDelta);
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