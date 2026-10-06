package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.util.function.Consumer;

/** Raycast chooses the target; this power only owns the collision-aware flight. */
public class TargetPouncePower extends Power {
    private final double speed;
    private final int duration;
    private final Consumer<Tuple<Entity, Entity>> hitAction;
    private LivingEntity target;
    private Vec3 direction;
    private int remaining;

    public TargetPouncePower(PowerType<?> type, LivingEntity entity, SerializableData.Instance data) {
        super(type, entity);
        speed = data.getDouble("speed");
        duration = data.getInt("duration");
        hitAction = data.get("bientity_action");
        setTicking(true);
    }

    public void start(LivingEntity target) {
        if (!isActive() || entity.level().isClientSide || !target.isAlive()) return;
        this.target = target;
        // Snapshot the direction: a straight pounce, not a homing projectile.
        direction = target.getBoundingBox().getCenter().subtract(entity.getBoundingBox().getCenter()).normalize();
        remaining = duration;
    }

    @Override
    public void tick() {
        if (entity.level().isClientSide || target == null) return;
        if (!isActive() || !entity.isAlive() || !target.isAlive() || target.isRemoved()
                || target.level() != entity.level() || remaining-- <= 0) {
            stop();
            return;
        }
        if (entity.getBoundingBox().inflate(0.3).intersects(target.getBoundingBox())) {
            LivingEntity hit = target;
            stop();
            hitAction.accept(new Tuple<>(entity, hit));
            return;
        }
        double distance = entity.getBoundingBox().getCenter().distanceTo(target.getBoundingBox().getCenter());
        Vec3 step = direction.scale(Math.min(speed, Math.max(0.1, distance - 0.3)));
        var sweptBox = entity.getBoundingBox().expandTowards(step);
        if (!entity.level().getWorldBorder().isWithinBounds(sweptBox)) {
            stop();
            return;
        }
        for (var collision : entity.level().getBlockCollisions(entity, sweptBox)) {
            if (!collision.isEmpty()) {
                stop();
                return;
            }
        }
        entity.fallDistance = 0;
        PerkActions.setVelocity(entity, step);
    }

    private void stop() {
        if (target != null) PerkActions.setVelocity(entity, Vec3.ZERO);
        target = null;
        remaining = 0;
    }

    @Override public void onRemoved() { stop(); }

    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(ShapeShifterCurseFabric.identifier("target_pounce"),
                new SerializableData().add("speed", SerializableDataTypes.DOUBLE, 1.5)
                        .add("duration", SerializableDataTypes.INT, 20)
                        .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION),
                data -> (type, entity) -> new TargetPouncePower(type, entity, data)).allowCondition();
    }
}
