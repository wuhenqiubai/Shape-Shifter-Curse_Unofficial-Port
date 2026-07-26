package net.onixary.shapeShifterCurseFabric.minion.mobs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.minion.IMinion;
import net.onixary.shapeShifterCurseFabric.minion.IPlayerEntityMinion;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class AnubisWolfMinionEntity extends Wolf implements IMinion<AnubisWolfMinionEntity> {
    public static final ResourceLocation MinionID = ShapeShifterCurseFabric.identifier("anubis_wolf_minion");

    public AnubisWolfMinionEntity(EntityType<? extends AnubisWolfMinionEntity> entityType, Level world) {
        super(entityType, world);
        this.setPathfindingMalus(PathType.POWDER_SNOW, -1.0F);
        this.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, -1.0F);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SpawnGroupData entityData) {
        AgeableMobGroupData data;
        if (entityData instanceof AgeableMobGroupData passiveData) {
            passiveData.shouldSpawnBaby = false;
            data = passiveData;
        }
        else {
            data = new AgeableMobGroupData(false);
        }
        return super.finalizeSpawn(world, difficulty, spawnReason, data);
    }

    public int MinionLevel = 1;

    public void setMinionLevel(int level) {
        this.MinionLevel = level;
        this.ApplyMinionLevel(true);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WolfMinionEscapeDangerGoal(1.5));
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(6, new FollowOwnerGoalNoTP(this, 1.0, 10.0F, 2.0F, false));
        this.goalSelector.addGoal(7, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractSkeleton.class, false));
    }

    @Override
    public void InitMinion(Player player) {
        if (player instanceof IPlayerEntityMinion iPlayerEntityMinion) {
            iPlayerEntityMinion.shape_shifter_curse$addMinion(this);
        }
        else {
            ShapeShifterCurseFabric.LOGGER.error("PlayerEntity is not IPlayerEntityMinion, It Shouldn't Happen!");
            this.setHealth(0.0f);   // 自动死亡
        }
    }

    @Override
    public UUID getMinionOwnerUUID() {
        return super.getOwnerUUID();
    }

    @Override
    public void setMinionOwnerUUID(UUID uuid) {
        this.setOwnerUUID(uuid);
    }

    @Override
    public void setOwner(Player player) {
            super.getOwner();
    }

    public ResourceLocation getMinionTypeID() {
        return MinionID;
    }

    @Override
    public AnubisWolfMinionEntity getSelf() {
        return this;
    }

    // isUndead() removed in 1.21 → use entity_type tag #minecraft:undead
    // canBreatheInWater() is final in LivingEntity 1.21 → use entity_type tag #minecraft:can_breathe_under_water

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        MobEffect statusEffect = effect.getEffect().value();
        return statusEffect != MobEffects.REGENERATION && statusEffect != MobEffects.POISON;
    }

    public static AttributeSupplier.Builder createWolfMinionAttributes() {
        // 速度0.3
        // 生命10/16/24
        // 攻击2/3/4
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.30000001192092896)
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    private void ApplyMinionLevel(boolean modifyHP) {
        AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance attack_damage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (health == null || attack_damage == null) {
            ShapeShifterCurseFabric.LOGGER.error("wolf minion attribute error");
            return;
        }
        switch (MinionLevel) {
            // 默认参数似乎直接break不会生效，依然要设置下
            case 1:
                health.setBaseValue(10.0d);
                attack_damage.setBaseValue(2.0d);
                break;
            case 2:
                health.setBaseValue(16.0d);
                attack_damage.setBaseValue(3.0d);
                break;
            case 3:
                health.setBaseValue(20.0d);
                attack_damage.setBaseValue(4.0d);
                break;
            default:
                ShapeShifterCurseFabric.LOGGER.error("wolf minion level error");
                break;
        }
        if (modifyHP) {
            this.setHealth((float) health.getValue());
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean IsSuccess = super.doHurtTarget(target);
        LivingEntity Owner = this.getOwner();
        if (Owner == null) {
            return IsSuccess;
        }
        if (IsSuccess) {
            switch (MinionLevel) {
                case 1:
                    break;
                case 2:
                    Owner.heal(1.0f);
                case 3:
                    Owner.heal(2.0f);
                default:
                    break;
            }
        }
        return IsSuccess;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide) {
            if (!this.shouldExist()) {
                this.setHealth(0.0f);  // 自动死亡
            }
            if (!this.hasEffect(MobEffects.WITHER)) {
                this.addEffect(new MobEffectInstance(MobEffects.WITHER, -1, 0));
            }
        }
        super.tick();
    }

    @Override
    public void setLastHurtMob(Entity target) {
        if (target instanceof LivingEntity livingEntity) {
            // 额外加5tick防止效果消失在伤害判定边缘
            livingEntity.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * MinionLevel + 5, 2));
        }
        super.setLastHurtMob(target);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("MinionLevel", this.MinionLevel);
        nbt.putFloat("MinionHealth", this.getHealth());  // 原版Bug不知道什么时候修
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        this.MinionLevel = nbt.getInt("MinionLevel");
        this.ApplyMinionLevel(false);
        this.setHealth(nbt.getFloat("MinionHealth"));
    }

    public double getMinionDisappearRange() {
        return 1024.0d;  // 自动消失距离的二次方 如果不需要这个功能可以填Double.MAX_VALUE 如果没有让召唤物强制传送功能必须要设置一个合理的值 否则召唤物可能会卸载
    }

    public boolean shouldExist() {
        if (this.level().isClientSide) {
            return true;
        }
        if (this.getMinionOwnerUUID() == null) {
            return false;
        }
        Player owner = this.level().getPlayerByUUID(this.getMinionOwnerUUID());
        if (owner == null) {
            return false;
        }
        if (this.distanceToSqr(owner) > this.getMinionDisappearRange()) {
            return false;
        }
        if (owner instanceof IPlayerEntityMinion iPlayerEntityMinion) {
            return iPlayerEntityMinion.shape_shifter_curse$minionExist(this.getMinionTypeID(), this.getUUID());
        }
        return false;
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.VEX_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_SKELETON_DEATH;
    }

    @Override
    public void die(DamageSource source) {
        if (this.getMinionOwnerUUID() != null && this.level().getPlayerByUUID(this.getMinionOwnerUUID()) instanceof IPlayerEntityMinion iPlayerEntityMinion) {
            iPlayerEntityMinion.shape_shifter_curse$removeMinion(this.getMinionTypeID(), this.getUUID());
        }
        // 清除死亡Message
        this.setOwnerUUID(null);
        super.die(source);
    }

    @Override
    public Level level() {
        return super.level();
    }

    class WolfMinionEscapeDangerGoal extends PanicGoal {
        public WolfMinionEscapeDangerGoal(double speed) {
            super(AnubisWolfMinionEntity.this, speed);
        }

        protected boolean shouldPanic() {
            return this.mob.isFreezing() || this.mob.isOnFire();
        }
    }
}