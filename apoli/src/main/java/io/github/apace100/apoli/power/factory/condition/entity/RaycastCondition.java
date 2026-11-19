package io.github.apace100.apoli.power.factory.condition.entity;

import com.mojang.datafixers.util.Pair;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.*;

import java.util.function.Predicate;

public class RaycastCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        Vec3 origin = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
        Vec3 direction = entity.getViewVector(1);
        Vec3 target = origin.add(direction.scale((double)data.get("distance")));

        HitResult hitResult = null;
        if(data.getBoolean("entity")) {
            hitResult = performEntityRaycast(entity, origin, target, data.get("match_bientity_condition"));
        }
        if(data.getBoolean("block")) {
            BlockHitResult blockHit = performBlockRaycast(entity, origin, target, data.get("shape_type"), data.get("fluid_handling"));
            if(blockHit.getType() != HitResult.Type.MISS) {
                if(hitResult == null || hitResult.getType() == HitResult.Type.MISS) {
                    hitResult = blockHit;
                } else {
                    if(hitResult.distanceTo(entity) > blockHit.distanceTo(entity)) {
                        hitResult = blockHit;
                    }
                }
            }
        }
        if(hitResult != null && hitResult.getType() != HitResult.Type.MISS) {
            if(hitResult instanceof BlockHitResult bhr && data.isPresent("block_condition")) {
                BlockInWorld cbp = new BlockInWorld(entity.level(), bhr.getBlockPos(), true);
                return data.<Predicate<BlockInWorld>>get("block_condition").test(cbp);
            }
            if(hitResult instanceof EntityHitResult ehr && data.isPresent("hit_bientity_condition")) {
                return data.<Predicate<Pair<Entity, Entity>>>get("hit_bientity_condition")
                    .test(new Pair<>(entity, ehr.getEntity()));
            }
            return true;
        }
        return false;
    }

    private static BlockHitResult performBlockRaycast(Entity source, Vec3 origin, Vec3 target, ClipContext.Block shapeType, ClipContext.Fluid fluidHandling) {
        ClipContext context = new ClipContext(origin, target, shapeType, fluidHandling, source);
        return source.level().clip(context);
    }

    private static EntityHitResult performEntityRaycast(Entity source, Vec3 origin, Vec3 target, ConditionFactory<Pair<Entity, Entity>>.Instance biEntityCondition) {
        Vec3 ray = target.subtract(origin);
        AABB box = source.getBoundingBox().expandTowards(ray).inflate(1.0D, 1.0D, 1.0D);
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(source, origin, target, box, (entityx) -> {
            return !entityx.isSpectator() && (biEntityCondition == null || biEntityCondition.test(new Pair<>(source, entityx)));
        }, ray.lengthSqr());
        return entityHitResult;
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(Apoli.identifier("raycast"),
            new SerializableData()
                .add("distance", SerializableDataTypes.DOUBLE)
                .add("block", SerializableDataTypes.BOOLEAN, true)
                .add("entity", SerializableDataTypes.BOOLEAN, true)
                .add("shape_type", SerializableDataType.enumValue(ClipContext.Block.class), ClipContext.Block.OUTLINE)
                .add("fluid_handling", SerializableDataType.enumValue(ClipContext.Fluid.class), ClipContext.Fluid.ANY)
                .add("match_bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("hit_bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null),
            RaycastCondition::condition
        );
    }
}
