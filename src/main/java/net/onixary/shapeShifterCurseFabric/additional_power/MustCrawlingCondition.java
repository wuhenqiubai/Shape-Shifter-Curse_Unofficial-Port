package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

public class MustCrawlingCondition {
    private static boolean IsHeadNotCollide(Entity e, float width, float height) {
        if (e.noPhysics || e.isSpectator()) {
            return true;
        }
        Vec3 vec3d = e.position();
        return e.level().noCollision(e, new AABB(vec3d.x - width / 2.0, vec3d.y, vec3d.z - width / 2.0, vec3d.x + width / 2.0, vec3d.y + height, vec3d.z + width / 2.0).deflate(1.0E-7));
    }

    public static boolean condition(SerializableData.Instance data, Entity e) {
        return !IsHeadNotCollide(e, data.getFloat("width"), data.getFloat("height"));
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<Entity>(
                ShapeShifterCurseFabric.identifier("must_crawling"),
                new SerializableData()
                        .add("width", SerializableDataTypes.FLOAT, 0.6f)
                        .add("height", SerializableDataTypes.FLOAT, 1.5f),
                MustCrawlingCondition::condition
        );
    }
}