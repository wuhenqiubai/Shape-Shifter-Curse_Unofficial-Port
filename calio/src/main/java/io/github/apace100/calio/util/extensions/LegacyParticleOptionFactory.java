package io.github.apace100.calio.util.extensions;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface LegacyParticleOptionFactory {
    void calio$addLegacyParticleOptionFactory(Function<String, ParticleOptions> factory);
    void calio$addLegacyParticleOptionFactory(BiFunction<String, HolderLookup.Provider, ParticleOptions> factory);
    ParticleOptions calio$createFromParams(String params, HolderLookup.Provider provider);
}
