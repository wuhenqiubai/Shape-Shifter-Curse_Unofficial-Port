package net.onixary.shapeShifterCurseFabric.status_effects;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import org.jetbrains.annotations.Nullable;

public class EntangledEffectUtils {
    public static final int ENTANGLED_DURATION_PER_LEVEL = 20 * 5;
    public static final int ENTANGLED_MAX_LEVEL = 4;
    public static final int ENTANGLED_FULL_DURATION = 20 * 15;
    // PVP考虑，对玩家裹茧效果的时长缩短
    public static final int ENTANGLED_FULL_DURATION_PLAYER = 20 * 5;

    public static void applyEntangledEffect(@Nullable Entity owner, LivingEntity target, int Time) {
        Holder<net.minecraft.world.effect.MobEffect> entangledEffectType =
                BuiltInRegistries.MOB_EFFECT.wrapAsHolder(RegOtherStatusEffects.ENTANGLED_EFFECT);
        Holder<net.minecraft.world.effect.MobEffect> entangledFullType =
                BuiltInRegistries.MOB_EFFECT.wrapAsHolder(RegOtherStatusEffects.ENTANGLED_FULL_EFFECT);
        if (target.level().isClientSide()) {
            return;
        }
        if (target.getEffect(entangledFullType) != null) {
            return;
        }
        MobEffectInstance entangledEffect = target.getEffect(entangledEffectType);
        if (entangledEffect == null) {
            target.addEffect(new MobEffectInstance(entangledEffectType, Time, Time / ENTANGLED_DURATION_PER_LEVEL));
        } else {
            int newDuration = entangledEffect.getDuration() + Time;
            int newAmplifier = Math.min(entangledEffect.getAmplifier() + 1, ENTANGLED_MAX_LEVEL);
            target.removeEffect(entangledEffectType);
            target.addEffect(new MobEffectInstance(entangledEffectType, newDuration, newAmplifier));
        }
        entangledEffect = target.getEffect(entangledEffectType);
        if (entangledEffect != null) {
            int NowDuration = entangledEffect.getDuration();
            if (NowDuration >= ENTANGLED_DURATION_PER_LEVEL * (ENTANGLED_MAX_LEVEL + 1)) {
                target.removeEffect(entangledEffectType);
                Identifier targetID = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
                if (owner instanceof ServerPlayer player) {
                    ShapeShifterCurseFabric.ON_WEB_ENTITY.trigger(player, targetID);
                }
                if(target instanceof Player){
                    target.addEffect(new MobEffectInstance(entangledFullType, ENTANGLED_FULL_DURATION_PLAYER, 0));
                }
                else{
                    target.addEffect(new MobEffectInstance(entangledFullType, ENTANGLED_FULL_DURATION, 0));
                }
            }
        }
    }
}