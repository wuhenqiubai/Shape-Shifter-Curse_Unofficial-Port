package net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.ocelot;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.additional_power.AdditionalPowers;
import net.onixary.shapeShifterCurseFabric.data.StaticParams;
import net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.ITMob;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.status_effects.BaseTransformativeStatusEffect;

import java.util.function.Predicate;

import static net.onixary.shapeShifterCurseFabric.status_effects.RegTStatusEffect.TO_OCELOT_0_EFFECT;

public class TransformativeOcelotEntity extends Ocelot implements ITMob {
    public FleeGoalModified<Player> fleeGoal;

    public TransformativeOcelotEntity(EntityType<? extends Ocelot> entityType, Level world) {
        super(entityType, world);
    }

    public static AttributeSupplier.Builder createAttributes() {
        // 1.21.11: 用 Animal.createAnimalAttributes()（含 TEMPT_RANGE，TemptingSensor 需要）
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.ATTACK_DAMAGE, StaticParams.CUSTOM_MOB_DEFAULT_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    public static boolean canCustomSpawn(EntityType<TransformativeOcelotEntity> type, LevelAccessor world, EntitySpawnReason spawnReason, BlockPos pos, RandomSource random) {
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

    public void applyDamageEffects(LivingEntity attacker, Entity target) {
        // 在applyStatusByChance里面已经判断形态了 无需在外面判断
        if (target instanceof Player player) {
            ITMob.applyStatusByChance(this.getStatusChance(), player, this.getStatusEffect());
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
        boolean bl = super.doHurtTarget(serverLevel, target);
        if (bl) {
            this.applyDamageEffects(this, target);
        }
        return bl;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, (entity, level) -> entity instanceof Player player && RegPlayerForms.ORIGINAL_SHIFTER.isPlayerForm(player)));
    }

    public static final Predicate<LivingEntity> FLEE_PREDICATE = (entity) -> {
        if (entity instanceof Player player) {
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
    protected void reassessTrustingGoals() {
        if (this.fleeGoal == null) {
            this.fleeGoal = new FleeGoalModified<>(this, Player.class, 16.0F, 0.8, 1.33, FLEE_PREDICATE);
        }

        this.goalSelector.removeGoal(this.fleeGoal);
        if (!this.isTrusting()) {
            this.goalSelector.addGoal(4, this.fleeGoal);
        }
    }

    public static class FleeGoalModified<T extends LivingEntity> extends AvoidEntityGoal<T> {
        private final TransformativeOcelotEntity ocelot;

        public FleeGoalModified(TransformativeOcelotEntity ocelot, Class<T> fleeFromType, float distance, double slowSpeed, double fastSpeed, Predicate<LivingEntity> predicate) {
            super(ocelot, fleeFromType, distance, slowSpeed, fastSpeed, predicate);
            this.ocelot = ocelot;
        }

        @Override
        public boolean canUse() {
            return !this.ocelot.isTrusting() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.ocelot.isTrusting() && super.canContinueToUse();
        }
    }
}