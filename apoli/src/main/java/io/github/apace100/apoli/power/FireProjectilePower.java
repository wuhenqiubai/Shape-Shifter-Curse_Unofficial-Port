package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.Consumer;

public class FireProjectilePower extends ActiveCooldownPower {

    private final EntityType<?> entityType;
    private final int projectileCount;
    private final int interval;
    private final int startDelay;
    private final float speed;
    private final float divergence;
    private final SoundEvent soundEvent;
    private final CompoundTag tag;
    private final Consumer<Entity> projectileAction;
    private final Consumer<Entity> shooterAction;

    private boolean isFiringProjectiles;
    private boolean finishedStartDelay;
    private int shotProjectiles;

    public FireProjectilePower(PowerType<?> type, LivingEntity entity, int cooldownDuration, HudRender hudRender, EntityType<?> entityType, int projectileCount, int interval, int startDelay, float speed, float divergence, SoundEvent soundEvent, CompoundTag tag, Consumer<Entity> projectileAction, Consumer<Entity> shooterAction) {
        super(type, entity, cooldownDuration, hudRender, null);
        this.entityType = entityType;
        this.projectileCount = projectileCount;
        this.interval = interval;
        this.startDelay = startDelay;
        this.speed = speed;
        this.divergence = divergence;
        this.soundEvent = soundEvent;
        this.tag = tag;
        this.projectileAction = projectileAction;
        this.shooterAction = shooterAction;
        this.setTicking(true);
    }

    @Override
    public void onUse() {
        if(canUse()) {
            isFiringProjectiles = true;
            use();
        }
    }

    @Override
    public Tag toTag(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putLong("LastUseTime", lastUseTime);
        nbt.putInt("ShotProjectiles", shotProjectiles);
        nbt.putBoolean("FinishedStartDelay", finishedStartDelay);
        nbt.putBoolean("IsFiringProjectiles", isFiringProjectiles);
        return nbt;
    }

    @Override
    public void fromTag(Tag tag, HolderLookup.Provider provider) {
        if(tag instanceof LongTag) {
            lastUseTime = ((LongTag)tag).getAsLong();
        }
        else {
            lastUseTime = ((CompoundTag)tag).getLong("LastUseTime");
            shotProjectiles = ((CompoundTag)tag).getInt("ShotProjectiles");
            finishedStartDelay = ((CompoundTag)tag).getBoolean("FinishedStartDelay");
            isFiringProjectiles = ((CompoundTag)tag).getBoolean("IsFiringProjectiles");
        }
    }

    public void tick() {
        if(isFiringProjectiles) {
            if(!finishedStartDelay && startDelay == 0) {
                finishedStartDelay = true;
            }
            if(!finishedStartDelay && (entity.getCommandSenderWorld().getGameTime() - lastUseTime) % startDelay == 0) {
                finishedStartDelay = true;
                shotProjectiles += 1;
                if(shotProjectiles <= projectileCount) {
                    if(soundEvent != null) {
                        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), soundEvent, SoundSource.NEUTRAL, 0.5F, 0.4F / (entity.getRandom().nextFloat() * 0.4F + 0.8F));
                    }
                    if(!entity.level().isClientSide) {
                        fireProjectile();
                    }
                }
                else {
                    shotProjectiles = 0;
                    finishedStartDelay = false;
                    isFiringProjectiles = false;
                }
            }
            else if(interval == 0 && finishedStartDelay) {
                if(soundEvent != null) {
                    entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), soundEvent, SoundSource.NEUTRAL, 0.5F, 0.4F / (entity.getRandom().nextFloat() * 0.4F + 0.8F));
                }
                if(!entity.level().isClientSide) {
                    for(; shotProjectiles < projectileCount; shotProjectiles++) {
                        fireProjectile();
                    }
                }
                shotProjectiles = 0;
                finishedStartDelay = false;
                isFiringProjectiles = false;
            }
            else if (finishedStartDelay && (entity.getCommandSenderWorld().getGameTime() - lastUseTime) % interval == 0) {
                shotProjectiles += 1;
                if(shotProjectiles <= projectileCount) {
                    if(soundEvent != null) {
                        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), soundEvent, SoundSource.NEUTRAL, 0.5F, 0.4F / (entity.getRandom().nextFloat() * 0.4F + 0.8F));
                    }
                    if(!entity.level().isClientSide) {
                        fireProjectile();
                    }
                }
                else {
                    shotProjectiles = 0;
                    finishedStartDelay = false;
                    isFiringProjectiles = false;
                }
            }
        }
    }

    private void fireProjectile() {

        if (entityType == null || entity.level().isClientSide) return;

        ServerLevel serverWorld = (ServerLevel) entity.level();
        float yaw = entity.getYRot();
        float pitch = entity.getXRot();

        Optional<Entity> opt$entityToSpawn = MiscUtil.getEntityWithPassengers(
            serverWorld,
            entityType,
            tag,
            entity.position().add(0, entity.getEyeHeight(entity.getPose()), 0),
            yaw,
            pitch
        );

        if (opt$entityToSpawn.isEmpty()) return;

        Vec3 rotationVector = entity.getLookAngle();
        Vec3 velocity = entity.getDeltaMovement();
        Entity entityToSpawn = opt$entityToSpawn.get();
        RandomSource random = serverWorld.getRandom();

        if (entityToSpawn instanceof Projectile projectileToSpawn) {

            if (projectileToSpawn instanceof AbstractHurtingProjectile explosiveProjectileToSpawn) {
                explosiveProjectileToSpawn.accelerationPower = speed;
            }

            projectileToSpawn.setOwner(entity);
            projectileToSpawn.shootFromRotation(entity, pitch, yaw, 0F, speed, divergence);

        }

        else {

            float  f = 0.017453292F;
            double g = 0.007499999832361937D;

            float h = -Mth.sin(yaw * f) * Mth.cos(pitch * f);
            float i = -Mth.sin(pitch * f);
            float j =  Mth.cos(yaw * f) * Mth.cos(pitch * f);

            Vec3 vec3d = new Vec3(h, i, j)
                .normalize()
                .add(random.nextGaussian() * g * divergence, random.nextGaussian() * g * divergence, random.nextGaussian() * g * divergence)
                .scale(speed);

            entityToSpawn.setDeltaMovement(vec3d);
            entityToSpawn.push(velocity.x, entity.onGround() ? 0.0D : velocity.y, velocity.z);

        }

        serverWorld.tryAddFreshEntityWithPassengers(entityToSpawn);
        if (projectileAction != null) projectileAction.accept(entityToSpawn);
        if (shooterAction != null) shooterAction.accept(entity);

    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("fire_projectile"),
            new SerializableData()
                .add("cooldown", SerializableDataTypes.INT, 1)
                .add("count", SerializableDataTypes.INT, 1)
                .add("interval", SerializableDataTypes.INT, 0)
                .add("start_delay", SerializableDataTypes.INT, 0)
                .add("speed", SerializableDataTypes.FLOAT, 1.5F)
                .add("divergence", SerializableDataTypes.FLOAT, 1F)
                .add("sound", SerializableDataTypes.SOUND_EVENT, null)
                .add("entity_type", SerializableDataTypes.ENTITY_TYPE)
                .add("hud_render", ApoliDataTypes.HUD_RENDER, HudRender.DONT_RENDER)
                .add("tag", SerializableDataTypes.NBT, null)
                .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key())
                .add("projectile_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("shooter_action", ApoliDataTypes.ENTITY_ACTION, null),
            data -> (powerType, livingEntity) -> {

                FireProjectilePower fpp = new FireProjectilePower(
                    powerType,
                    livingEntity,
                    data.get("cooldown"),
                    data.get("hud_render"),
                    data.get("entity_type"),
                    data.get("count"),
                    data.get("interval"),
                    data.get("start_delay"),
                    data.get("speed"),
                    data.get("divergence"),
                    data.get("sound"),
                    data.get("tag"),
                    data.get("projectile_action"),
                    data.get("shooter_action")
                );

                fpp.setKey(data.get("key"));
                return fpp;

            }
        ).allowCondition();
    }
}
