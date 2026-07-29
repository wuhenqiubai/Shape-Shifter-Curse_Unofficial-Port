package net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.axolotl;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.data.StaticParams;
import net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.ITMob;
import net.onixary.shapeShifterCurseFabric.items.RegCustomItem;
import net.onixary.shapeShifterCurseFabric.status_effects.BaseTransformativeStatusEffect;
import net.onixary.shapeShifterCurseFabric.status_effects.TStatusApplier;

import java.util.Optional;

import static net.onixary.shapeShifterCurseFabric.status_effects.RegTStatusEffect.TO_AXOLOTL_0_EFFECT;

public class TransformativeAxolotlEntity extends Axolotl implements Bucketable, ITMob {

    public TransformativeAxolotlEntity(EntityType<? extends Axolotl> entityType, Level world) {
        super(entityType, world);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.ATTACK_DAMAGE, StaticParams.CUSTOM_MOB_DEFAULT_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 1.0);
    }

    public static boolean canCustomSpawn(EntityType<TransformativeAxolotlEntity> type, ServerLevelAccessor world, EntitySpawnReason spawnReason, BlockPos pos, RandomSource random) {
        // ~100% 刷新成功率
        float Chance = ShapeShifterCurseFabric.commonConfig.transformativeAxolotlSpawnChance;
        if (Chance <= 0.0f) { return false; }
        if (Chance >= 1.0f) { return true; }
        if (random.nextFloat() < Chance) { return true; }
        return checkAxolotlSpawnRules(type, world, spawnReason, pos, random);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        return Bucketable.bucketMobPickup(player, hand, this).orElse(super.mobInteract(player, hand));
    }

    public ItemStack getBucketItemStack() {
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
        if (!this.level().isClientSide() && !this.IsInCooldown()) {
            var players = this.level().getEntitiesOfClass(
                Player.class,
                this.getBoundingBox().inflate(StaticParams.CUSTOM_MOB_DEFAULT_ATTACK_RANGE),
                p -> true
            );
            for (Player player : players) {
                TStatusApplier.applyStatusByChance(this.getStatusChance(), player, this.getStatusEffect());
            }
            if (!players.isEmpty()) {
                this.ApplyCooldown();
            }
        }
        this.TickCooldown();
        // Particles
        if (this.level().isClientSide()) {
            for (int i = 0; i < 1; i++) {
                this.level().addParticle(StaticParams.CUSTOM_MOB_DEFAULT_PARTICLE,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                    this.getY() + this.random.nextDouble() * 0.5,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                    0, 0, 0);
            }
        }
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