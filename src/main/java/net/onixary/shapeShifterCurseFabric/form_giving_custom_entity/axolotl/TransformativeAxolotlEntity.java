package net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.axolotl;

import net.minecraft.entity.Bucketable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.data.StaticParams;
import net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.ITMob;
import net.onixary.shapeShifterCurseFabric.items.RegCustomItem;
import net.onixary.shapeShifterCurseFabric.status_effects.BaseTransformativeStatusEffect;
import net.onixary.shapeShifterCurseFabric.status_effects.TStatusApplier;

import java.util.Optional;

import static net.onixary.shapeShifterCurseFabric.status_effects.RegTStatusEffect.TO_AXOLOTL_0_EFFECT;

public class TransformativeAxolotlEntity extends AxolotlEntity implements Bucketable, ITMob {

    public TransformativeAxolotlEntity(EntityType<? extends AxolotlEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 6.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, StaticParams.CUSTOM_MOB_DEFAULT_DAMAGE)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 1.0);
    }

    public static boolean canCustomSpawn(EntityType<TransformativeAxolotlEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        // ~100% 刷新成功率
        float Chance = ShapeShifterCurseFabric.commonConfig.transformativeAxolotlSpawnChance;
        if (Chance <= 0.0f) { return false; }
        if (Chance >= 1.0f) { return true; }
        if (random.nextFloat() < Chance) { return true; }
        return canSpawn(type, world, spawnReason, pos, random);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        return Bucketable.tryBucket(player, hand, this).orElse(super.interactMob(player, hand));
    }

    public ItemStack getBucketItem() {
        return new ItemStack(RegCustomItem.TRANSFORMATIVE_AXOLOTL_BUCKET);
    }

    @Override
    public float getStatusChance() {
        return 0.7f;
    }

    @Override
    public BaseTransformativeStatusEffect getStatusEffect() {
        return TO_AXOLOTL_0_EFFECT;
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
        // Axolotl uses Brain AI in 1.21, traditional goals don't work.
        // Manually find nearby players and apply effect.
        if (!this.getWorld().isClient && !this.IsInCooldown()) {
            var players = this.getWorld().getEntitiesByClass(
                PlayerEntity.class,
                this.getBoundingBox().expand(StaticParams.CUSTOM_MOB_DEFAULT_ATTACK_RANGE),
                p -> true
            );
            for (PlayerEntity player : players) {
                TStatusApplier.applyStatusByChance(this.getStatusChance(), player, this.getStatusEffect());
            }
            if (!players.isEmpty()) {
                this.ApplyCooldown();
            }
        }
        this.TickCooldown();
        // Particles
        if (this.getWorld().isClient) {
            for (int i = 0; i < 1; i++) {
                this.getWorld().addParticle(StaticParams.CUSTOM_MOB_DEFAULT_PARTICLE,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                    this.getY() + this.random.nextDouble() * 0.5,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                    0, 0, 0);
            }
        }
    }

    @Override
    public boolean tryAttack(Entity target) {
        Optional<Boolean> attacked = this.TMob_TryAttack(this, target);
        return attacked.orElseGet(() -> super.tryAttack(target));
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }
}
