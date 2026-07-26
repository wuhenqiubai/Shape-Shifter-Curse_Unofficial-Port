package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

public class VirtualShieldPower extends Power {
    private final ConditionFactory<Entity>.Instance activeShieldCondition;
    private final ActionFactory<Entity>.Instance takenDamageAction;
    private final ActionFactory<Entity>.Instance normalDamageAction;
    private final ActionFactory<Entity>.Instance shieldBreakAction;

    public VirtualShieldPower(PowerType<?> type, LivingEntity entity, SerializableData.Instance data) {
        super(type, entity);
        this.activeShieldCondition = data.get("active_shield_condition");
        this.takenDamageAction = data.get("taken_damage_action");
        this.normalDamageAction = data.get("normal_damage_action");
        this.shieldBreakAction = data.get("shield_break_action");
    }

    public boolean blockDamage(DamageSource source) {
        Vec3 vec3d;
        AbstractArrow persistentProjectileEntity;
        Entity attacker = source.getDirectEntity();
        boolean bl = attacker instanceof AbstractArrow && (persistentProjectileEntity = (AbstractArrow) attacker).getPierceLevel() > 0;
	    if (!source.is(DamageTypeTags.BYPASSES_SHIELD) && (this.activeShieldCondition == null || this.activeShieldCondition.test(this.entity)) && !bl && (vec3d = source.getSourcePosition()) != null) {
            Vec3 vec3d2 = this.entity.getViewVector(1.0f);
            Vec3 vec3d3 = vec3d.vectorTo(this.entity.position()).normalize();
            vec3d3 = new Vec3(vec3d3.x, 0.0, vec3d3.z);
            if (vec3d3.dot(vec3d2) < 0.0) {
                if (this.takenDamageAction != null) {
                    this.takenDamageAction.accept(this.entity);
                }
                if (attacker instanceof LivingEntity livingEntity && livingEntity.canDisableShield()) {
                    if (this.shieldBreakAction != null) {
                        this.shieldBreakAction.accept(this.entity);
                    }
                } else {
                    if (this.normalDamageAction != null) {
                        this.normalDamageAction.accept(this.entity);
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(
                ShapeShifterCurseFabric.identifier("virtual_shield"),
                new SerializableData()
                        .add("active_shield_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                        .add("taken_damage_action", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("normal_damage_action", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("shield_break_action", ApoliDataTypes.ENTITY_ACTION, null),
                data -> (powerType, entity) -> new VirtualShieldPower(powerType, entity, data)
        );
    }
}
