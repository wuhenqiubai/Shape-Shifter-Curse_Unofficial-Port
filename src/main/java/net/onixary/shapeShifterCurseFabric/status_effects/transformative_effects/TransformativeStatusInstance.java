package net.onixary.shapeShifterCurseFabric.status_effects.transformative_effects;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.status_effects.BaseTransformativeStatusEffect;
import org.jetbrains.annotations.Nullable;

// 这个Instance仅出现在服务器端 客户端为StatusEffectInstance
public class TransformativeStatusInstance extends MobEffectInstance {

    public TransformativeStatusInstance(BaseTransformativeStatusEffect effect, int duration) {
        super(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect), duration, 0, false, false, true);
    }

    @Override
    public boolean tick(LivingEntity entity, Runnable overwriteCallback) {
        if (entity instanceof ServerPlayer player && this.getDuration() <= 1) {
            ShapeShifterCurseFabric.ON_TRANSFORM_EFFECT_FADE.trigger(player);
        }
        return super.tick(entity, overwriteCallback);
    }

    public void ActiveEffect(ServerPlayer player) {
        BaseTransformativeStatusEffect effect = this.getTransformativeEffectType();
        if (effect != null) {
            effect.ActiveEffect(player);
        }
    }

    public static @Nullable TransformativeStatusInstance formStatusEffectInstance(MobEffectInstance instance) {
        Holder<MobEffect> entry = instance.getEffect();
        MobEffect effect = entry.value();
        if (effect instanceof BaseTransformativeStatusEffect baseTransformativeStatusEffect) {
            return new TransformativeStatusInstance(baseTransformativeStatusEffect, instance.getDuration());
        }
        return null;
    }

    public @Nullable BaseTransformativeStatusEffect getTransformativeEffectType() {
        if (super.getEffect().value() instanceof BaseTransformativeStatusEffect baseTransformativeStatusEffect) {
            return baseTransformativeStatusEffect;
        }
        return null;
    }
}
