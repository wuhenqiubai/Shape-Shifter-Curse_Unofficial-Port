package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import java.util.function.Consumer;

/** Runs only from the accepted shield-block branch of damage(), never from a shield query. */
public class ActionOnShieldBlockPower extends Power {
    private final Consumer<Tuple<Entity, Entity>> action;
    public ActionOnShieldBlockPower(PowerType<?> type, LivingEntity entity, Consumer<Tuple<Entity, Entity>> action) {
        super(type, entity);
        this.action = action;
    }
    public static void onBlock(LivingEntity defender, DamageSource source) {
        if (defender.level().isClientSide || !defender.isBlocking() || !source.is(DamageTypeTags.IS_PROJECTILE)
                || source.getEntity() == null || source.getEntity() == defender) return;
        for (var power : PowerHolderComponent.getPowers(defender, ActionOnShieldBlockPower.class)) {
            power.action.accept(new Tuple<>(defender, source.getEntity()));
        }
    }
    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(ShapeShifterCurseFabric.identifier("action_on_shield_block"),
                new SerializableData().add("bientity_action", ApoliDataTypes.BIENTITY_ACTION),
                data -> (type, entity) -> new ActionOnShieldBlockPower(type, entity, data.get("bientity_action"))).allowCondition();
    }
}
