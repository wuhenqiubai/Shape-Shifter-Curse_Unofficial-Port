package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ParticlePower extends Power {

    private final ParticleOptions particleEffect;
    private final int frequency;
    private final boolean visibleInFirstPerson;

    private final Vec3 spread;

    private final float offset_y;

    private final int count;

    private final boolean visibleWhileInvisible;

    private final float speed;

    public ParticlePower(PowerType<?> type, LivingEntity entity, ParticleOptions particle, int frequency, boolean visibleInFirstPerson, Vec3 spread, float offset_y, int count, boolean visibleWhileInvisible, float speed) {
        super(type, entity);
        this.particleEffect = particle;
        this.frequency = frequency;
        this.visibleInFirstPerson = visibleInFirstPerson;
        this.spread = spread;
        this.offset_y = offset_y;
        this.count = count;
        this.visibleWhileInvisible = visibleWhileInvisible;
        this.speed = speed;
    }

    public ParticleOptions getParticle() {
        return particleEffect;
    }

    public int getFrequency() {
        return frequency;
    }

    public boolean isVisibleInFirstPerson() {
        return visibleInFirstPerson;
    }

    public Vec3 getSpread() { return spread; }

    public float getOffset_y() { return offset_y; }

    public int getCount() { return count; }

    public boolean isVisibleWhileInvisible() { return visibleWhileInvisible; }

    public float getSpeed() { return speed; }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("particle"),
                new SerializableData()
                        .add("particle", SerializableDataTypes.PARTICLE_EFFECT_OR_TYPE)
                        .add("frequency", SerializableDataTypes.INT)
                        .add("visible_in_first_person", SerializableDataTypes.BOOLEAN, false)
                        .add("spread", SerializableDataTypes.VECTOR, new Vec3(0.25, 0.5, 0.25))
                        .add("offset_y", SerializableDataTypes.FLOAT, 1.0F)
                        .add("count", SerializableDataTypes.INT, 1)
                        .add("visible_while_invisible", SerializableDataTypes.BOOLEAN, false)
                        .add("speed", SerializableDataTypes.FLOAT, 0.0F),
                data ->
                        (type, player) ->
                                new ParticlePower(type, player, data.get("particle"), data.getInt("frequency"), data.getBoolean("visible_in_first_person"), data.get("spread"), data.getFloat("offset_y"), data.getInt("count"), data.getBoolean("visible_while_invisible"), data.getFloat("speed")))
                .allowCondition();
    }
}