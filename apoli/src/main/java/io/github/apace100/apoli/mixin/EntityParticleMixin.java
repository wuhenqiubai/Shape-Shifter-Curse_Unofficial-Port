package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ParticlePower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntity.class)
public abstract class EntityParticleMixin extends Entity {

    public EntityParticleMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void tick(CallbackInfo info) {
        LocalPlayer player = Minecraft.getInstance().player;
        if(player != null) {
            boolean firstPerson = Minecraft.getInstance().options.getCameraType().isFirstPerson();
            PowerHolderComponent component = PowerHolderComponent.KEY.get(this);
            List<ParticlePower> particlePowers = component.getPowers(ParticlePower.class);
            for (ParticlePower particlePower : particlePowers) {
                if(!this.isInvisibleTo(player) || particlePower.isVisibleWhileInvisible()) {
                    if (((Object) this != player || (!firstPerson || particlePower.isVisibleInFirstPerson()))) {
                        if (this.tickCount % particlePower.getFrequency() == 0 && particlePower.getCount() > 0 && particlePower.getSpeed() >= 0) {
                            Vec3 spread = particlePower.getSpread();
                            for (int i = 0; i < particlePower.getCount(); i++) {
                                level().addParticle(particlePower.getParticle(), this.getX() + this.random.nextGaussian() * spread.x(), particlePower.getOffset_y() + this.getY() + this.random.nextGaussian() * spread.y(), this.getZ() + this.random.nextGaussian() * spread.z(), (2.0 * this.random.nextDouble() - 1.0) * particlePower.getSpeed(), (2.0 * this.random.nextDouble() - 1.0) *particlePower.getSpeed(), (2.0 * this.random.nextDouble() - 1.0) *particlePower.getSpeed());
                            }
                        }
                    }
                }
            }
        }
    }
}