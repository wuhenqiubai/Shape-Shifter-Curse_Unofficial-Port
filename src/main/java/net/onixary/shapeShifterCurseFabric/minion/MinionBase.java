package net.onixary.shapeShifterCurseFabric.minion;

import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

// 推荐直接继承MinionBase而非单独实现IMinion接口

public abstract class MinionBase extends TamableAnimal implements IMinion<MinionBase> {
    public MinionBase(EntityType<? extends MinionBase> entityType, Level world) {
        super(entityType, world);
    }

    public void InitMinion(Player player) {
        if (player instanceof IPlayerEntityMinion iPlayerEntityMinion) {
            iPlayerEntityMinion.shape_shifter_curse$addMinion(this);
        }
        else {
            ShapeShifterCurseFabric.LOGGER.error("PlayerEntity is not IPlayerEntityMinion, It Shouldn't Happen!");
            this.setHealth(0.0f);   // 自动死亡
        }
    }

    public Identifier minionTypeID;

    @Override
    public Identifier getMinionTypeID() {
        return this.minionTypeID;
    }

    @Override
    public UUID getMinionOwnerUUID() {
        // getOwnerUUID() 在 1.21.11 移除，改用 getOwner() 实体引用
        net.minecraft.world.entity.LivingEntity owner = this.getOwner();
        return owner != null ? owner.getUUID() : null;
    }

    @Override
    public void setMinionOwnerUUID(UUID uuid) {
        // setOwnerUUID() 在 1.21.11 移除，改为 getOwner()/setOwner() 实体引用系统
    }

    @Override
    public MinionBase getSelf() {
        return this;
    }

    @Override
    public Level level() {
        return super.level();
    }

    public double getMinionDisappearRange() {
        return 1024.0d;  // 32格外自动消失 如果不需要这个功能可以填Double.MAX_VALUE 如果没有让召唤物强制传送功能必须要设置一个合理的值 否则召唤物可能会卸载
    }

    public boolean shouldExist() {
        if (this.level().isClientSide()) {
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

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
    }

    @Override
    public void tame(Player player) {
        super.tame(player);
    }

    @Override
    public void addAdditionalSaveData(@NonNull ValueOutput nbt) {
        super.addAdditionalSaveData(nbt);
    }

    @Override
    public void readAdditionalSaveData(@NonNull ValueInput nbt) {
        super.readAdditionalSaveData(nbt);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.shouldExist()) {
            this.setHealth(0.0f);  // 自动死亡
        }
    }

    // 从玩家召唤物列表中移除
    @Override
    public void die(DamageSource source) {
        if (this.getMinionOwnerUUID() != null && this.level().getPlayerByUUID(this.getMinionOwnerUUID()) instanceof IPlayerEntityMinion iPlayerEntityMinion) {
            iPlayerEntityMinion.shape_shifter_curse$removeMinion(this.getMinionTypeID(), this.getUUID());
        }
        this.tame(null);
        super.die(source);
    }
}