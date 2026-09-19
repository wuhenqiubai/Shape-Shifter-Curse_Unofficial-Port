package io.github.apace100.calio.mixin.particles;

import io.github.apace100.calio.util.extensions.LegacyParticleOptionFactory;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.BiFunction;
import java.util.function.Function;

@Mixin(targets = "net.minecraft.core.particles.ParticleTypes$1")
public abstract class ParticleTypesAnonymousMixin implements LegacyParticleOptionFactory {
    @Unique private BiFunction<String, HolderLookup.Provider, ParticleOptions> calio$particleOptionFactory = null;

    @Override
    public void calio$addLegacyParticleOptionFactory(Function<String, ParticleOptions> factory) {
        this.calio$particleOptionFactory = (s, provider) -> factory.apply(s);
    }

    @Override
    public void calio$addLegacyParticleOptionFactory(BiFunction<String, HolderLookup.Provider, ParticleOptions> factory) {
        this.calio$particleOptionFactory = factory;
    }

    @Override
    public ParticleOptions calio$createFromParams(String params, HolderLookup.Provider provider) {
        if (calio$particleOptionFactory != null) {
            return calio$particleOptionFactory.apply(params, provider);
        }

        return null;
    }
}
