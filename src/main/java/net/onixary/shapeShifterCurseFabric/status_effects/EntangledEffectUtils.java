package net.onixary.shapeShifterCurseFabric.status_effects;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import org.jetbrains.annotations.Nullable;

public class EntangledEffectUtils {
    public static final int ENTANGLED_DURATION_PER_LEVEL = 20 * 5;
    public static final int ENTANGLED_MAX_LEVEL = 4;
    public static final int ENTANGLED_FULL_DURATION = 20 * 15;
    // PVP考虑，对玩家裹茧效果的时长缩短
    public static final int ENTANGLED_FULL_DURATION_PLAYER = 20 * 5;

    public static void applyEntangledEffect(@Nullable Entity owner, LivingEntity target, int Time) {
        RegistryEntry<net.minecraft.entity.effect.StatusEffect> entangledEffectType =
                Registries.STATUS_EFFECT.getEntry(RegOtherStatusEffects.ENTANGLED_EFFECT);
        RegistryEntry<net.minecraft.entity.effect.StatusEffect> entangledFullType =
                Registries.STATUS_EFFECT.getEntry(RegOtherStatusEffects.ENTANGLED_FULL_EFFECT);
        if (target.getWorld().isClient) {
            return;
        }
        if (target.getStatusEffect(entangledFullType) != null) {
            return;
        }
        StatusEffectInstance entangledEffect = target.getStatusEffect(entangledEffectType);
        if (entangledEffect == null) {
            target.addStatusEffect(new StatusEffectInstance(entangledEffectType, Time, Time / ENTANGLED_DURATION_PER_LEVEL));
        } else {
            int newDuration = entangledEffect.getDuration() + Time;
            int newAmplifier = Math.min(entangledEffect.getAmplifier() + 1, ENTANGLED_MAX_LEVEL);
            target.removeStatusEffect(entangledEffectType);
            target.addStatusEffect(new StatusEffectInstance(entangledEffectType, newDuration, newAmplifier));
        }
        entangledEffect = target.getStatusEffect(entangledEffectType);
        if (entangledEffect != null) {
            int NowDuration = entangledEffect.getDuration();
            if (NowDuration >= ENTANGLED_DURATION_PER_LEVEL * (ENTANGLED_MAX_LEVEL + 1)) {
                target.removeStatusEffect(entangledEffectType);
                Identifier targetID = Registries.ENTITY_TYPE.getId(target.getType());
                if (owner instanceof ServerPlayerEntity player) {
                    ShapeShifterCurseFabric.ON_WEB_ENTITY.trigger(player, targetID);
                }
                if(target instanceof PlayerEntity){
                    target.addStatusEffect(new StatusEffectInstance(entangledFullType, ENTANGLED_FULL_DURATION_PLAYER, 0));
                }
                else{
                    target.addStatusEffect(new StatusEffectInstance(entangledFullType, ENTANGLED_FULL_DURATION, 0));
                }
            }
        }
    }
}
