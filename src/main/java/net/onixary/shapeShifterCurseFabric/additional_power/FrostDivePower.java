package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.CooldownPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.LongTag;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import java.util.function.Consumer;

public class FrostDivePower extends CooldownPower implements Active {
    private final double speed, minimumHeight;
    private final Consumer<Entity> startAction, landingAction;
    private Key key;
    private boolean diving;
    private double startY;
    private Vec3 previousPosition;
    private Level startWorld;

    public FrostDivePower(PowerType<?> type, LivingEntity entity, SerializableData.Instance data) {
        super(type, entity, data.getInt("cooldown"), HudRender.DONT_RENDER);
        speed = data.getDouble("speed"); minimumHeight = data.getDouble("minimum_height");
        startAction = data.get("start_action"); landingAction = data.get("landing_action");
        key = data.get("key"); setTicking(true);
    }
    @Override public Key getKey() { return key; }
    @Override public void setKey(Key key) { this.key = key; }
    @Override public void onUse() {
        if (entity.level().isClientSide || !canUse() || diving || !airborne(entity)) return;
        diving = true; startY = entity.getY(); startWorld = entity.level(); previousPosition = entity.position();
        startAction.accept(entity);
        entity.fallDistance = 0;
        PerkActions.setVelocity(entity, new Vec3(0, -speed, 0));
        use();
    }
    public static boolean airborne(LivingEntity entity) {
        return !entity.onGround() && !entity.isInWater() && !entity.isInLava()
                && !entity.isPassenger() && !entity.isFallFlying() && !entity.onClimbable()
                && !(entity instanceof Player player && player.getAbilities().flying);
    }
    public static boolean isDiving(Entity entity) {
        return entity instanceof LivingEntity && PowerHolderComponent.KEY.get(entity).getPowers(FrostDivePower.class, true)
                .stream().anyMatch(p -> p.diving);
    }
    public static Vec3 constrainMovement(Entity entity, Vec3 movement) {
        if (!isDiving(entity)) return movement;
        // Upward skills, knockback and steering cannot interrupt an active dive.
        return new Vec3(0, Math.min(movement.y, 0), 0);
    }
    @Override public void tick() {
        if (!diving) return;
        entity.fallDistance = 0;
        if (!entity.level().isClientSide) {
            if (!entity.isAlive() || entity.level() != startWorld
                    || previousPosition.distanceToSqr(entity.position()) > 64) { stop(); return; }
            if (entity.onGround()) {
                boolean impact = startY - entity.getY() > minimumHeight;
                stop();
                if (impact) landingAction.accept(entity);
                return;
            }
            if (!airborne(entity)) { stop(); return; }
            previousPosition = entity.position();
        }
        PerkActions.setVelocity(entity, new Vec3(0, -speed, 0));
    }
    private void stop() {
        diving = false; entity.fallDistance = 0;
        if (!entity.level().isClientSide) PowerHolderComponent.syncPower(entity, getType());
    }
    @Override public void onRemoved() { stop(); }
    @Override public Tag toTag(HolderLookup.Provider provider) {
        var tag = new CompoundTag(); tag.put("cooldown", super.toTag(provider)); tag.putBoolean("diving", diving); return tag;
    }
    @Override public void fromTag(Tag tag, HolderLookup.Provider provider) {
        if (tag instanceof CompoundTag data) {
            super.fromTag(data.contains("cooldown") ? data.get("cooldown") : LongTag.valueOf(0), provider);
            // Only network clients resume the transient state; never resume it after login.
            diving = entity.level().isClientSide && data.getBoolean("diving");
        }
    }
    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(ShapeShifterCurseFabric.identifier("frost_dive"), new SerializableData()
                .add("speed", SerializableDataTypes.DOUBLE, 0.8)
                .add("minimum_height", SerializableDataTypes.DOUBLE, 4.0)
                .add("cooldown", SerializableDataTypes.INT, 160)
                .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key())
                .add("start_action", ApoliDataTypes.ENTITY_ACTION)
                .add("landing_action", ApoliDataTypes.ENTITY_ACTION),
                data -> (type, entity) -> new FrostDivePower(type, entity, data)).allowCondition();
    }
}
