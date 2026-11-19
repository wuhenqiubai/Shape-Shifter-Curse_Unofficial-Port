package io.github.apace100.apoli.power.factory.condition;

import com.mojang.datafixers.util.Pair;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.bientity.RelativeRotationCondition;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Predicate;

public class BiEntityConditions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ConditionFactory<>(Apoli.identifier("constant"), new SerializableData()
            .add("value", SerializableDataTypes.BOOLEAN),
            (data, pair) -> data.getBoolean("value")));
        register(new ConditionFactory<>(Apoli.identifier("and"), new SerializableData()
            .add("conditions", ApoliDataTypes.BIENTITY_CONDITIONS),
            (data, pair) -> {
                for (ConditionFactory<Pair<Entity, Entity>>.Instance condition : ((List<ConditionFactory<Pair<Entity, Entity>>.Instance>) data.get("conditions"))) {
                    if (!condition.test(pair))
                        return false;
                }

                return true;
            }));
        register(new ConditionFactory<>(Apoli.identifier("or"), new SerializableData()
            .add("conditions", ApoliDataTypes.BIENTITY_CONDITIONS),
            (data, pair) -> {
                for (ConditionFactory<Pair<Entity, Entity>>.Instance condition : ((List<ConditionFactory<Pair<Entity, Entity>>.Instance>) data.get("conditions"))) {
                    if (condition.test(pair))
                        return true;
                }

                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("invert"), new SerializableData()
            .add("condition", ApoliDataTypes.BIENTITY_CONDITION),
            (data, pair) -> {
                Predicate<Pair<Entity, Entity>> cond = data.get("condition");
                return cond.test(new Pair<>(pair.getSecond(), pair.getFirst()));
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("actor_condition"), new SerializableData()
            .add("condition", ApoliDataTypes.ENTITY_CONDITION),
            (data, pair) -> {
                Predicate<Entity> cond = data.get("condition");
                return cond.test(pair.getFirst());
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("target_condition"), new SerializableData()
            .add("condition", ApoliDataTypes.ENTITY_CONDITION),
            (data, pair) -> {
                Predicate<Entity> cond = data.get("condition");
                return cond.test(pair.getSecond());
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("either"), new SerializableData()
            .add("condition", ApoliDataTypes.ENTITY_CONDITION),
            (data, pair) -> {
                Predicate<Entity> cond = data.get("condition");
                return cond.test(pair.getFirst()) || cond.test(pair.getSecond());
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("both"), new SerializableData()
            .add("condition", ApoliDataTypes.ENTITY_CONDITION),
            (data, pair) -> {
                Predicate<Entity> cond = data.get("condition");
                return cond.test(pair.getFirst()) && cond.test(pair.getSecond());
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("undirected"), new SerializableData()
            .add("condition", ApoliDataTypes.BIENTITY_CONDITION),
            (data, pair) -> {
                Predicate<Pair<Entity, Entity>> cond = data.get("condition");
                return cond.test(pair) || cond.test(new Pair<>(pair.getSecond(), pair.getFirst()));
            }
            ));

        register(new ConditionFactory<>(Apoli.identifier("distance"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.DOUBLE),
            (data, pair) -> {
                double distanceSq = pair.getFirst().position().distanceToSqr(pair.getSecond().position());
                double comp = data.getDouble("compare_to");
                comp *= comp;
                return ((Comparison)data.get("comparison")).compare(distanceSq, comp);
            }
            ));
        register(new ConditionFactory<>(Apoli.identifier("can_see"), new SerializableData()
            .add("shape_type", SerializableDataType.enumValue(ClipContext.Block.class), ClipContext.Block.VISUAL)
            .add("fluid_handling", SerializableDataType.enumValue(ClipContext.Fluid.class), ClipContext.Fluid.NONE),
            (data, pair) -> {
                ClipContext.Block shapeType = data.get("shape_type");
                ClipContext.Fluid fluidHandling = data.get("fluid_handling");
                if (pair.getSecond().level() != pair.getFirst().level()) {
                    return false;
                } else {
                    Vec3 vec3d = new Vec3(pair.getFirst().getX(), pair.getFirst().getEyeY(), pair.getFirst().getZ());
                    Vec3 vec3d2 = new Vec3(pair.getSecond().getX(), pair.getSecond().getEyeY(), pair.getSecond().getZ());
                    if (vec3d2.distanceTo(vec3d) > 128.0D) {
                        return false;
                    } else {
                        return pair.getFirst().level().clip(new ClipContext(vec3d, vec3d2, shapeType, fluidHandling, pair.getFirst())).getType() == HitResult.Type.MISS;
                    }
                }
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("owner"), new SerializableData(),
            (data, pair) -> {
                if(pair.getSecond() instanceof OwnableEntity) {
                    return pair.getFirst() == ((OwnableEntity)pair.getSecond()).getOwner();
                }
                return false;
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("riding"), new SerializableData(),
            (data, pair) -> pair.getFirst().getVehicle() == pair.getSecond()
        ));
        register(new ConditionFactory<>(Apoli.identifier("riding_root"), new SerializableData(),
            (data, pair) -> pair.getFirst().getRootVehicle() == pair.getSecond()
        ));
        register(new ConditionFactory<>(Apoli.identifier("riding_recursive"), new SerializableData(),
            (data, pair) -> {
                if(pair.getFirst().getVehicle() == null) {
                    return false;
                }
                Entity vehicle = pair.getFirst().getVehicle();
                while(vehicle != pair.getSecond() && vehicle != null) {
                    vehicle = vehicle.getVehicle();
                }
                return vehicle == pair.getSecond();
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("attack_target"), new SerializableData(),
            (data, pair) -> {
                if(pair.getFirst() instanceof Mob) {
                    return ((Mob)pair.getFirst()).getTarget() == pair.getSecond();
                }
                if(pair.getFirst() instanceof NeutralMob) {
                    return ((NeutralMob)pair.getFirst()).getTarget() == pair.getSecond();
                }
                return false;
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("attacker"), new SerializableData(),
            (data, pair) -> {
                if(pair.getSecond() instanceof LivingEntity living) {
                    return living.getLastHurtByMob() == pair.getFirst();
                }
                return false;
            }
        ));
        register(RelativeRotationCondition.getFactory());
    }

    private static void register(ConditionFactory<Pair<Entity, Entity>> conditionFactory) {
        Registry.register(ApoliRegistries.BIENTITY_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
