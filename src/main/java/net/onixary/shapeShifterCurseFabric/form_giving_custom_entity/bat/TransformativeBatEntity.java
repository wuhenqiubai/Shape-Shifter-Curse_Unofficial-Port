package net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.bat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.data.StaticParams;
import net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.ITMob;
import net.onixary.shapeShifterCurseFabric.status_effects.BaseTransformativeStatusEffect;

import java.util.Optional;

import static net.onixary.shapeShifterCurseFabric.status_effects.RegTStatusEffect.TO_BAT_0_EFFECT;

public class TransformativeBatEntity extends Bat implements ITMob {
    public TransformativeBatEntity(EntityType<? extends Bat> entityType, Level world) {
        super(entityType, world);
    }

    public static AttributeSupplier.Builder createAttributes() {
        // 1.21.11: 用 Animal.createAnimalAttributes()（含 TEMPT_RANGE，TemptingSensor 需要）
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.ATTACK_DAMAGE, StaticParams.CUSTOM_MOB_DEFAULT_DAMAGE_OLD)
                .add(Attributes.MOVEMENT_SPEED, 1.0);
    }

    public static boolean canCustomSpawn(EntityType<TransformativeBatEntity> type, LevelAccessor world, EntitySpawnReason spawnReason, BlockPos pos, RandomSource random) {
        if (pos.getY() >= world.getSeaLevel()) {
            return false;
        } else {
            int i = world.getMaxLocalRawBrightness(pos);
            int j = 4;
            float Chance = ShapeShifterCurseFabric.commonConfig.transformativeBatSpawnChance;
            if (Chance <= 0) { return false; }
            if (Chance >= 1) { return true; }
            if (random.nextFloat() > Chance) { return false; }

            return i <= random.nextInt(j) && checkMobSpawnRules(type, world, spawnReason, pos, random);
        }
    }

    @Override
    public float getStatusChance() {
        return 0.5f;
    }

    @Override
    public BaseTransformativeStatusEffect getStatusEffect() {
        return TO_BAT_0_EFFECT;
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
        // 由于大部分变形生物都改了攻击逻辑 所以把这个逻辑放唯一一个没改的蝙蝠代码里
        LivingEntity target = this.getTarget();
        if (target instanceof Player player && !this.IsInCooldown()) {
            double distance = this.distanceToSqr(player);
            if (distance <= StaticParams.CUSTOM_MOB_DEFAULT_ATTACK_RANGE * StaticParams.CUSTOM_MOB_DEFAULT_ATTACK_RANGE) {
                this.doHurtTarget(player);
                ITMob.applyStatusByChance(this.getStatusChance(), player, this.getStatusEffect());
                this.ApplyCooldown();
            }
        }
        this.TMob_Tick(this);
    }

    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
        Optional<Boolean> attacked = this.TMob_TryAttack(this, target);
        return attacked.orElseGet(() -> super.doHurtTarget(serverLevel, target));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
}