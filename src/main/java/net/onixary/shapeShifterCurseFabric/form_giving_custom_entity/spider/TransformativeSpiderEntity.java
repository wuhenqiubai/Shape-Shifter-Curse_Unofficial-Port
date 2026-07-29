package net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.spider;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.data.StaticParams;
import net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.ITMob;
import net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.ocelot.TransformativeOcelotEntity;
import net.onixary.shapeShifterCurseFabric.status_effects.BaseTransformativeStatusEffect;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

import static net.onixary.shapeShifterCurseFabric.status_effects.RegTStatusEffect.TO_SPIDER_0_EFFECT;

public class TransformativeSpiderEntity extends Spider implements ITMob {
    public TransformativeSpiderEntity(EntityType<? extends Spider> entityType, Level world) {
        super(entityType, world);
    }

    public static AttributeSupplier.@NonNull Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0f)
                .add(Attributes.ATTACK_DAMAGE, StaticParams.CUSTOM_MOB_DEFAULT_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 0.3f);
    }

    public static boolean canCustomSpawn(EntityType<TransformativeOcelotEntity> type, LevelAccessor world, EntitySpawnReason spawnReason, BlockPos pos, RandomSource random) {
        float Chance = ShapeShifterCurseFabric.commonConfig.transformativeSpiderSpawnChance;
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
        return TO_SPIDER_0_EFFECT;
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
    public boolean doHurtTarget(@NonNull ServerLevel serverLevel, @NonNull Entity target) {
        Optional<Boolean> attacked = this.TMob_TryAttack(this, target);
        return attacked.orElseGet(() -> super.doHurtTarget(serverLevel, target));
    }

    @Override
    public @NonNull EntityDimensions getDefaultDimensions(@NonNull Pose pose) {
        return EntityDimensions.fixed(0.7f, 0.45f);
    }
}