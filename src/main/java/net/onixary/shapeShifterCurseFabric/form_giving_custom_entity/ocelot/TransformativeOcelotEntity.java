package net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.ocelot;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.additional_power.AdditionalPowers;
import net.onixary.shapeShifterCurseFabric.data.StaticParams;
import net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.ITMob;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.status_effects.BaseTransformativeStatusEffect;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static net.onixary.shapeShifterCurseFabric.status_effects.RegTStatusEffect.TO_OCELOT_0_EFFECT;

public class TransformativeOcelotEntity extends Ocelot implements ITMob {
    public FleeGoalModified<PlayerEntity> fleeGoal;

    public TransformativeOcelotEntity(EntityType<? extends Ocelot> entityType, Level world) {
        super(entityType, world);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.ATTACK_DAMAGE, StaticParams.CUSTOM_MOB_DEFAULT_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    public static boolean canCustomSpawn(EntityType<TransformativeOcelotEntity> type, LevelAccessor world, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        float Chance = ShapeShifterCurseFabric.commonConfig.transformativeOcelotSpawnChance;
        if (Chance <= 0.0f) { return false; }
        if (Chance >= 1.0f) { return true; }
        return random.nextFloat() < Chance;
    }

    @Override
    public float getStatusChance() {
        return 0.5f;
    }

    @Override
    public BaseTransformativeStatusEffect getStatusEffect() {
        return TO_OCELOT_0_EFFECT;
    }

    @Override
    public void tick() {
        super.tick();
        this.TMob_Tick(this);
    }

    @Override
    public void applyDamageEffects(LivingEntity attacker, Entity target) {
        // 在applyStatusByChance里面已经判断形态了 无需在外面判断
        if (target instanceof PlayerEntity player) {
            ITMob.applyStatusByChance(this.getStatusChance(), player, this.getStatusEffect());
        }
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean bl = super.tryAttack(target);
        if (bl) {
            this.applyDamageEffects(this, target);
        }
        return bl;
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false, entity -> entity instanceof PlayerEntity player && RegPlayerForms.ORIGINAL_SHIFTER.isPlayerForm(player)));
    }

    public static final Predicate<LivingEntity> FLEE_PREDICATE = (entity) -> {
        if (entity instanceof PlayerEntity player) {
            if (RegPlayerForms.ORIGINAL_SHIFTER.isPlayerForm(player)) {
                return false;
            }
            if (AdditionalPowers.CAT_FRIENDLY.isActive(player)) {
                return false;
            }
        }
        return true;
    };

    @Override
    protected void updateFleeing() {
        if (this.fleeGoal == null) {
            this.fleeGoal = new FleeGoalModified<PlayerEntity>(this, PlayerEntity.class, 16.0F, 0.8, 1.33, FLEE_PREDICATE);
        }

        this.goalSelector.remove(this.fleeGoal);
        if (!this.isTrusting()) {
            this.goalSelector.add(4, this.fleeGoal);
        }
    }

    public static class FleeGoalModified<T extends LivingEntity> extends FleeEntityGoal<T> {
        private final TransformativeOcelotEntity ocelot;

        public FleeGoalModified(TransformativeOcelotEntity ocelot, Class<T> fleeFromType, float distance, double slowSpeed, double fastSpeed, Predicate<LivingEntity> predicate) {
            super(ocelot, fleeFromType, distance, slowSpeed, fastSpeed, predicate);
            this.ocelot = ocelot;
        }

        public boolean canStart() {
            return !this.ocelot.isTrusting() && super.canStart();
        }

        public boolean shouldContinue() {
            return !this.ocelot.isTrusting() && super.shouldContinue();
        }
    }
}
