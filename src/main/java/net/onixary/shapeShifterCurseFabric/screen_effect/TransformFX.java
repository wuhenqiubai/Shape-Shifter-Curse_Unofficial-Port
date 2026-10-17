package net.onixary.shapeShifterCurseFabric.screen_effect;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.onixary.shapeShifterCurseFabric.data.StaticParams;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

public final class TransformFX implements ClientTickEvents.EndTick {
    public static final Identifier TRANSFORM_EFFECT_SHADER_ID = Identifier.fromNamespaceAndPath(MOD_ID, "shaders/post/transform_effect.json");

    public static final TransformFX INSTANCE = new TransformFX();
    private final Minecraft mc = Minecraft.getInstance();

    private static float transformEffectDuration = 0.0f;
    private static boolean doEffectIn = false;
    private static boolean doEffectOut = false;

    private int ticks = 0;

    public void registerCallbacks() {
        ClientTickEvents.END_CLIENT_TICK.register(this);
    }

    @Override
    public void onEndTick(Minecraft client) {
        ++ticks;
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
