package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.HiddenEffectStatus;
import io.github.apace100.apoli.access.ModifiableFoodEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.networking.SetAttackerPacket;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.apoli.util.StackPowerUtil;
import io.github.apace100.apoli.util.SyncStatusEffectsUtil;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.function.BiConsumer;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ModifiableFoodEntity {
    @Shadow
    protected abstract float getJumpPower();

    @Shadow
    private Optional<BlockPos> lastClimbablePos;

    @Shadow
    public abstract boolean isSuppressingSlidingDownLadder();

    @Shadow
    public abstract void setHealth(float health);

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "onEffectAdded", at = @At("TAIL"))
    private void updateStatusEffectWhenApplied(MobEffectInstance effectInstance, Entity source, CallbackInfo ci) {
        SyncStatusEffectsUtil.sendStatusEffectUpdatePacket((LivingEntity)(Object)this, SyncStatusEffectsUtil.UpdateType.APPLY, effectInstance);
    }

    @Inject(method = "onEffectUpdated", at = @At("TAIL"))
    private void updateStatusEffectWhenUpgraded(MobEffectInstance effectInstance, boolean reapplyEffect, Entity source, CallbackInfo ci) {
        SyncStatusEffectsUtil.sendStatusEffectUpdatePacket((LivingEntity)(Object)this, SyncStatusEffectsUtil.UpdateType.UPGRADE, effectInstance);
    }

    @Inject(method = "onEffectsRemoved", at = @At("TAIL"))
    private void updateStatusEffectWhenRemoved(Collection<MobEffectInstance> effects, CallbackInfo ci) {
        for (MobEffectInstance effectInstance : effects) {
            SyncStatusEffectsUtil.sendStatusEffectUpdatePacket((LivingEntity)(Object)this, SyncStatusEffectsUtil.UpdateType.REMOVE, effectInstance);
        }
    }

    @Inject(method = "removeAllEffects", at = @At("RETURN"))
    private void updateStatusEffectWhenCleared(CallbackInfoReturnable<Boolean> cir) {
        SyncStatusEffectsUtil.sendStatusEffectUpdatePacket((LivingEntity)(Object)this, SyncStatusEffectsUtil.UpdateType.CLEAR, null);
    }

    @ModifyVariable(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"))
    private MobEffectInstance modifyStatusEffect(MobEffectInstance effect) {
        Holder<MobEffect> effectType = effect.getEffect();
        int originalAmp = effect.getAmplifier();
        int originalDur = effect.getDuration();

        int amplifier = Math.round(PowerHolderComponent.modify(this, ModifyStatusEffectAmplifierPower.class, originalAmp, power -> power.doesApply(effectType.value())));
        int duration = Math.round(PowerHolderComponent.modify(this, ModifyStatusEffectDurationPower.class, originalDur, power -> power.doesApply(effectType.value())));

        if (amplifier != originalAmp || duration != originalDur) {
            return new MobEffectInstance(
                    effectType,
                    duration,
                    amplifier,
                    effect.isAmbient(),
                    effect.isVisible(),
                    effect.showIcon(),
                    ((HiddenEffectStatus) effect).getHiddenEffect()
            );
        }
        return effect;
    }

    @Inject(method = "setLastHurtByMob", at = @At("TAIL"))
    private void syncAttacker(LivingEntity attacker, CallbackInfo ci) {
        if(!level().isClientSide()) {
            for (ServerPlayer player : PlayerLookup.tracking(this)) {
                ServerPlayNetworking.send(player, new SetAttackerPacket(getId(), this.lastHurtByMob == null ? Optional.empty() : Optional.of(this.lastHurtByMob.getEntity(this.level(), LivingEntity.class).getId())));
            }
        }
    }

    @Inject(method = "collectEquipmentChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V"))
    private void removeEquipmentPowers(CallbackInfoReturnable<Map> cir, @Local(ordinal = 0) ItemStack stack, @Local EquipmentSlot equipmentSlot) {
        List<StackPowerUtil.StackPower> powers = StackPowerUtil.getPowers(stack, equipmentSlot);
        if(powers.size() > 0) {
            Identifier source = Identifier.fromNamespaceAndPath(Apoli.MODID, equipmentSlot.getName());
            PowerHolderComponent powerHolder = PowerHolderComponent.KEY.get(this);
            powers.forEach(sp -> {
                if(PowerTypeRegistry.contains(sp.powerId)) {
                    powerHolder.removePower(PowerTypeRegistry.get(sp.powerId), source);
                }
            });
            powerHolder.sync();
        }
    }

    // O-L: We're not using an inject because otherwise the second ItemStack local isn't captured.
    @WrapOperation(method = "collectEquipmentChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V"))
    private void addEquipmentPowers(ItemStack stack2, EquipmentSlot equipmentSlot, BiConsumer<Holder<Attribute>, AttributeModifier> action, Operation<Void> original, @Local(ordinal = 0) ItemStack stack) {
        original.call(stack2, equipmentSlot, action);

        List<StackPowerUtil.StackPower> powers = StackPowerUtil.getPowers(stack2, equipmentSlot);
        if(powers.size() > 0) {
            Identifier source = Identifier.fromNamespaceAndPath(Apoli.MODID, equipmentSlot.getName());
            PowerHolderComponent powerHolder = PowerHolderComponent.KEY.get(this);
            powers.forEach(sp -> {
                if(PowerTypeRegistry.contains(sp.powerId)) {
                    powerHolder.addPower(PowerTypeRegistry.get(sp.powerId), source);
                }
            });
            powerHolder.sync();
        } else if(StackPowerUtil.getPowers(stack, equipmentSlot).size() > 0) {
            PowerHolderComponent.KEY.get(this).sync();
        }

    }

    @Inject(method = "canStandOnFluid", at = @At("HEAD"), cancellable = true)
    private void modifyWalkableFluids(FluidState fluidState, CallbackInfoReturnable<Boolean> cir) {
        if(PowerHolderComponent.getPowers(this, WalkOnFluidPower.class).stream().anyMatch(p -> fluidState.is(p.getFluidTag()))) {
            cir.setReturnValue(true);
        }
    }

    @ModifyVariable(method = "heal", at = @At("HEAD"), argsOnly = true)
    private float modifyHealingApplied(float originalValue) {
        return PowerHolderComponent.modify(this, ModifyHealingPower.class, originalValue);
    }

    private boolean apoli$hasModifiedDamage;
    private Optional<Boolean> apoli$shouldApplyArmor;
    private Optional<Boolean> apoli$shouldDamageArmor;

    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true)
    private float modifyDamageTaken(float originalValue, @Local(argsOnly = true) DamageSource source) {
        float newValue = originalValue;
        LivingEntity thisAsLiving = (LivingEntity)(Object)this;
        if(source.getEntity() != null) {
            if (!source.is(DamageTypeTags.IS_PROJECTILE)) {
                newValue = PowerHolderComponent.modify(source.getEntity(), ModifyDamageDealtPower.class, originalValue,
                    p -> p.doesApply(source, originalValue, thisAsLiving), p -> p.executeActions(thisAsLiving));
            } else {
                newValue = PowerHolderComponent.modify(
                    source.getEntity(), ModifyProjectileDamagePower.class, originalValue,
                    p -> p.doesApply(source, originalValue, thisAsLiving), p -> p.executeActions(thisAsLiving));
            }
        }

        float intermediateValue = newValue;
        newValue = PowerHolderComponent.modify(this, ModifyDamageTakenPower.class,
            intermediateValue, p -> p.doesApply(source, intermediateValue), p -> p.executeActions(source.getEntity()));

        apoli$hasModifiedDamage = newValue != originalValue;

        List<ModifyDamageTakenPower> mdtps = PowerHolderComponent.getPowers(this, ModifyDamageTakenPower.class).stream().filter(p -> p.doesApply(source, originalValue)).toList();
        long wantArmor = mdtps.stream().filter(p -> p.modifiesArmorApplicance() && p.shouldApplyArmor()).count();
        long dontWantArmor = mdtps.stream().filter(p -> p.modifiesArmorApplicance() && !p.shouldApplyArmor()).count();
        apoli$shouldApplyArmor = wantArmor == dontWantArmor ? Optional.empty() : Optional.of(wantArmor > dontWantArmor);
        long wantDamage = mdtps.stream().filter(p -> p.modifiesArmorDamaging() && p.shouldDamageArmor()).count();
        long dontWantDamage = mdtps.stream().filter(p -> p.modifiesArmorDamaging() && !p.shouldDamageArmor()).count();
        apoli$shouldDamageArmor = wantDamage == dontWantDamage ? Optional.empty() : Optional.of(wantDamage > dontWantDamage);

        return newValue;
    }

    @Inject(method = "getDamageAfterArmorAbsorb", at = @At("HEAD"), cancellable = true)
    private void modifyArmorApplicance(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        if(apoli$shouldApplyArmor.isPresent()) {
            if(apoli$shouldDamageArmor.isPresent() && apoli$shouldDamageArmor.get()) {
                this.hurtArmor(source, amount);
            }
            if(apoli$shouldApplyArmor.get()) {
                if(apoli$shouldDamageArmor.isEmpty()) {
                    this.hurtArmor(source, amount);
                }
                float damageLeft = CombatRules.getDamageAfterAbsorb((LivingEntity) (Object) this, amount, source, this.getArmorValue(), (float)this.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
                cir.setReturnValue(damageLeft);
            } else {
                cir.setReturnValue(amount);
            }
        } else {
            if(apoli$shouldDamageArmor.isPresent()) {
                if(apoli$shouldDamageArmor.get() && source.is(DamageTypeTags.BYPASSES_ARMOR)) {
                    this.hurtArmor(source, amount);
                }
            }
        }
    }

    @WrapWithCondition(method = "getDamageAfterArmorAbsorb", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurtArmor(Lnet/minecraft/world/damagesource/DamageSource;F)V"))
    private boolean preventArmorDamaging(LivingEntity instance, DamageSource source, float amount) {
        return !apoli$shouldDamageArmor.isPresent() || apoli$shouldDamageArmor.get();
    }

    @Inject(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSleeping()Z"), cancellable = true)
    private void preventHitIfDamageIsZero(ServerLevel level, DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        if(apoli$hasModifiedDamage && amount <= 0f) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void invokeHitActions(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if(cir.getReturnValue()) {
            Entity attacker = source.getEntity();
            if(attacker != null) {
                PowerHolderComponent.withPowers(this, ActionWhenHitPower.class, p -> true, p -> p.whenHit(attacker, source, amount));
                PowerHolderComponent.withPowers(attacker, ActionOnHitPower.class, p -> true, p -> p.onHit(this, source, amount));
            }
            PowerHolderComponent.getPowers(this, SelfActionWhenHitPower.class).forEach(p -> p.whenHit(source, amount));
            PowerHolderComponent.getPowers(this, AttackerActionWhenHitPower.class).forEach(p -> p.whenHit(source, amount));
            PowerHolderComponent.getPowers(source.getEntity(), SelfActionOnHitPower.class).forEach(p -> p.onHit((LivingEntity)(Object)this, source, amount));
            PowerHolderComponent.getPowers(source.getEntity(), TargetActionOnHitPower.class).forEach(p -> p.onHit((LivingEntity)(Object)this, source, amount));

        }
    }

    @Inject(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;die(Lnet/minecraft/world/damagesource/DamageSource;)V"))
    private void invokeKillAction(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PowerHolderComponent.getPowers(source.getEntity(), SelfActionOnKillPower.class).forEach(p -> p.onKill((LivingEntity)(Object)this, source, amount));
    }

    /*@WrapOperation(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isInWaterRainOrBubble()Z"))
    private boolean preventExtinguishingFromSwimming(LivingEntity livingEntity, Operation<Boolean> original) {
        if(PowerHolderComponent.hasPower(livingEntity, SwimmingPower.class) && livingEntity.isSwimming() && !(getFluidHeight(FluidTags.WATER) > 0)) {
            return false;
        }
        return original.call(livingEntity);
    }*/

    @Unique
    private boolean prevPowderSnowState = false;

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getTicksFrozen()I"))
    private void freezeEntityFromPower(CallbackInfo ci) {
        if(PowerHolderComponent.hasPower(this, FreezePower.class)) {
            this.prevPowderSnowState = this.isInPowderSnow;
            this.isInPowderSnow = true;
        }
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;removeFrost()V"))
    private void unfreezeEntityFromPower(CallbackInfo ci) {
        if(PowerHolderComponent.hasPower(this, FreezePower.class)) {
            this.isInPowderSnow = this.prevPowderSnowState;
        }
    }

    @Inject(method = "canFreeze", at = @At("RETURN"), cancellable = true)
    private void allowFreezingPower(CallbackInfoReturnable<Boolean> cir) {
        if(PowerHolderComponent.hasPower(this, FreezePower.class)) {
            cir.setReturnValue(true);
        }
    }

    // SPRINT_JUMP
    @ModifyExpressionValue(method = "jumpFromGround", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getJumpPower()F"))
    private float modifyJumpVelocity(float original) {
        return PowerHolderComponent.modify(this, ModifyJumpPower.class, original, p -> {
            p.executeAction();
            return true;
        });
    }

    // HOTBLOODED
    @Inject(at = @At("HEAD"), method= "canBeAffected", cancellable = true)
    private void preventStatusEffects(MobEffectInstance effect, CallbackInfoReturnable<Boolean> info) {
        for (EffectImmunityPower power : PowerHolderComponent.getPowers(this, EffectImmunityPower.class)) {
            if(power.doesApply(effect)) {
                info.setReturnValue(false);
                return;
            }
        }
    }

    // CLIMBING
    @ModifyReturnValue(at = @At("RETURN"), method = "onClimbable")
    public boolean doSpiderClimbing(boolean original) {
        if(!original) {
            if((Entity)this instanceof LivingEntity) {
                List<ClimbingPower> climbingPowers = PowerHolderComponent.KEY.get(this).getPowers(ClimbingPower.class, true);
                // TODO: Rethink how "holding" is implemented
                if(climbingPowers.size() > 0) {
                    if(climbingPowers.stream().anyMatch(ClimbingPower::isActive)) {
                        BlockPos pos = blockPosition();
                        this.lastClimbablePos = Optional.of(pos);
                        //origins_lastClimbingPos = getPos();
                        return true;
                    } else if(isSuppressingSlidingDownLadder()) {
                        //if(origins_lastClimbingPos != null && isHoldingOntoLadder()) {
                            // [移植 f3bd124] 回归修复：canHold 分支原只查 isSuppressingSlidingDownLadder()（潜行时恒 true）
                            // + canHold，导致潜行时任意场景 onClimbable 恒 true → 原版攀爬（空格上升/悬停）误触发。
                            // 改用「ClimbingPower.isActive()（真正贴墙爬墙中）+ canHold」：开阔地（isActive false）不触发，
                            // 贴墙爬墙（isActive true）悬停正常。
                            if(climbingPowers.stream().anyMatch(p -> p.isActive() && p.canHold())) {
                                return true;
                            }
                        //}
                    }
                }
            }
        }

        return original;
    }

    // SLOW_FALLING
    @ModifyReturnValue(at = @At("RETURN"), method = "getEffectiveGravity")
    public double modifyFallingVelocity(double in) {
        if(this.getDeltaMovement().y > 0D) {
            return in;
        }
        List<ModifyFallingPower> modifyFallingPowers = PowerHolderComponent.getPowers(this, ModifyFallingPower.class);
        if(modifyFallingPowers.size() > 0) {

            if(modifyFallingPowers.stream().anyMatch(p -> !p.takeFallDamage)) {
                this.fallDistance = 0;
            }
            return PowerHolderComponent.modify(this, ModifyFallingPower.class, in);
        }
        return in;
    }

    @Inject(method = "getAttributeValue", at = @At("RETURN"), cancellable = true)
    private void modifyAttributeValue(Holder<Attribute> attribute, CallbackInfoReturnable<Double> cir) {
        double originalValue = this.getAttributes().getValue(attribute);
        double modified = PowerHolderComponent.modify(this, ModifyAttributePower.class, (float)originalValue, p -> p.getAttribute() == attribute);
        if(originalValue != modified) {
            cir.setReturnValue(modified);
        }
    }

    @ModifyExpressionValue(method = "travelInAir", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getFriction()F", ordinal = 0))
    private float modifySlipperiness(float original) {
        return PowerHolderComponent.modify(this, ModifySlipperinessPower.class, original, p -> p.doesApply(level(), getBlockPosBelowThatAffectsMyMovement()));
    }

    @Inject(method = "doPush", at = @At("HEAD"), cancellable = true)
    private void preventPushing(Entity entity, CallbackInfo ci) {
        if(PowerHolderComponent.hasPower(this, PreventEntityCollisionPower.class, p -> p.doesApply(entity))
            || PowerHolderComponent.hasPower(entity, PreventEntityCollisionPower.class, p -> p.doesApply(this))) {
            ci.cancel();
        }
    }

    @Unique
    private float cachedDamageAmount;

    @Inject(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;checkTotemDeathProtection(Lnet/minecraft/world/damagesource/DamageSource;)Z"))
    private void cacheDamageAmount(ServerLevel level, DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        this.cachedDamageAmount = amount;
    }

    @Inject(method = "checkTotemDeathProtection", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/InteractionHand;values()[Lnet/minecraft/world/InteractionHand;"), cancellable = true)
    private void preventDeath(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        Optional<PreventDeathPower> preventDeathPower = PowerHolderComponent.getPowers(this, PreventDeathPower.class).stream().filter(p -> p.doesApply(source, cachedDamageAmount)).findFirst();
        if(preventDeathPower.isPresent()) {
            this.setHealth(1.0F);
            preventDeathPower.get().executeAction();
            cir.setReturnValue(true);
        }
    }

    @Shadow protected abstract void hurtArmor(DamageSource source, float amount);

    @Shadow public abstract int getArmorValue();

    @Shadow public abstract AttributeMap getAttributes();

    @Shadow public abstract double getAttributeValue(Holder<Attribute> attribute);

    @Shadow private @Nullable EntityReference<LivingEntity> lastHurtByMob;

    @Inject(method = "getFlyingSpeed", at = @At("RETURN"), cancellable = true)
    private void modifyFlySpeed(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(PowerHolderComponent.modify(this, ModifyAirSpeedPower.class, cir.getReturnValue()));
    }

    @Unique
    private List<ModifyFoodPower> apoli$currentModifyFoodPowers = new LinkedList<>();

    @Unique
    private ItemStack apoli$originalFoodStack;

    @Override
    public List<ModifyFoodPower> getCurrentModifyFoodPowers() {
        return apoli$currentModifyFoodPowers;
    }

    @Override
    public void setCurrentModifyFoodPowers(List<ModifyFoodPower> powers) {
        apoli$currentModifyFoodPowers = powers;
    }

    @Override
    public ItemStack getOriginalFoodStack() {
        return apoli$originalFoodStack;
    }

    @Override
    public void setOriginalFoodStack(ItemStack original) {
        apoli$originalFoodStack = original;
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void updateItemStackHolder(CallbackInfo ci) {
        InventoryUtil.forEachStack(this, stack -> stack.setEntityRepresentation(this));
    }

    @Inject(method = "canEquipWithDispenser", at = @At("HEAD"), cancellable = true)
    private void preventArmorDispensing(ItemStack stack, CallbackInfoReturnable<Boolean> info) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.dispensable()) {
            var slot = equippable.slot();
            PowerHolderComponent component = PowerHolderComponent.KEY.get(this);
            if(component.getPowers(RestrictArmorPower.class).stream().anyMatch(rap -> !rap.canEquip(stack, slot))) {
                info.setReturnValue(false);
            }
        }
    }

    // Restrict Armor
    @Inject(method = "isEquippableInSlot", at = @At("HEAD"), cancellable = true)
    private void apoli_legacy$checkCanEquipInSlot(ItemStack stack, EquipmentSlot slot, CallbackInfoReturnable<Boolean> cir) {
        PowerHolderComponent component = PowerHolderComponent.KEY.get(this);
        if(component.getPowers(RestrictArmorPower.class).stream().anyMatch(rap -> !rap.canEquip(stack, slot))) {
            cir.setReturnValue(false);
        }
    }
}
