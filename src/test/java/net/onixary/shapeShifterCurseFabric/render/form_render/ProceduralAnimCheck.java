package net.onixary.shapeShifterCurseFabric.render.form_render;

public final class ProceduralAnimCheck {
    public static void main(String[] args) {
        float frame60 = 1.0F / 3.0F;
        expect(Math.abs(ProceduralAnimMath.continuousTick(3, 0.5F) - 3.5) < 1e-9, "continuousTick");
        expect(ProceduralAnimMath.deltaTicks(5, 0.0F, 5, 0.0F) == 0.0F, "deltaTicks duplicate frame");
        expect(ProceduralAnimMath.deltaTicks(0, 0.1F, 50000, 0.5F) == 0.0F, "deltaTicks respawn reset");
        expect(Math.abs(ProceduralAnimMath.deltaTicks(0, 0.4F, 0, 0.1F) - 0.3F) < 1e-6F, "deltaTicks after reset");
        expect(Math.abs(ProceduralAnimMath.deltaTicks(2000000, 1.2333333F, 2000000, 0.9F) - 0.3333333F) < 1e-5F, "deltaTicks long uptime precision");
        expect(Math.abs(ProceduralAnimMath.deltaTicks(100, 1.5F, 100, 0.0F) - 1.0F) < 1e-6F, "deltaTicks clamp");
        expect(ProceduralAnimMath.deltaTicks(5, Float.NaN, 4, 0.0F) == 0.0F, "deltaTicks nan");

        float rate = 0.33333334F * 0.5F;
        double ticks = 2000000.9;
        expect(Math.abs(ProceduralAnimMath.cosTicks(ticks, rate, 0.0) - (float) Math.cos((ticks * rate) % (Math.PI * 2.0))) < 1e-5F, "cosTicks long uptime");
        expect(Math.abs(ProceduralAnimMath.cosTicks(ticks, rate, 0.0) - ProceduralAnimMath.cosTicks(ticks + 1e-4, rate, 0.0)) < 1e-4F, "cosTicks continuity");
        expect(Math.abs(ProceduralAnimMath.cosTicks(0.0, rate, 0.0) - 1.0F) < 1e-6F, "cosTicks zero");
        expect(Math.abs(ProceduralAnimMath.cosTicks(123.456, rate, 1.0) - (float) Math.cos((123.456 * rate + 1.0) % (Math.PI * 2.0))) < 1e-5F, "cosTicks offset");

        expect(Math.abs(ProceduralAnimMath.expAlpha(0.04F, frame60) - 0.04F) < 1e-6F, "expAlpha 60fps");
        expect(Math.abs(ProceduralAnimMath.expAlpha(0.04F, 1.0F) - 0.115264F) < 1e-5F, "expAlpha 20fps");
        expect(ProceduralAnimMath.expAlpha(0.04F, 0.0F) == 0.0F, "expAlpha zero");
        expect(Math.abs(ProceduralAnimMath.expDecay(0.75F, frame60) - 0.75F) < 1e-6F, "expDecay 60fps");
        expect(Math.abs(ProceduralAnimMath.expDecay(0.8F, 1.0F) - 0.512F) < 1e-6F, "expDecay 20fps");
        expect(Math.abs(ProceduralAnimMath.expGain(0.15F, 0.8F, frame60) - 0.15F) < 1e-6F, "expGain 60fps");
        for (float dt : new float[] {frame60, 0.5F, 1.0F}) {
            float decay = ProceduralAnimMath.expDecay(0.8F, dt);
            float gain = ProceduralAnimMath.expGain(0.15F, 0.8F, dt);
            expect(Math.abs(gain / (1.0F - decay) - 0.75F) < 1e-5F, "expGain steady state " + dt);
        }
        float previousAlpha = -1.0F;
        for (int i = 1; i <= 20; i++) {
            float alpha = ProceduralAnimMath.expAlpha(0.04F, i * 0.05F);
            expect(alpha > previousAlpha, "expAlpha monotonic");
            previousAlpha = alpha;
        }
        float previousDecay = 2.0F;
        for (int i = 1; i <= 20; i++) {
            float decay = ProceduralAnimMath.expDecay(0.8F, i * 0.05F);
            expect(decay < previousDecay, "expDecay monotonic");
            previousDecay = decay;
        }
        System.out.println("Procedural animation time-base checks passed.");
    }

    private static void expect(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError(label);
        }
    }
}
