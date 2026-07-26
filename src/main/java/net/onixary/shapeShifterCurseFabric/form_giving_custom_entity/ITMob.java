package net.onixary.shapeShifterCurseFabric.form_giving_custom_entity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.data.StaticParams;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;
import net.onixary.shapeShifterCurseFabric.status_effects.BaseTransformativeStatusEffect;
import net.onixary.shapeShifterCurseFabric.status_effects.attachment.EffectManager;
import net.onixary.shapeShifterCurseFabric.status_effects.transformative_effects.TransformativeStatusInstance;

import java.util.Optional;

public interface ITMob {
    public float getStatusChance();
    public BaseTransformativeStatusEffect getStatusEffect();
    public void TickCooldown();
    public void ApplyCooldown();
    public boolean IsInCooldown();

    public default void TMob_Tick(Mob TMob) {
        TickCooldown();

        LivingEntity target = TMob.getTarget();
        if (target instanceof Player && !this.IsInCooldown()) {
            Player player = (Player) target;

			double distance = TMob.distanceToSqr(player);
            if (distance <= StaticParams.CUSTOM_MOB_DEFAULT_ATTACK_RANGE * StaticParams.CUSTOM_MOB_DEFAULT_ATTACK_RANGE) {
                TMob.doHurtTarget(player);
                applyStatusByChance(this.getStatusChance(), player, this.getStatusEffect());
                this.ApplyCooldown();
            }
        }

        // 生成粒子效果
        if (TMob.level().isClientSide) {
            for (int i = 0; i < 1; i++) {
                TMob.level().addParticle(StaticParams.CUSTOM_MOB_DEFAULT_PARTICLE,
                        TMob.getX() + (TMob.getRandom().nextDouble() - 0.5) * 0.5,
                        TMob.getY() + TMob.getRandom().nextDouble() * 0.5,
                        TMob.getZ() + (TMob.getRandom().nextDouble() - 0.5) * 0.5,
                        0, 0, 0);
            }
        }
    }

    public default Optional<Boolean> TMob_TryAttack(Mob TMob, Entity target) {
        if(target instanceof Player player) {
            IForm currentForm = FormUtils.getPlayerForm(player);
            if (currentForm.isEquals(RegPlayerForms.ORIGINAL_SHIFTER)) {
                boolean attacked = target.hurt(TMob.damageSources().mobAttack(TMob), (float)TMob.getAttributeValue(Attributes.ATTACK_DAMAGE));
                if (attacked) {
                    TMob.setLastHurtMob(TMob);
                }
                return Optional.of(attacked);
            }
            return Optional.of(false);
        }
        return Optional.empty();
    }

    public static void applyStatusByChance(float chance, Player player, BaseTransformativeStatusEffect regStatusEffect) {
        if (player instanceof ServerPlayer playerEntity) {
            TransformativeStatusInstance instance = EffectManager.getTransformativeEffect(playerEntity);
            if (instance == null || instance.getTransformativeEffectType() == null || !instance.getTransformativeEffectType().getToForm(player).isEquals(regStatusEffect.getToForm(player))) {  // 如果当前效果的形态与regStatusEffect不同
                if (Math.random() < chance && RegPlayerForms.ORIGINAL_SHIFTER.isPlayerForm(player)) {
                    EffectManager.overrideEffect(player, regStatusEffect);
                }
            }
        }
	}
}