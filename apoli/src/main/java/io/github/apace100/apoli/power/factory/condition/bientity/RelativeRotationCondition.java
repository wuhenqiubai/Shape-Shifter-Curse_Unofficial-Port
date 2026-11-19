package io.github.apace100.apoli.power.factory.condition.bientity;

import com.mojang.datafixers.util.Pair;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.function.Function;

public class RelativeRotationCondition {

    public static boolean condition(SerializableData.Instance data, Pair<Entity, Entity> pair) {
        RotationType actorRotation = data.get("actor_rotation");
        RotationType targetRotation = data.get("target_rotation");
        Vec3 vec0 = actorRotation.getRotation(pair.getFirst());
        Vec3 vec1 = targetRotation.getRotation(pair.getSecond());
        EnumSet<Direction.Axis> axes = data.get("axes");
        vec0 = reduceAxes(vec0, axes);
        vec1 = reduceAxes(vec1, axes);
        double angle = getAngleBetween(vec0, vec1);
        Comparison comparison = data.get("comparison");
        double compareTo = data.getDouble("compare_to");
        return comparison.compare(angle, compareTo);
    }

    private static double getAngleBetween(Vec3 a, Vec3 b) {
        double dot = a.dot(b);
        return dot / (a.length() * b.length());
    }

    private static Vec3 reduceAxes(Vec3 vector, EnumSet<Direction.Axis> axesToKeep) {
        return new Vec3(
            axesToKeep.contains(Direction.Axis.X) ? vector.x : 0,
            axesToKeep.contains(Direction.Axis.Y) ? vector.y : 0,
            axesToKeep.contains(Direction.Axis.Z) ? vector.z : 0
        );
    }

    public static ConditionFactory<Pair<Entity, Entity>> getFactory() {
        return new ConditionFactory<>(Apoli.identifier("relative_rotation"),
            new SerializableData()
                .add("axes", SerializableDataTypes.AXIS_SET, EnumSet.allOf(Direction.Axis.class))
                .add("actor_rotation", SerializableDataType.enumValue(RotationType.class), RotationType.HEAD)
                .add("target_rotation", SerializableDataType.enumValue(RotationType.class), RotationType.BODY)
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.DOUBLE),
            RelativeRotationCondition::condition
        );
    }

    private static Vec3 getRotationVector(float pitch, float yaw) {
        float f = pitch * ((float)Math.PI / 180);
        float g = -yaw * ((float)Math.PI / 180);
        float h = Mth.cos(g);
        float i = Mth.sin(g);
        float j = Mth.cos(f);
        float k = Mth.sin(f);
        return new Vec3(i * j, -k, h * j);
    }

    public enum RotationType {
        HEAD(e -> e.getViewVector(1.0F)), BODY(e -> {
            if(e instanceof LivingEntity l) {
                return getRotationVector(0F, l.yBodyRot);
            } else {
                return e.getViewVector(1.0F);
            }
        });

        private final Function<Entity, Vec3> function;

        RotationType(Function<Entity, Vec3> function) {
            this.function = function;
        }

        public Vec3 getRotation(Entity entity) {
            return function.apply(entity);
        }
    }
}