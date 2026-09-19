package io.github.apace100.apoli.mixin.integration.connector;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import io.github.apace100.apoli.access.MovingEntity;
import io.github.apace100.apoli.access.SubmergableEntity;
import io.github.apace100.apoli.access.WaterMovingEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.*;
import io.github.apace100.calio.Calio;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;

/**
 * NeoForge/Connector 兼容：主 EntityMixin 的 phasing 防窒息用 @Redirect(method_30022) 依赖 isInWall 的 lambda，
 * NeoForge 重编译使 lambda 编号变化而失效。此版本是主 EntityMixin 的完整替代，仅把 phasing 注入点改为
 * isInWall HEAD（方法名/描述符两版一致，最稳）。由 ApoliMixinPlugin 在 NeoForge 下启用、Fabric 下跳过。
 */
@Mixin(Entity.class)
public abstract class EntityMixin implements MovingEntity, SubmergableEntity {

    @Inject(method = "fireImmune", at = @At("HEAD"), cancellable = true)
    private void makeFullyFireImmune(CallbackInfoReturnable<Boolean> cir) {
        if(PowerHolderComponent.hasPower((Entity)(Object)this, FireImmunityPower.class)) {
            cir.setReturnValue(true);
        }
    }

    @Shadow
    public Level level;

    @Shadow
    public abstract double getFluidHeight(TagKey<Fluid> fluid);

    @Shadow
    public float moveDist;

    @Shadow
    protected boolean onGround;

    @Shadow @Nullable protected Set<TagKey<Fluid>> fluidOnEyes;

    @Shadow protected Object2DoubleMap<TagKey<Fluid>> fluidHeight;

    @Shadow public abstract boolean isSwimming();

    @Inject(method = "isInWater", at = @At("HEAD"), cancellable = true)
    private void makeEntitiesIgnoreWater(CallbackInfoReturnable<Boolean> cir) {
        if(PowerHolderComponent.hasPower((Entity)(Object)this, IgnoreWaterPower.class)) {
            if(this instanceof WaterMovingEntity) {
                if(((WaterMovingEntity)this).isInMovementPhase()) {
                    cir.setReturnValue(false);
                }
            }
        }
    }

    @Inject(method = "checkFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;fallOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;F)V"))
    private void invokeActionOnLand(CallbackInfo ci) {
        List<ActionOnLandPower> powers = PowerHolderComponent.getPowers((Entity)(Object)this, ActionOnLandPower.class);
        powers.forEach(ActionOnLandPower::executeAction);
    }

    @Inject(at = @At("HEAD"), method = "isInvulnerableTo", cancellable = true)
    private void makeOriginInvulnerable(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if((Object)this instanceof LivingEntity) {
            PowerHolderComponent component = PowerHolderComponent.KEY.get(this);
            if(component.getPowers(InvulnerablePower.class).stream().anyMatch(inv -> inv.doesApply(damageSource))) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "clearFire", at = @At("HEAD"), cancellable = true)
    private void preventExtinguishingFromSwimming(CallbackInfo ci) {
        if(PowerHolderComponent.hasPower((Entity) (Object) this, SwimmingPower.class) && this.isSwimming() && !(getFluidHeight(FluidTags.WATER) > 0)) {
            ci.cancel();
        }
    }

    @WrapWithCondition(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setRemainingFireTicks(I)V"))
    private boolean preventExtinguishingFromSwimming(Entity instance, int remainingFireTicks) {
        return !(PowerHolderComponent.hasPower(instance, SwimmingPower.class) && instance.isSwimming() && !(getFluidHeight(FluidTags.WATER) > 0));
    }

    @Inject(at = @At("HEAD"), method = "isInvisible", cancellable = true)
    private void phantomInvisibility(CallbackInfoReturnable<Boolean> info) {
        if(PowerHolderComponent.hasPower((Entity)(Object)this, InvisibilityPower.class)) {
            info.setReturnValue(true);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;containing(DDD)Lnet/minecraft/core/BlockPos;"), method = "moveTowardsClosestSpace", cancellable = true)
    protected void pushOutOfBlocks(double x, double y, double z, CallbackInfo info) {
        List<PhasingPower> powers = PowerHolderComponent.getPowers((Entity)(Object)this, PhasingPower.class);
        if(powers.size() > 0) {
            if(powers.stream().anyMatch(phasingPower -> phasingPower.doesApply(BlockPos.containing(x, y, z)))) {
                info.cancel();
            }
        }
    }

    // NeoForge 安全注入点：原主版 @Redirect(method_30022, getCollisionShape) 依赖 isInWall 的 lambda（编号不稳定）。
    // 改 isInWall HEAD——PhasingPower 激活时直接返回 false（阻止窒息判定），两版方法名/描述符一致。
    @Inject(method = "isInWall", at = @At("HEAD"), cancellable = true)
    private void preventPhasingSuffocation(CallbackInfoReturnable<Boolean> cir) {
        PowerHolderComponent component = PowerHolderComponent.KEY.get(this);
        if (component.getPowers(PhasingPower.class).stream().anyMatch(PhasingPower::isActive)) {
            cir.setReturnValue(false);
        }
    }

    private boolean isMoving;
    private float distanceBefore;

    @Inject(method = "move", at = @At("HEAD"))
    private void saveDistanceTraveled(MoverType type, Vec3 movement, CallbackInfo ci) {
        this.isMoving = false;
        this.distanceBefore = this.moveDist;
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V"))
    private void checkIsMoving(MoverType type, Vec3 movement, CallbackInfo ci) {
        if (this.moveDist > this.distanceBefore) {
            this.isMoving = true;
        }
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true)
    private Vec3 modifyMovementVelocity(Vec3 original, MoverType movementType) {
        if(movementType != MoverType.SELF) {
            return original;
        }
        Vec3 modified = new Vec3(
            PowerHolderComponent.modify((Entity)(Object)this, ModifyVelocityPower.class, original.x, p -> p.axes.contains(Direction.Axis.X), null),
            PowerHolderComponent.modify((Entity)(Object)this, ModifyVelocityPower.class, original.y, p -> p.axes.contains(Direction.Axis.Y), null),
            PowerHolderComponent.modify((Entity)(Object)this, ModifyVelocityPower.class, original.z, p -> p.axes.contains(Direction.Axis.Z), null)
        );
        return modified;
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getOnPosLegacy()Lnet/minecraft/core/BlockPos;"))
    private void forceGrounded(MoverType movementType, Vec3 movement, CallbackInfo ci) {
        if(PowerHolderComponent.hasPower((Entity)(Object)this, GroundedPower.class)) {
            this.onGround = true;
        }
    }

    @Override
    public boolean isSubmergedInLoosely(TagKey<Fluid> tag) {
        if(tag == null || fluidOnEyes == null) {
            return false;
        }
        if(fluidOnEyes.contains(tag)) {
            return true;
        }
        return false;
    }

    @Override
    public double getFluidHeightLoosely(TagKey<Fluid> tag) {
        if(tag == null) {
            return 0;
        }
        if(fluidHeight.containsKey(tag)) {
            return fluidHeight.getDouble(tag);
        }
        for(TagKey<Fluid> ft : fluidHeight.keySet()) {
            if(Calio.areTagsEqual(Registries.FLUID, ft, tag)) {
                return fluidHeight.getDouble(ft);
            }
        }
        return 0;
    }

    @Override
    public boolean isMoving() {
        return isMoving;
    }
}
