package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.ClimbingPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.util.function.Predicate;

public class ClimbingEXPower extends ClimbingPower {
    public final Predicate<Entity> startClimbCondition;
    public final Predicate<Entity> continueClimbCondition;
    public final Predicate<Entity> holdingCondition;
    public final boolean allowHolding;

    public boolean lastIsClimbing = false;

    public ClimbingEXPower(PowerType<?> type, LivingEntity entity, Predicate<Entity> startClimbCondition, Predicate<Entity> continueClimbCondition, Predicate<Entity> holdingCondition, boolean allowHolding) {
        super(type, entity, allowHolding, holdingCondition);
        this.startClimbCondition = startClimbCondition;
        this.continueClimbCondition = continueClimbCondition;
        this.holdingCondition = holdingCondition;
        this.allowHolding = allowHolding;
        this.setTicking();
    }

    public boolean canHold() {
        if (!this.allowHolding) {
            return false;
        }
        if (this.holdingCondition != null) {
            return this.holdingCondition.test(this.entity);
        }
        // 1.21.11 修复：holdingCondition 为 null 时不能只看 isShiftKeyDown——
        // Apoli 的 LivingEntityMixin.doSpiderClimbing 用 canHold() 判定攀爬，任何 shift（含空中）都会触发
        // 攀爬 → 玩家在空中按 shift 悬停。改为复用 isActive()（start/continue 条件 = 贴住可攀爬面）判定。
        return this.isActive();
    }

    @Override
    public void tick() {
        if (this.isActive()) {
            entity.fallDistance = 0;
        }
    }

    @Override
    public boolean isActive() {
        if (!super.isActive()) {
            return false;
        }
        // 注：不能用 horizontalCollision 区分「贴墙爬墙」与「开阔地浮空」——爬墙（竖直移动）时 horizontalCollision 为 false，
        // 会误伤爬墙悬停。真正的区分靠 start/continue 条件（start 要求 collided_horizontally 贴墙；
        // continue 要求不在地面 + 旁边有方块）。开阔地（无墙）start/continue 均 false → 不激活。
        boolean active;
        if (lastIsClimbing) {
            active = continueClimbCondition == null || continueClimbCondition.test(this.entity);
        } else {
            active = startClimbCondition == null || startClimbCondition.test(this.entity);
        }
        lastIsClimbing = active;
        return active;
    }

    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(
                ShapeShifterCurseFabric.identifier("climbing_ex"),
                new SerializableData()
                        .add("start_climb_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                        .add("continue_climb_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                        .add("holding_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                        .add("allow_holding", SerializableDataTypes.BOOLEAN, true),
                data -> (type, entity) -> new ClimbingEXPower(type, entity, data.get("start_climb_condition"), data.get("continue_climb_condition"), data.get("holding_condition"), data.get("allow_holding"))
        ).allowCondition();
    }
}