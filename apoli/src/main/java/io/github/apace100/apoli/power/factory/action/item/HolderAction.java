package io.github.apace100.apoli.power.factory.action.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class HolderAction {
    public static void action(SerializableData.Instance data, Tuple<Level, ItemStack> worldAndStack) {
        if(worldAndStack.getB().isEmpty()) {
            return;
        }
        Entity holder = ((EntityLinkedItemStack)worldAndStack.getB()).getEntity();
        if(holder == null) {
            return;
        }
        Consumer<Entity> entityAction = data.get("entity_action");
        entityAction.accept(holder);
    }

    public static ActionFactory<Tuple<Level, ItemStack>> getFactory() {
        return new ActionFactory<>(Apoli.identifier("holder"),
            new SerializableData()
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION),
            HolderAction::action
        );
    }
}

