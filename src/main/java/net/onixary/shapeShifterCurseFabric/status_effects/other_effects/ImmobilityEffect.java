package net.onixary.shapeShifterCurseFabric.status_effects.other_effects;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class ImmobilityEffect extends MobEffect {
    public ImmobilityEffect() {
        super(MobEffectCategory.HARMFUL, 0x000000); // Color can be changed as needed
    }

    @Override
    public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity entity, int amplifier) {
        if (entity instanceof Player){
            double speedY = entity.getDeltaMovement().y;
            entity.setDeltaMovement(0, speedY, 0);
            entity.hurtMarked = true;
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true; // Apply effect every tick
    }


}
