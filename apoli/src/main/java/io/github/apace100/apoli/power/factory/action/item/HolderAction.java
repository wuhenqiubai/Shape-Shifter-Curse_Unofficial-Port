package io.github.apace100.apoli.power.factory.action.item;

import com.mojang.datafixers.util.Pair;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class HolderAction {
    public static void action(SerializableData.Instance data, Pair<Level, ItemStack> worldAndStack) {
        if(worldAndStack.getSecond().isEmpty()) {
            return;
        }
        Entity holder = ((EntityLinkedItemStack)worldAndStack.getSecond()).apoli$getEntity();
        if(holder == null) {
            return;
        }
        Consumer<Entity> entityAction = data.get("entity_action");
        entityAction.accept(holder);
    }

    public static ActionFactory<Pair<Level, ItemStack>> getFactory() {
        return new ActionFactory<>(Apoli.identifier("holder"),
            new SerializableData()
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION),
            HolderAction::action
        );
    }
}

