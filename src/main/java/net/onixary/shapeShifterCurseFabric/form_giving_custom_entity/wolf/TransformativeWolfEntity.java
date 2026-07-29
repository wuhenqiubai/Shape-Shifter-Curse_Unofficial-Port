package net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.wolf;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootTable;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.additional_power.TWolfFriendlyPower;
import net.onixary.shapeShifterCurseFabric.data.StaticParams;
import net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.ITMob;
import net.onixary.shapeShifterCurseFabric.status_effects.BaseTransformativeStatusEffect;
import net.onixary.shapeShifterCurseFabric.status_effects.TStatusApplier;
import org.jetbrains.annotations.Nullable;

import static net.onixary.shapeShifterCurseFabric.status_effects.RegTStatusEffect.TO_ANUBIS_WOLF_0_EFFECT;

public class TransformativeWolfEntity extends Wolf implements ITMob {
    public TransformativeWolfEntity(EntityType<? extends Wolf> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData entityData) {
        return super.finalizeSpawn(world, difficulty, spawnReason, entityData);
    }

    private float cooldown = 0;

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.AvoidEntityGoal<>(this, Llama.class, 24.0F, 1.5, 1.5));
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(6, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new BegGoal(this, 8.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, (new HurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(3, new NonTameRandomTargetGoal<>(this, Turtle.class, false, null));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractSkeleton.class, false));
        this.targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // 我觉得把逆天攻击距离移除之后 攻击力高点也没多大问题
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0f)
                .add(Attributes.MOVEMENT_SPEED, 0.2);
    }

    public static boolean canCustomSpawn(EntityType<TransformativeWolfEntity> type, LevelAccessor world, EntitySpawnReason spawnReason, BlockPos pos, RandomSource random) {
        BlockPos NowCheckPos = pos;
        // 脚下如果藏有TNT 则不生成 防止沙漠神殿自爆
        for (int i = 0; i < 5; i++) {
            if (world.getBlockState(NowCheckPos).getBlock() == Blocks.TNT) {
                return false;
            }
            NowCheckPos = NowCheckPos.below();
        }
        float Chance = ShapeShifterCurseFabric.commonConfig.transformativeWolfSpawnChance;
        if (Chance <= 0.0f) { return false; }
        if (Chance >= 1.0f) { return true; }
        return random.nextFloat() < Chance;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (target instanceof Player playerEntity) {
            if (PowerHolderComponent.hasPower(playerEntity, TWolfFriendlyPower.class)) {
                return false;
            }
        }
        return super.canAttack(target);
    }

    @Override
    public void tick() {
        super.tick();
        // 更新冷却时间
        if (cooldown > 0) {
            cooldown--;
        }

        // 生成粒子效果
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
    public void setLastHurtMob(Entity target) {
        // 在applyStatusByChance里面已经判断形态了 无需在外面判断
        if (target instanceof Player player) {
            TStatusApplier.applyStatusByChance(this.getStatusChance(), player, this.getStatusEffect());
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
        if(target instanceof Player) {
            this.setLastHurtMob(target);
            target.hurt(this.damageSources().mobAttack(this), (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE));
            return true;
        }
        return super.doHurtTarget(serverLevel, target);
    }

    // 禁止与此生物交互 防止使用Wolf的驯服逻辑
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public void tame(Player player) {
        return;
    }

    public ResourceKey<LootTable> getLootTableKey() {
        return ResourceKey.create(
                Registries.LOOT_TABLE,
                Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "entities/t_wolf")
        );
    }

    @Override
    public void setTame(boolean tamed, boolean updateAttributes) {
    }

    @Override
    public float getStatusChance() {
        return 0.5f;
    }

    @Override
    public BaseTransformativeStatusEffect getStatusEffect() {
        return TO_ANUBIS_WOLF_0_EFFECT;
    }

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
}