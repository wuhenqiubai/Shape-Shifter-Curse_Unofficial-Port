package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.core.Registry;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;

public class AdditionalEntityActions {
    public static void register() {
        registerAction(AddInstinctAction.getFactory());
        registerAction(SetFallingDistanceAction.createFactory());
        TransformAction.registerAction(AdditionalEntityActions::registerAction, AdditionalEntityActions::registerBIAction);
        registerAction(ExplosionDamageEntityAction.createFactory());
        registerAction(SummonMinionWolfNearbyAction.createFactory());
        registerBIAction(SummonMinionWolfNearbyAction.createBIFactory());
        PlayPowerAnimationAction.register(AdditionalEntityActions::registerAction, AdditionalEntityActions::registerBIAction);
        TrinketsConditionAction.registerAction(AdditionalEntityActions::registerAction, AdditionalEntityActions::registerBIAction);
        ManaUtilsApoli.registerAction(AdditionalEntityActions::registerAction, AdditionalEntityActions::registerBIAction);
        FireArrowAction.registerAction(AdditionalEntityActions::registerAction, AdditionalEntityActions::registerBIAction);
        registerAction(SpawnParticlesInCircleAction.getFactory());
        WebBridgeAction.registerAction(AdditionalEntityActions::registerAction, AdditionalEntityActions::registerBIAction);
        ItemStorePower.registerAction(AdditionalEntityActions::registerAction, AdditionalEntityActions::registerBIAction);
        ItemCooldownCA.registerAction(AdditionalEntityActions::registerAction, AdditionalEntityActions::registerBIAction);
        registerAction(TANAddThirstAction.createFactory());
    }

    public static ActionFactory<Entity> registerAction(ActionFactory<Entity> actionFactory) {
        return Registry.register(ApoliRegistries.ENTITY_ACTION, actionFactory.getSerializerId(), actionFactory);
    }

    public static ActionFactory<Tuple<Entity, Entity>> registerBIAction(ActionFactory<Tuple<Entity, Entity>> actionFactory) {
        return Registry.register(ApoliRegistries.BIENTITY_ACTION, actionFactory.getSerializerId(), actionFactory);
    }
}
