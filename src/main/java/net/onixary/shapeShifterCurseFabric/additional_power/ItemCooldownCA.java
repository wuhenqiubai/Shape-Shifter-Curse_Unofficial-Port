package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.util.function.Consumer;

public class ItemCooldownCA {
    public static void registerCondition(Consumer<ConditionFactory<Entity>> registerFunc) {
        registerFunc.accept(new ConditionFactory<Entity>(
			    ShapeShifterCurseFabric.identifier("is_item_in_cooldown"),
			    new SerializableData()
					    .add("item", SerializableDataTypes.ITEM, null),
			    (data, e) -> {
				    Item item = data.get("item");
				    if (item == null) {
					    return false;
				    }
				    if (e instanceof Player player) {
					    return player.getCooldowns().isOnCooldown(item);
				    }
				    return false;
			    }
        ));
    }

    public static void registerAction(Consumer<ActionFactory<Entity>> ActionRegister, Consumer<ActionFactory<Tuple<Entity, Entity>>> BIActionRegister) {
        ActionRegister.accept(new ActionFactory<Entity>(
			    ShapeShifterCurseFabric.identifier("set_item_cooldown"),
			    new SerializableData()
					    .add("item", SerializableDataTypes.ITEM, null)
					    .add("cooldown", SerializableDataTypes.INT, 0),
			    (data, entity) -> {
				    Item item = data.get("item");
				    int cooldown = data.get("cooldown");
				    if (item == null) {
					    return;
				    }
				    if (entity instanceof Player player) {
					    player.getCooldowns().addCooldown(item, cooldown);
				    }
			    }
        ));
    }
}