package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.bientity.RelativeRotationCondition;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.Registry;
import net.minecraft.util.Tuple;
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
                for (ConditionFactory<Tuple<Entity, Entity>>.Instance condition : ((List<ConditionFactory<Tuple<Entity, Entity>>.Instance>) data.get("conditions"))) {
                    if (!condition.test(pair))
                        return false;
                }

                return true;
            }));
        register(new ConditionFactory<>(Apoli.identifier("or"), new SerializableData()
            .add("conditions", ApoliDataTypes.BIENTITY_CONDITIONS),
            (data, pair) -> {
                for (ConditionFactory<Tuple<Entity, Entity>>.Instance condition : ((List<ConditionFactory<Tuple<Entity, Entity>>.Instance>) data.get("conditions"))) {
                    if (condition.test(pair))
                        return true;
                }

                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("invert"), new SerializableData()
            .add("condition", ApoliDataTypes.BIENTITY_CONDITION),
            (data, pair) -> {
                Predicate<Tuple<Entity, Entity>> cond = data.get("condition");
                return cond.test(new Tuple<>(pair.getB(), pair.getA()));
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("actor_condition"), new SerializableData()
            .add("condition", ApoliDataTypes.ENTITY_CONDITION),
            (data, pair) -> {
                Predicate<Entity> cond = data.get("condition");
                return cond.test(pair.getA());
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("target_condition"), new SerializableData()
            .add("condition", ApoliDataTypes.ENTITY_CONDITION),
            (data, pair) -> {
                Predicate<Entity> cond = data.get("condition");
                return cond.test(pair.getB());
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("either"), new SerializableData()
            .add("condition", ApoliDataTypes.ENTITY_CONDITION),
            (data, pair) -> {
                Predicate<Entity> cond = data.get("condition");
                return cond.test(pair.getA()) || cond.test(pair.getB());
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("both"), new SerializableData()
            .add("condition", ApoliDataTypes.ENTITY_CONDITION),
            (data, pair) -> {
                Predicate<Entity> cond = data.get("condition");
                return cond.test(pair.getA()) && cond.test(pair.getB());
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("undirected"), new SerializableData()
            .add("condition", ApoliDataTypes.BIENTITY_CONDITION),
            (data, pair) -> {
                Predicate<Tuple<Entity, Entity>> cond = data.get("condition");
                return cond.test(pair) || cond.test(new Tuple<>(pair.getB(), pair.getA()));
            }
            ));

        register(new ConditionFactory<>(Apoli.identifier("distance"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.DOUBLE),
            (data, pair) -> {
                double distanceSq = pair.getA().position().distanceToSqr(pair.getB().position());
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
                if (pair.getB().level() != pair.getA().level()) {
                    return false;
                } else {
                    Vec3 vec3d = new Vec3(pair.getA().getX(), pair.getA().getEyeY(), pair.getA().getZ());
                    Vec3 vec3d2 = new Vec3(pair.getB().getX(), pair.getB().getEyeY(), pair.getB().getZ());
                    if (vec3d2.distanceTo(vec3d) > 128.0D) {
                        return false;
                    } else {
                        return pair.getA().level().clip(new ClipContext(vec3d, vec3d2, shapeType, fluidHandling, pair.getA())).getType() == HitResult.Type.MISS;
                    }
                }
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("owner"), new SerializableData(),
            (data, pair) -> {
                if(pair.getB() instanceof OwnableEntity) {
                    return pair.getA() == ((OwnableEntity)pair.getB()).getOwner();
                }
                return false;
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("riding"), new SerializableData(),
            (data, pair) -> pair.getA().getVehicle() == pair.getB()
        ));
        register(new ConditionFactory<>(Apoli.identifier("riding_root"), new SerializableData(),
            (data, pair) -> pair.getA().getRootVehicle() == pair.getB()
        ));
        register(new ConditionFactory<>(Apoli.identifier("riding_recursive"), new SerializableData(),
            (data, pair) -> {
                if(pair.getA().getVehicle() == null) {
                    return false;
                }
                Entity vehicle = pair.getA().getVehicle();
                while(vehicle != pair.getB() && vehicle != null) {
                    vehicle = vehicle.getVehicle();
                }
                return vehicle == pair.getB();
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("attack_target"), new SerializableData(),
            (data, pair) -> {
                if(pair.getA() instanceof Mob) {
                    return ((Mob)pair.getA()).getTarget() == pair.getB();
                }
                if(pair.getA() instanceof NeutralMob) {
                    return ((NeutralMob)pair.getA()).getTarget() == pair.getB();
                }
                return false;
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("attacker"), new SerializableData(),
            (data, pair) -> {
                if(pair.getB() instanceof LivingEntity living) {
                    return living.getLastHurtByMob() == pair.getA();
                }
                return false;
            }
        ));
        register(RelativeRotationCondition.getFactory());
    }

    private static void register(ConditionFactory<Tuple<Entity, Entity>> conditionFactory) {
        Registry.register(ApoliRegistries.BIENTITY_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
