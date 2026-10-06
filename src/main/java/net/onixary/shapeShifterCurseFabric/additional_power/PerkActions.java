package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.util.function.Consumer;
import java.util.function.Predicate;

/** Small reusable gaps around Apoli actions; perk values remain in resource JSON. */
public final class PerkActions {
    private PerkActions() {}

    public static void setVelocity(Entity entity, Vec3 velocity) {
        entity.setDeltaMovement(velocity);
        entity.hurtMarked = true;
        if (entity instanceof ServerPlayer player) player.connection.send(new ClientboundSetEntityMotionPacket(entity));
    }

    public static boolean isEnemy(Entity actor, Entity target) {
        if (!(target instanceof LivingEntity) || target.isSpectator()
                || actor == target || actor.isAlliedTo(target)) return false;
        if (target instanceof TamableAnimal pet && actor.getUUID().equals(pet.getOwnerUUID())) return false;
        return target instanceof Monster || target instanceof Mob mob && mob.getTarget() == actor;
    }

    public static void registerConditions() {
        Registry.register(ApoliRegistries.ENTITY_CONDITION, ShapeShifterCurseFabric.identifier("charging"),
                new ConditionFactory<Entity>(ShapeShifterCurseFabric.identifier("charging"),
                        new SerializableData().add("power", SerializableDataTypes.IDENTIFIER), (data, entity) -> {
                    var power = PowerHolderComponent.KEY.get(entity).getPower(PowerTypeRegistry.get(data.getId("power")));
                    return power instanceof ChargePower charge && charge.isCharging();
                }));
        Registry.register(ApoliRegistries.BIENTITY_CONDITION, ShapeShifterCurseFabric.identifier("enemy"),
                new ConditionFactory<Tuple<Entity, Entity>>(ShapeShifterCurseFabric.identifier("enemy"),
                        new SerializableData(), (data, pair) -> isEnemy(pair.getA(), pair.getB())));
        Registry.register(ApoliRegistries.BIENTITY_CONDITION, ShapeShifterCurseFabric.identifier("raycast_target"),
                new ConditionFactory<Tuple<Entity, Entity>>(ShapeShifterCurseFabric.identifier("raycast_target"),
                        new SerializableData().add("distance", SerializableDataTypes.DOUBLE, 10.0), (data, pair) -> {
                    Entity actor = pair.getA();
                    Vec3 from = actor.getEyePosition();
                    Vec3 to = from.add(actor.getViewVector(1).scale(data.getDouble("distance")));
                    var block = actor.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER,
                            ClipContext.Fluid.NONE, actor));
                    double max = block.getType() == HitResult.Type.MISS ? from.distanceToSqr(to) : from.distanceToSqr(block.getLocation());
                    var hit = ProjectileUtil.getEntityHitResult(actor, from, to, actor.getBoundingBox().expandTowards(to.subtract(from)).inflate(1),
                            e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator(), max);
                    return hit != null && hit.getEntity() == pair.getB();
                }));
    }

    public static void registerActions() {
        AdditionalEntityActions.registerAction(new ActionFactory<Entity>(ShapeShifterCurseFabric.identifier("change_food"),
                new SerializableData().add("amount", SerializableDataTypes.INT), (data, entity) -> {
            if (entity instanceof Player player && !entity.level().isClientSide) {
                var hunger = player.getFoodData();
                hunger.setFoodLevel(Math.max(0, Math.min(20, hunger.getFoodLevel() + data.getInt("amount"))));
                hunger.setSaturation(Math.min(hunger.getSaturationLevel(), hunger.getFoodLevel()));
            }
        }));
        AdditionalEntityActions.registerBIAction(new ActionFactory<Tuple<Entity, Entity>>(ShapeShifterCurseFabric.identifier("damage_on_success"),
                new SerializableData().add("amount", SerializableDataTypes.FLOAT)
                        .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null), (data, pair) -> {
            if (!(pair.getA() instanceof LivingEntity actor) || actor.level().isClientSide) return;
            var source = actor instanceof Player player ? actor.damageSources().playerAttack(player) : actor.damageSources().mobAttack(actor);
            if (pair.getB().hurt(source, data.getFloat("amount"))) {
                Consumer<Tuple<Entity, Entity>> action = data.get("bientity_action");
                if (action != null) action.accept(pair);
            }
        }));
        AdditionalEntityActions.registerBIAction(new ActionFactory<Tuple<Entity, Entity>>(ShapeShifterCurseFabric.identifier("start_target_pounce"),
                new SerializableData().add("power", SerializableDataTypes.IDENTIFIER), (data, pair) -> {
            if (pair.getA().level().isClientSide || !(pair.getB() instanceof LivingEntity target)) return;
            var type = PowerTypeRegistry.get(data.getId("power"));
            var power = PowerHolderComponent.KEY.get(pair.getA()).getPower(type);
            if (power instanceof TargetPouncePower pounce) pounce.start(target);
        }));
        AdditionalEntityActions.registerAction(new ActionFactory<Entity>(ShapeShifterCurseFabric.identifier("fire_held_arrow"),
                new SerializableData().add("speed", SerializableDataTypes.FLOAT, 3f)
                        .add("cooldown", SerializableDataTypes.INT, 20), (data, entity) -> {
            if (!(entity instanceof Player player) || player.level().isClientSide) return;
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof ArrowItem item) || (player.getCooldowns().isOnCooldown(item) || player.getCooldowns().isOnCooldown(Items.ARROW))) return;
            // 1.21 起 createArrow 多一个「发射武器 stack」参数。
            // ⚠ 必须传 null，不能传 ItemStack.EMPTY —— AbstractArrow 的构造里对「非 null 但空」的
            //   武器栈会在服务端直接抛 IllegalArgumentException（"Invalid weapon firing an arrow"）。
            AbstractArrow arrow = item.createArrow(player.level(), stack, player, null);
            // 按 pitch/yaw 发射用 shootFromRotation；setDeltaMovement 只能直接设速度向量
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, data.getFloat("speed"), 0);
            arrow.setCritArrow(true);
            arrow.pickup = player.isCreative() ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.ALLOWED;
            if (player.level().addFreshEntity(arrow)) {
                if (!player.isCreative()) stack.shrink(1);
                player.getCooldowns().addCooldown(item, data.getInt("cooldown"));
                player.getCooldowns().addCooldown(Items.ARROW, data.getInt("cooldown"));
            }
        }));
        AdditionalEntityActions.registerAction(new ActionFactory<Entity>(ShapeShifterCurseFabric.identifier("oriented_box"),
                new SerializableData().add("width", SerializableDataTypes.DOUBLE, 4.0)
                        .add("length", SerializableDataTypes.DOUBLE, 7.0)
                        .add("height", SerializableDataTypes.DOUBLE, 3.0)
                        .add("centered", SerializableDataTypes.BOOLEAN, false)
                        .add("particles", SerializableDataTypes.BOOLEAN, false)
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION), PerkActions::boxAction));
    }

    private static void boxAction(SerializableData.Instance data, Entity actor) {
        if (!(actor.level() instanceof ServerLevel world)) return;
        double width = data.getDouble("width"), length = data.getDouble("length"), height = data.getDouble("height");
        if (width <= 0 || length <= 0 || height <= 0) return;
        Vec3 forward = new Vec3(-Math.sin(Math.toRadians(actor.getYRot())), 0, Math.cos(Math.toRadians(actor.getYRot())));
        Vec3 right = new Vec3(forward.z, 0, -forward.x);
        double offset = data.getBoolean("centered") ? 0 : length / 2;
        Vec3 center = actor.position().add(forward.scale(offset)).add(0, height / 2, 0);
        Predicate<Tuple<Entity, Entity>> condition = data.get("bientity_condition");
        Consumer<Tuple<Entity, Entity>> action = data.get("bientity_action");
        for (LivingEntity target : world.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(width + length, height, width + length),
                e -> e != actor && e.isAlive() && !e.isSpectator())) {
            AABB bounds = target.getBoundingBox();
            Vec3 relative = bounds.getCenter().subtract(center);
            double rx = (bounds.maxX - bounds.minX) / 2, rz = (bounds.maxZ - bounds.minZ) / 2;
            if (Math.abs(relative.dot(right)) > width / 2 + Math.abs(right.x) * rx + Math.abs(right.z) * rz
                    || Math.abs(relative.dot(forward)) > length / 2 + Math.abs(forward.x) * rx + Math.abs(forward.z) * rz
                    || Math.abs(relative.y) > height / 2 + (bounds.maxY - bounds.minY) / 2) continue;
            Tuple<Entity, Entity> pair = new Tuple<>(actor, target);
            if (condition == null || condition.test(pair)) action.accept(pair);
        }
        if (data.getBoolean("particles")) {
            for (double z = -length / 2; z <= length / 2; z += 0.5) {
                for (double x = -width / 2; x <= width / 2; x += 0.5) {
                    Vec3 pos = center.add(forward.scale(z)).add(right.scale(x));
                    world.sendParticles(ParticleTypes.CLOUD, pos.x, pos.y, pos.z, 1, 0, height / 4, 0, 0.03);
                }
            }
        }
    }
}
