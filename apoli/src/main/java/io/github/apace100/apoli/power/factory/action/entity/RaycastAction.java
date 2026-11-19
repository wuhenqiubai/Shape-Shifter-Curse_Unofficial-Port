package io.github.apace100.apoli.power.factory.action.entity;

import com.mojang.datafixers.util.Pair;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.apache.commons.lang3.tuple.Triple;

import java.util.function.Consumer;

public class RaycastAction {

    public static void action(SerializableData.Instance data, Entity entity) {

        Vec3 origin = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
        Vec3 direction = entity.getViewVector(1);
        Vec3 target = origin.add(direction.scale((double)data.get("distance")));

        data.<Consumer<Entity>>ifPresent("before_action", action -> action.accept(entity));

        HitResult hitResult = null;
        if(data.getBoolean("entity")) {
            hitResult = performEntityRaycast(entity, origin, target, data.get("bientity_condition"));
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
            if(data.isPresent("command_at_hit")) {
                Vec3 offsetDirection = direction;
                double offset = 0;
                Vec3 hitPos = hitResult.getLocation();
                if(data.isPresent("command_hit_offset")) {
                    offset = data.getDouble("command_hit_offset");
                } else {
                    if(hitResult instanceof BlockHitResult bhr) {
                        if(bhr.getDirection() == Direction.DOWN) {
                            offset = entity.getBbHeight();
                        } else if(bhr.getDirection() == Direction.UP) {
                            offset = 0;
                        } else {
                            offset = entity.getBbWidth() / 2;
                            offsetDirection = new Vec3(
                                bhr.getDirection().getStepX(),
                                bhr.getDirection().getStepY(),
                                bhr.getDirection().getStepZ()
                            ).scale(-1);
                        }
                    }
                    offset += 0.05;
                }
                Vec3 at = hitPos.subtract(offsetDirection.scale(offset));
                executeCommandAtHit(entity, at, data.getString("command_at_hit"));
            }
            if(data.isPresent("command_along_ray")) {
                executeStepCommands(entity, origin, hitResult.getLocation(), data.getString("command_along_ray"), data.getDouble("command_step"));
            }
            if(data.isPresent("block_action") && hitResult instanceof BlockHitResult bhr) {
                ActionFactory<Triple<Level, BlockPos, Direction>>.Instance blockAction = data.get("block_action");
                Triple<Level, BlockPos, Direction> blockActionContext = Triple.of(entity.level(), bhr.getBlockPos(), bhr.getDirection());
                blockAction.accept(blockActionContext);
            }
            if(data.isPresent("bientity_action") && hitResult instanceof EntityHitResult ehr) {
                ActionFactory<Pair<Entity, Entity>>.Instance bientityAction = data.get("bientity_action");
                Pair<Entity, Entity> bientityActionContext = new Pair<>(entity, ehr.getEntity());
                bientityAction.accept(bientityActionContext);
            }
            data.<Consumer<Entity>>ifPresent("hit_action", action -> action.accept(entity));
        } else {
            if(data.isPresent("command_along_ray") && !data.getBoolean("command_along_ray_only_on_hit")) {
                executeStepCommands(entity, origin, target, data.getString("command_along_ray"), data.getDouble("command_step"));
            }
            data.<Consumer<Entity>>ifPresent("miss_action", action -> action.accept(entity));
        }
    }

    private static void executeStepCommands(Entity entity, Vec3 origin, Vec3 target, String command, double step) {
        MinecraftServer server = entity.level().getServer();
        if(server != null) {
            Vec3 direction = target.subtract(origin).normalize();
            double length = origin.distanceTo(target);
            for(double current = 0; current < length; current += step) {
                boolean validOutput = !(entity instanceof ServerPlayer) || ((ServerPlayer)entity).connection != null;
                CommandSourceStack source = new CommandSourceStack(
                    Apoli.config.executeCommand.showOutput && validOutput ? entity instanceof ServerPlayer serverPlayer ? serverPlayer.commandSource() : CommandSource.NULL : CommandSource.NULL,
                    origin.add(direction.scale(current)),
                    entity.getRotationVector(),
                    entity.level() instanceof ServerLevel ? (ServerLevel)entity.level() : null,
                    Apoli.config.executeCommand.getPermissionHandler(),
                    entity.getName().getString(),
                    entity.getDisplayName(),
                    entity.level().getServer(),
                    entity);
                server.getCommands().performPrefixedCommand(source, command);
            }
        }
    }

    private static void executeCommandAtHit(Entity entity, Vec3 hitPosition, String command) {
        MinecraftServer server = entity.level().getServer();
        if(server != null) {
            boolean validOutput = !(entity instanceof ServerPlayer) || ((ServerPlayer)entity).connection != null;
            CommandSourceStack source = new CommandSourceStack(
                Apoli.config.executeCommand.showOutput && validOutput ? entity instanceof ServerPlayer serverPlayer ? serverPlayer.commandSource() : CommandSource.NULL : CommandSource.NULL,
                hitPosition,
                entity.getRotationVector(),
                entity.level() instanceof ServerLevel ? (ServerLevel)entity.level() : null,
                Apoli.config.executeCommand.getPermissionHandler(),
                entity.getName().getString(),
                entity.getDisplayName(),
                entity.level().getServer(),
                entity);
            server.getCommands().performPrefixedCommand(source, command);
        }
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

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("raycast"),
            new SerializableData()
                .add("distance", SerializableDataTypes.DOUBLE)
                .add("block", SerializableDataTypes.BOOLEAN, true)
                .add("entity", SerializableDataTypes.BOOLEAN, true)
                .add("shape_type", SerializableDataType.enumValue(ClipContext.Block.class), ClipContext.Block.OUTLINE)
                .add("fluid_handling", SerializableDataType.enumValue(ClipContext.Fluid.class), ClipContext.Fluid.ANY)
                .add("block_action", ApoliDataTypes.BLOCK_ACTION, null)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null)
                .add("command_at_hit", SerializableDataTypes.STRING, null)
                .add("command_hit_offset", SerializableDataTypes.DOUBLE, null)
                .add("command_along_ray", SerializableDataTypes.STRING, null)
                .add("command_step", SerializableDataTypes.DOUBLE, 1.0)
                .add("command_along_ray_only_on_hit", SerializableDataTypes.BOOLEAN, false)
                .add("before_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("hit_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("miss_action", ApoliDataTypes.ENTITY_ACTION, null),
            RaycastAction::action
        );
    }
}
