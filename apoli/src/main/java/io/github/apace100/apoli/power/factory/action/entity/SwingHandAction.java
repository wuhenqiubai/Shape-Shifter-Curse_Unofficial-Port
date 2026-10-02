package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class SwingHandAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if(entity instanceof LivingEntity living) {
            living.swing(data.get("hand"), true);
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("swing_hand"),
            new SerializableData()
                .add("hand", SerializableDataTypes.HAND, InteractionHand.MAIN_HAND),
            SwingHandAction::action
        );
    }
}
