package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class HissPhantomPower extends Power {
    private final @Nullable Consumer<Tuple<Entity, Entity>> onHissPhantomAction;

    public HissPhantomPower(PowerType<?> type, LivingEntity entity, SerializableData.Instance data) {
        super(type, entity);
        this.onHissPhantomAction = data.get("on_hiss_phantom_action");
    }

    public void invokeAction(LivingEntity powerOwner, Phantom phantom) {
        if (this.onHissPhantomAction != null) {
            this.onHissPhantomAction.accept(new Tuple<>(powerOwner, phantom));
        }
    }

    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(
                ShapeShifterCurseFabric.identifier("hiss_phantom_power"),
                new SerializableData()
                        .add("on_hiss_phantom_action", ApoliDataTypes.BIENTITY_ACTION, null),
                data -> (type, entity) -> new HissPhantomPower(type, entity, data)
        ).allowCondition();
    }
}
