package net.onixary.shapeShifterCurseFabric.render.form_render;

public final class ProceduralAnimMath {
    public static final float REFERENCE_FPS = 60.0F;
    private static final float TICKS_PER_SECOND = 20.0F;
    private static final double TAU = Math.PI * 2.0;

    private ProceduralAnimMath() {}

    public static double continuousTick(int tickCount, float tickDelta) {
        return (double) tickCount + (double) tickDelta;
    }

    public static float deltaTicks(int tickCount, float tickDelta, int lastTickCount, float lastTickDelta) {
        double delta = ((double) tickCount - (double) lastTickCount) + ((double) tickDelta - (double) lastTickDelta);
        if (!Double.isFinite(delta) || delta <= 0.0) {
            return 0.0F;
        }
        return (float) Math.min(delta, 1.0);
    }

    public static float cosTicks(double ticks, double radiansPerTick, double offsetRadians) {
        double phase = ticks * radiansPerTick + offsetRadians;
        if (!Double.isFinite(phase)) {
            return 0.0F;
        }
        phase -= Math.floor(phase / TAU) * TAU;
        return (float) Math.cos(phase);
    }

    public static float expAlpha(float baseAlpha, float deltaTicks) {
        if (deltaTicks <= 0.0F) {
            return 0.0F;
        }
        return 1.0F - (float) Math.pow(1.0F - baseAlpha, referenceFrames(deltaTicks));
    }

    public static float expDecay(float baseDecay, float deltaTicks) {
        if (deltaTicks <= 0.0F) {
            return 1.0F;
        }
        return (float) Math.pow(baseDecay, referenceFrames(deltaTicks));
    }

    public static float expGain(float baseGain, float baseDecay, float deltaTicks) {
        if (baseDecay >= 1.0F) {
            return baseGain * referenceFrames(deltaTicks);
        }
        return baseGain * (1.0F - expDecay(baseDecay, deltaTicks)) / (1.0F - baseDecay);
    }

    private static float referenceFrames(float deltaTicks) {
        return deltaTicks * REFERENCE_FPS / TICKS_PER_SECOND;
    }
}
