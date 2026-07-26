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
import net.onixary.shapeShifterCurseFabric.data.StaticParams;
import net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.ITMob;
import net.onixary.shapeShifterCurseFabric.status_effects.BaseTransformativeStatusEffect;

import java.util.Optional;

import static net.onixary.shapeShifterCurseFabric.status_effects.RegTStatusEffect.TO_OCELOT_0_EFFECT;

public class TransformativeOcelotEntity extends Ocelot implements ITMob {
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

    private int cooldown = 0;

    @Override
    public void TickCooldown() {
        if (this.cooldown > 0) {
            this.cooldown --;
        }
    }

    @Override
    public void ApplyCooldown() {
        this.cooldown = 100;
    }

    @Override
    public boolean IsInCooldown() {
        return this.cooldown > 0;
    }

    @Override
    public void tick() {
        super.tick();
        this.TMob_Tick(this);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        Optional<Boolean> attacked = this.TMob_TryAttack(this, target);
        return attacked.orElseGet(() -> super.doHurtTarget(target));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));

        // this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true, livingEntity -> {
        //     if (livingEntity instanceof PlayerEntity player) {
        //         return RegPlayerForms.ORIGINAL_SHIFTER.isPlayerForm(player);
        //     }
        //     return false;
        // }));

        // this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true, livingEntity -> {
        //     if (livingEntity instanceof PlayerEntity player) {
        //         return !AdditionalPowers.CAT_FRIENDLY.isActive(player);
        //     }
        //     return true;
        // }));
    }
}
