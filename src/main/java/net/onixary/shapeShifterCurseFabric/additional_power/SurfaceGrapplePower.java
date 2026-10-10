package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.mana.ManaUtils;

/** Server-owned reservation, surface preview and collision-aware straight flight. */
public class SurfaceGrapplePower extends Power implements Active {
    private final double distance, speed, manaCost;
    private final int cooldown;
    private final ResourceLocation manaType;
    private Key key;
    private boolean charging, paid, savedGravity, restoreGravity;
    private int ticks, lastUse, flightTicks;
    private long readyAt;
    private Vec3 destination;

    public SurfaceGrapplePower(PowerType<?> type, LivingEntity entity, SerializableData.Instance data) {
        super(type, entity);
        distance = Math.max(0, data.getDouble("distance"));
        speed = Math.max(0.01, data.getDouble("speed"));
        manaCost = Math.max(0, data.getDouble("mana_cost"));
        cooldown = Math.max(0, data.getInt("cooldown"));
        manaType = data.getId("mana_type");
        key = data.get("key");
        setTicking(true);
    }

    @Override public void onUse() {
        if (!(entity instanceof ServerPlayer player) || !isActive() || !entity.isAlive()
                || entity.isPassenger() || destination != null || player.level().getGameTime() < readyAt) return;
        if (!charging) {
            if (!manaType.equals(ManaUtils.getManaComponent(player).getManaTypeID())
                    || ManaUtils.getManaComponent(player).getMana() < manaCost) return;
            ManaUtils.getManaComponent(player).consumeMana(manaCost);
            charging = paid = true;
        }
        lastUse = ticks;
    }

    public Vec3 findDestination() {
        Vec3 eye = entity.getEyePosition();
        var hit = entity.level().clip(new ClipContext(eye,
                eye.add(entity.getViewVector(1).scale(distance)), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, entity));
        if (hit.getType() != HitResult.Type.BLOCK) return null;
        Vec3 point = hit.getLocation().add(Vec3.atLowerCornerOf(hit.getDirection().getNormal()));
        var box = entity.getBoundingBox().move(point.subtract(entity.position()));
        return entity.level().getWorldBorder().isWithinBounds(box) && entity.level().noCollision(entity, box) ? point : null;
    }

    public void release() {
        if (!charging || entity.level().isClientSide) return;
        charging = false;
        Vec3 point = isActive() && entity.isAlive() && !entity.isPassenger() ? findDestination() : null;
        if (point == null) { refund(); return; }
        paid = false;
        destination = point;
        readyAt = entity.level().getGameTime() + cooldown;
        savedGravity = entity.isNoGravity();
        entity.setNoGravity(true);
        flightTicks = (int) Math.ceil(entity.position().distanceTo(point) / speed) + 20;
    }

    @Override public void tick() {
        if (entity.level().isClientSide) return;
        if (restoreGravity) { entity.setNoGravity(savedGravity); restoreGravity = false; }
        ticks++;
        if (!isActive() || !entity.isAlive() || entity.isPassenger()) { cancel(); return; }
        if (paid && !charging) refund(); // Reloaded reservations never resume an unattended charge.
        if (charging) {
            if (ticks - lastUse > 2) release();
            else if (entity instanceof ServerPlayer player) {
                Vec3 point = findDestination();
                if (point != null) player.serverLevel().sendParticles(player, ParticleTypes.CLOUD, true,
                        point.x, point.y, point.z, 2, 0.08, 0.08, 0.08, 0);
            }
        }
        if (destination == null) return;
        Vec3 delta = destination.subtract(entity.position());
        if (delta.lengthSqr() < 0.04 || flightTicks-- <= 0) { stopFlight(); return; }
        Vec3 step = delta.normalize().scale(Math.min(speed, delta.length()));
        var swept = entity.getBoundingBox().expandTowards(step);
        if (!entity.level().getWorldBorder().isWithinBounds(swept)) { stopFlight(); return; }
        for (var collision : entity.level().getBlockCollisions(entity, swept)) {
            if (!collision.isEmpty()) { stopFlight(); return; }
        }
        entity.fallDistance = 0;
        PerkActions.setVelocity(entity, step);
    }

    private void refund() {
        if (!paid) return;
        paid = false;
        if (entity instanceof Player player && manaType.equals(ManaUtils.getManaComponent(player).getManaTypeID())) {
            ManaUtils.getManaComponent(player).gainMana(manaCost);
        }
    }
    private void stopFlight() {
        if (destination == null) return;
        entity.setNoGravity(savedGravity);
        PerkActions.setVelocity(entity, Vec3.ZERO);
        destination = null;
    }
    private void cancel() { charging = false; refund(); stopFlight(); }
    @Override public void onRemoved() { if (!entity.level().isClientSide) cancel(); }
    @Override public Key getKey() { return key; }
    @Override public void setKey(Key key) { this.key = key; }
    @Override public Tag toTag(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        tag.putLong("readyAt", readyAt);
        tag.putBoolean("paid", paid);
        tag.putBoolean("inFlight", destination != null);
        tag.putBoolean("savedGravity", savedGravity);
        return tag;
    }
    @Override public void fromTag(Tag nbt, HolderLookup.Provider provider) {
        if (nbt instanceof CompoundTag tag) {
            readyAt = tag.getLong("readyAt"); paid = tag.getBoolean("paid");
            restoreGravity = tag.getBoolean("inFlight"); savedGravity = tag.getBoolean("savedGravity");
        }
    }
    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(ShapeShifterCurseFabric.identifier("surface_grapple"),
                new SerializableData().add("distance", SerializableDataTypes.DOUBLE, 12.0)
                        .add("speed", SerializableDataTypes.DOUBLE, 1.5)
                        .add("mana_cost", SerializableDataTypes.DOUBLE, 10.0)
                        .add("mana_type", SerializableDataTypes.IDENTIFIER, ShapeShifterCurseFabric.identifier("web_resource"))
                        .add("cooldown", SerializableDataTypes.INT, 100)
                        .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key()),
                data -> (type, entity) -> new SurfaceGrapplePower(type, entity, data)).allowCondition();
    }
}
