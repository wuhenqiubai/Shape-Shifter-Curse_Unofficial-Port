package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.HiddenEffectStatus;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MobEffectInstance.class)
public class StatusEffectInstanceMixin implements HiddenEffectStatus {
    @Shadow
    @Nullable
    private MobEffectInstance hiddenEffect;

    public @Nullable MobEffectInstance getHiddenEffect() {
        return this.hiddenEffect;
    }
}
