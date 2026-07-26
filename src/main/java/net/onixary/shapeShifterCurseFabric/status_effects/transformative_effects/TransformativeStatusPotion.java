package net.onixary.shapeShifterCurseFabric.status_effects.transformative_effects;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.onixary.shapeShifterCurseFabric.status_effects.BaseTransformativeStatusEffect;
import net.onixary.shapeShifterCurseFabric.status_effects.attachment.EffectManager;
import org.jetbrains.annotations.Nullable;

public class TransformativeStatusPotion extends MobEffect {
    public BaseTransformativeStatusEffect TransformativeStatusEffect;

    public TransformativeStatusPotion(BaseTransformativeStatusEffect TransformativeStatusEffect) {
        super(MobEffectCategory.NEUTRAL, 0xFFFFFF);
        this.TransformativeStatusEffect = TransformativeStatusEffect;
    }

    @Override
    public boolean isInstantenous() {
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration >= 1;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        this.applyInstantenousEffect(null, null, entity, amplifier, 0);
        entity.removeEffect(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(this));
        return true;
    }

    @Override
    public void applyInstantenousEffect(@Nullable Entity source, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity) {
        if (!target.level().isClientSide() && target instanceof ServerPlayer player) {
            TransformativeStatusInstance instance = EffectManager.getTransformativeEffect(player);
            if (instance == null || instance.getTransformativeEffectType() == null || !instance.getTransformativeEffectType().getToForm(player).equals(this.TransformativeStatusEffect.getToForm(player))) {  // 如果当前效果的形态与regStatusEffect不同
                if (EffectManager.playerCanHaveTransformativeEffect(player)) {
                    EffectManager.overrideEffect(player, this.TransformativeStatusEffect);
                }
            }
        }
    }
}
