package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.access.ModifiableFoodEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.networking.PlayerDismountPacket;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.util.ApoliSharedMixinValues;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity implements Nameable, CommandSource {

    @Shadow
    @Final
    public Inventory inventory;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Unique
    private InteractionResult apoli$CachedPriorityZeroResult;

    @Inject(method = "interactOn", at = @At("HEAD"), cancellable = true)
    private void preventEntityInteraction(Entity entity, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if(this.isSpectator()) {
            return;
        }
        ItemStack stack = this.getItemInHand(hand);
        for(PreventEntityUsePower peup : PowerHolderComponent.getPowers(this, PreventEntityUsePower.class)) {
            if(peup.doesApply(entity, hand, stack)) {
                cir.setReturnValue(peup.executeAction(entity, hand));
                cir.cancel();
                return;
            }
        }
        for(PreventBeingUsedPower pbup : PowerHolderComponent.getPowers(entity, PreventBeingUsedPower.class)) {
            if(pbup.doesApply((Player) (Object) this, hand, stack)) {
                cir.setReturnValue(pbup.executeAction((Player) (Object) this, hand));
                cir.cancel();
                return;
            }
        }
        apoli$CachedPriorityZeroResult = InteractionResult.PASS;
        ActiveInteractionPower.CallInstance<ActiveInteractionPower> callInstance = new ActiveInteractionPower.CallInstance<>();
        callInstance.add(this, ActionOnEntityUsePower.class, p -> p.shouldExecute(entity, hand, stack) && p.getPriority() >= 0);
        callInstance.add(entity, ActionOnBeingUsedPower.class, p -> p.shouldExecute((Player) (Object) this, hand, stack) && p.getPriority() >= 0);
        for(int i = callInstance.getMaxPriority(); i >= 0; i--) {
            if(!callInstance.hasPowers(i)) {
                continue;
            }
            List<ActiveInteractionPower> powers = callInstance.getPowers(i);
            InteractionResult result = InteractionResult.PASS;
            for(ActiveInteractionPower ip : powers) {
                InteractionResult ar = InteractionResult.PASS;
                if(ip instanceof ActionOnEntityUsePower aoeup) {
                    ar = aoeup.executeAction(entity, hand);
                } else if(ip instanceof ActionOnBeingUsedPower aobup) {
                    ar = aobup.executeAction((Player) (Object) this, hand);
                }
                if(ar.consumesAction() && !result.consumesAction()) {
                    result = ar;
                } else if(ar.shouldSwing() && !result.shouldSwing()) {
                    result = ar;
                }
            }
            if(i == 0) {
                apoli$CachedPriorityZeroResult = result;
            } else {
                apoli$CachedPriorityZeroResult = InteractionResult.PASS;
                if(result != InteractionResult.PASS) {
                    if(result.shouldSwing()) {
                        this.swing(hand);
                    }
                    cir.setReturnValue(result);
                    break;
                }
            }
        }
    }

    @Inject(method = "hurt", at = @At(value = "RETURN", ordinal = 3), cancellable = true)
    private void allowDamageIfModifyingPowersExist(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {

        boolean hasModifyingPower = false;

        if (source.getEntity() != null) {
            if (source.is(DamageTypeTags.IS_PROJECTILE)) hasModifyingPower = PowerHolderComponent.hasPower(source.getEntity(), ModifyProjectileDamagePower.class, mpdp -> mpdp.doesApply(source, amount, this));
            else hasModifyingPower = PowerHolderComponent.hasPower(source.getEntity(), ModifyDamageDealtPower.class, mddp -> mddp.doesApply(source, amount, this));
        }

        hasModifyingPower |= PowerHolderComponent.hasPower(this, ModifyDamageTakenPower.class, mdtp -> mdtp.doesApply(source, amount));
        if (hasModifyingPower) cir.setReturnValue(super.hurt(source, amount));

    }

    @Inject(method = "interactOn", at = @At("RETURN"), cancellable = true)
    private void entityInteractionAfter(Entity entity, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult original = cir.getReturnValue();
        InteractionResult custom = InteractionResult.PASS;
        if(apoli$CachedPriorityZeroResult != InteractionResult.PASS) {
            custom = apoli$CachedPriorityZeroResult;
        } else if(cir.getReturnValue() == InteractionResult.PASS) {
            ItemStack stack = this.getItemInHand(hand);
            ActiveInteractionPower.CallInstance<ActiveInteractionPower> callInstance = new ActiveInteractionPower.CallInstance<>();
            callInstance.add(this, ActionOnEntityUsePower.class, p -> p.shouldExecute(entity, hand, stack) && p.getPriority() < 0);
            callInstance.add(entity, ActionOnBeingUsedPower.class, p -> p.shouldExecute((Player) (Object) this, hand, stack) && p.getPriority() < 0);
            for(int i = -1; i >= callInstance.getMinPriority(); i--) {
                if(!callInstance.hasPowers(i)) {
                    continue;
                }
                List<ActiveInteractionPower> powers = callInstance.getPowers(i);
                InteractionResult result = InteractionResult.PASS;
                for(ActiveInteractionPower ip : powers) {
                    InteractionResult ar = InteractionResult.PASS;
                    if(ip instanceof ActionOnEntityUsePower aoeup) {
                        ar = aoeup.executeAction(entity, hand);
                    } else if(ip instanceof ActionOnBeingUsedPower aobup) {
                        ar = aobup.executeAction((Player) (Object) this, hand);
                    }
                    if(ar.consumesAction() && !result.consumesAction()) {
                        result = ar;
                    } else if(ar.shouldSwing() && !result.shouldSwing()) {
                        result = ar;
                    }
                }
                if(result != InteractionResult.PASS) {
                    custom = result;
                    break;
                }
            }
        }
        if(custom.shouldSwing()) {
            this.swing(hand);
        }
        if(original.consumesAction() && !custom.consumesAction()) {
        } else if(original.shouldSwing() && !custom.shouldSwing()) {
        } else {
            cir.setReturnValue(custom);
        }
    }

    @Inject(method = "removeVehicle", at = @At("HEAD"))
    private void sendPlayerDismountPacket(CallbackInfo ci) {
        if(!level().isClientSide && getVehicle() instanceof Player) {
            ServerPlayNetworking.send((ServerPlayer) getVehicle(), new PlayerDismountPacket(getId()));
        }
    }

    @Inject(method = "updateSwimming", at = @At("TAIL"))
    private void updateSwimmingPower(CallbackInfo ci) {
        if(PowerHolderComponent.hasPower(this, SwimmingPower.class)) {
            this.setSwimming(this.isSprinting() && !this.isPassenger());
            this.wasTouchingWater = this.isSwimming();
            if (this.isSwimming()) {
                this.fallDistance = 0.0F;
                Vec3 look = this.getLookAngle();
                move(MoverType.SELF, new Vec3(look.x/4, look.y/4, look.z/4));
            }
        } else if(PowerHolderComponent.hasPower(this, IgnoreWaterPower.class)) {
            this.setSwimming(false);
        }
    }

    @Inject(method = "stopSleepInBed", at = @At("HEAD"))
    private void invokeWakeUpAction(boolean bl, boolean updateSleepingPlayers, CallbackInfo ci) {
        if(!bl && !updateSleepingPlayers && getSleepingPos().isPresent()) {
            BlockPos sleepingPos = getSleepingPos().get();
            PowerHolderComponent.getPowers(this, ActionOnWakeUp.class).stream().filter(p -> p.doesApply(sleepingPos)).forEach(p -> p.executeActions(sleepingPos, Direction.DOWN));
        }
    }

    // Prevent healing if DisableRegenPower
    // Note that this function was called "shouldHeal" instead of "canFoodHeal" at some point in time.
    @Inject(method = "isHurt", at = @At("HEAD"), cancellable = true)
    private void disableHeal(CallbackInfoReturnable<Boolean> info) {
        if(PowerHolderComponent.hasPower(this, DisableRegenPower.class)) {
            info.setReturnValue(false);
        }
    }

    // ModifyExhaustion
    @ModifyVariable(at = @At("HEAD"), method = "causeFoodExhaustion", ordinal = 0, argsOnly = true)
    private float modifyExhaustion(float exhaustionIn) {
        return PowerHolderComponent.modify(this, ModifyExhaustionPower.class, exhaustionIn);
    }

    @Inject(method = "dropEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;dropAll()V"))
    private void dropAdditionalInventory(CallbackInfo ci) {
        PowerHolderComponent.getPowers(this, InventoryPower.class).forEach(inventoryPower -> {
            if(inventoryPower.shouldDropOnDeath()) {
                inventoryPower.dropItemsOnDeath();
            }
        });
        PowerHolderComponent.getPowers(this, KeepInventoryPower.class).forEach(keepInventoryPower -> {
            keepInventoryPower.preventItemsFromDropping(inventory);
        });
    }

    @Inject(method = "dropEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;dropAll()V", shift = At.Shift.AFTER))
    private void restoreKeptInventory(CallbackInfo ci) {
        PowerHolderComponent.getPowers(this, KeepInventoryPower.class).forEach(keepInventoryPower -> {
            keepInventoryPower.restoreSavedItems(inventory);
        });
    }

    @WrapOperation(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/food/FoodProperties;)V"))
    private void apoli$storeSharedStack(FoodData instance, FoodProperties foodProperties, Operation<Void> original, @Local(argsOnly = true) ItemStack stack) {
        List<ModifyFoodPower> mfps = PowerHolderComponent.getPowers(this, ModifyFoodPower.class);
        mfps = mfps.stream().filter(mfp -> mfp.doesApply(stack)).toList();

        ApoliSharedMixinValues.CURRENT_STACK.set(stack);
        ((ModifiableFoodEntity) this).setOriginalFoodStack(stack);
        ((ModifiableFoodEntity) this).setCurrentModifyFoodPowers(mfps);
        original.call(instance, foodProperties);
        ApoliSharedMixinValues.CURRENT_STACK.remove();
        ((ModifiableFoodEntity) this).setOriginalFoodStack(null);
        ((ModifiableFoodEntity) this).setCurrentModifyFoodPowers(new ArrayList<>());
    }
}
