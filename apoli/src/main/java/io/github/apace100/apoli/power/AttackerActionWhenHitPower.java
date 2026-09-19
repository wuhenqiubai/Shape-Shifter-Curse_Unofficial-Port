package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class AttackerActionWhenHitPower extends CooldownPower {

    private final Predicate<Tuple<DamageSource, Float>> damageCondition;
    private final Consumer<Entity> entityAction;

    public AttackerActionWhenHitPower(PowerType<?> type, LivingEntity entity, int cooldownDuration, HudRender hudRender, Predicate<Tuple<DamageSource, Float>> damageCondition, Consumer<Entity> entityAction) {
        super(type, entity, cooldownDuration, hudRender);
        this.damageCondition = damageCondition;
        this.entityAction = entityAction;
    }

    public void whenHit(DamageSource damageSource, float damageAmount) {
        if(damageSource.getEntity() != null && damageSource.getEntity() != entity) {
            if(damageCondition == null || damageCondition.test(new Tuple<>(damageSource, damageAmount))) {
                if(canUse()) {
                    this.entityAction.accept(damageSource.getEntity());
                    use();
                }
            }
        }
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("attacker_action_when_hit"),
            new SerializableData()
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION)
                .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION, null)
                .add("cooldown", SerializableDataTypes.INT, 1)
                .add("hud_render", ApoliDataTypes.HUD_RENDER, HudRender.DONT_RENDER),
            data ->
                (type, player) -> new AttackerActionWhenHitPower(type, player, data.getInt("cooldown"),
                    (HudRender)data.get("hud_render"), (ConditionFactory<Tuple<DamageSource, Float>>.Instance)data.get("damage_condition"),
                    (ActionFactory<Entity>.Instance)data.get("entity_action")))
            .allowCondition();
    }
}
