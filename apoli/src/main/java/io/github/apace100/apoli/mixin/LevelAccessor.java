package io.github.apace100.apoli.mixin;

import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Level.class)
public interface LevelAccessor {
    @Accessor("DEFAULT_EXPLOSION_BLOCK_PARTICLES")
    static WeightedList<ExplosionParticleInfo> apoli$getDefaultExplosionBlockParticles() {
        throw new AssertionError();
    }
}
