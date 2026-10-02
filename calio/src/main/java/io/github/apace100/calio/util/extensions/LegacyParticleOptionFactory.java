package io.github.apace100.calio.util.extensions;

import net.minecraft.core.particles.ParticleOptions;

import java.util.function.Function;

public interface LegacyParticleOptionFactory {
    void calio$addLegacyParticleOptionFactory(Function<String, ParticleOptions> factory);
    ParticleOptions calio$createFromParams(String params);
}
