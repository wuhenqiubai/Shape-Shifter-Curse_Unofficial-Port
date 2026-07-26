package net.onixary.shapeShifterCurseFabric.screen_effect;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.onixary.shapeShifterCurseFabric.data.StaticParams;
import org.ladysnake.satin.api.event.ShaderEffectRenderCallback;

public final class TransformFX implements ShaderEffectRenderCallback, ClientTickEvents.EndTick {

    public static final TransformFX INSTANCE = new TransformFX();

	private static boolean doEffectIn = false;
    private static boolean doEffectOut = false;

    @Override
    public void onEndTick(Minecraft client) {

    }

    @Override
    public void renderShaderEffects(float tickDelta) {
        if(doEffectIn || doEffectOut){
            if(doEffectIn){
                float duration = StaticParams.TRANSFORM_FX_DURATION_IN;
	            //uniformSlider.set(1.0f - (transformEffectDuration / duration));
            }
            else {
                float duration = StaticParams.TRANSFORM_FX_DURATION_OUT;
	            //uniformSlider.set(transformEffectDuration / duration);
            }
        }
        //uniformSlider.set(0.5f);
        //transformFXShader.render(tickDelta);
    }

    public static void beginTransformEffect() {
        doEffectIn = true;
        doEffectOut = false;
    }

}