package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.minion.IPlayerEntityMinion;
import net.onixary.shapeShifterCurseFabric.minion.MinionRegister;

import java.util.function.Consumer;
import java.util.function.Predicate;

public final class FamiliarFoxActions {
    public static void register() {
        Registry.register(io.github.apace100.apoli.registry.ApoliRegistries.ENTITY_CONDITION,
                ShapeShifterCurseFabric.identifier("blocking"), new io.github.apace100.apoli.power.factory.condition.ConditionFactory<Entity>(
                        ShapeShifterCurseFabric.identifier("blocking"), new SerializableData(),
                        (data, entity) -> entity instanceof LivingEntity living && living.isBlocking()));
        AdditionalEntityActions.registerBIAction(new ActionFactory<Tuple<Entity, Entity>>(
                ShapeShifterCurseFabric.identifier("fireball_at_target"), new SerializableData(), (data, pair) -> {
            if (!(pair.getA() instanceof LivingEntity actor) || !(actor.level() instanceof ServerLevel world)) return;
            Vec3 direction = pair.getB().getBoundingBox().getCenter().subtract(actor.getEyePosition()).normalize();
            // SmallFireball 的第 3 参是 Vec3（不是三个 double）；Vec3 标量缩放用 scale，不是 multiply
            var fireball = new SmallFireball(world, actor, direction);
            fireball.setPos(actor.getEyePosition().add(direction.scale(0.6)));
            world.addFreshEntity(fireball);
        }));
        AdditionalEntityActions.registerAction(new ActionFactory<Entity>(ShapeShifterCurseFabric.identifier("surface_area"),
                new SerializableData().add("distance", SerializableDataTypes.DOUBLE, 20.0)
                        .add("radius", SerializableDataTypes.DOUBLE, 4.0)
                        .add("preview", SerializableDataTypes.BOOLEAN, false)
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null), FamiliarFoxActions::surfaceArea));
        AdditionalEntityActions.registerAction(new ActionFactory<Entity>(ShapeShifterCurseFabric.identifier("summon_mana_reservoir"),
                new SerializableData().add("food_cost", SerializableDataTypes.INT, 6), (data, entity) -> {
            if (!(entity instanceof ServerPlayer player) || !(player instanceof IPlayerEntityMinion minions)) return;
            int cost = data.getInt("food_cost");
            if (player.getFoodData().getFoodLevel() < cost
                    || minions.shape_shifter_curse$getMinionsCount(ShapeShifterCurseFabric.identifier("mana_reservoir")) >= 1) return;
            var reservoir = MinionRegister.MANA_RESERVOIR.create(player.serverLevel());
            if (reservoir == null) return;
            Vec3 position = player.getEyePosition().add(player.getViewVector(1).scale(1.5));
            reservoir.setPos(position);
            if (!player.level().noCollision(reservoir, reservoir.getBoundingBox())) return;
            reservoir.setOwner(player);
            if (player.serverLevel().addFreshEntity(reservoir)) {
                reservoir.InitMinion(player);
                player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel() - cost);
                player.getFoodData().setSaturation(Math.min(player.getFoodData().getSaturationLevel(), player.getFoodData().getFoodLevel()));
            }
        }));
    }

    private static void surfaceArea(SerializableData.Instance data, Entity actor) {
        if (!(actor.level() instanceof ServerLevel world)) return;
        Vec3 eye = actor.getEyePosition();
        var hit = world.clip(new ClipContext(eye, eye.add(actor.getViewVector(1).scale(data.getDouble("distance"))),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, actor));
        if (hit.getType() != HitResult.Type.BLOCK) return;
        Vec3 center = hit.getLocation().add(Vec3.atLowerCornerOf(hit.getDirection().getNormal()).scale(0.05));
        if (data.getBoolean("preview")) {
            world.sendParticles(ParticleTypes.FLAME, center.x, center.y, center.z, 2, 0.08, 0.08, 0.08, 0);
            return;
        }
        double radius = data.getDouble("radius");
        if (radius <= 0) return;
        Predicate<Tuple<Entity, Entity>> condition = data.get("bientity_condition");
        Consumer<Tuple<Entity, Entity>> action = data.get("bientity_action");
        for (var target : world.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(radius),
                target -> target != actor && target.isAlive() && !target.isSpectator() && target.distanceToSqr(center) <= radius * radius)) {
            var pair = new Tuple<Entity, Entity>(actor, target);
            if (action != null && (condition == null || condition.test(pair))) action.accept(pair);
        }
        for (int i = 0; i < 64; i++) {
            double angle = i * Math.PI * 2 / 64;
            world.sendParticles(ParticleTypes.FLAME, center.x + Math.cos(angle) * radius, center.y,
                    center.z + Math.sin(angle) * radius, 1, 0, 0.05, 0, 0.01);
        }
    }
}
