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
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

/** Configurable extra impulses per airborne interval; never invokes LivingEntity.jump(). */
public class AirJumpPower extends Power implements Active {
    private final double speed;
    private Key key;
    private final int jumps;
    private int used;
    private int airTicks;
    public AirJumpPower(PowerType<?> type, LivingEntity entity, SerializableData.Instance data) {
        super(type, entity); speed = data.getDouble("speed"); jumps = Math.max(1, data.getInt("jumps")); key = data.get("key"); setTicking(true);
    }
    @Override public Key getKey() { return key; }
    @Override public void setKey(Key key) { this.key = key; }
    @Override public void tick() {
        if (entity.onGround()) { used = 0; airTicks = 0; }
        else airTicks++;
    }
    @Override public void onUse() {
        if (entity.level().isClientSide || !isActive() || used >= jumps || airTicks < 2
                || !FrostDivePower.airborne(entity) || FrostDivePower.isDiving(entity)) return;
        used++;
        Vec3 velocity = entity.getDeltaMovement();
        PerkActions.setVelocity(entity, new Vec3(velocity.x, speed, velocity.z));
    }
    @Override public Tag toTag(HolderLookup.Provider provider) { return IntTag.valueOf(used); }
    // Accept the old boolean byte as a spent single jump.
    @Override public void fromTag(Tag tag, HolderLookup.Provider provider) { used = tag instanceof NumericTag value ? Math.max(0, value.getAsInt()) : 0; }
    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(ShapeShifterCurseFabric.identifier("air_jump"), new SerializableData()
                .add("speed", SerializableDataTypes.DOUBLE, 0.6)
                .add("jumps", SerializableDataTypes.INT, 1)
                .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key()),
                data -> (type, entity) -> new AirJumpPower(type, entity, data)).allowCondition();
    }
}
