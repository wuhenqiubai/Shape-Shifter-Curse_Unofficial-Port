package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.access.MutableItemStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ActionOnItemUsePower;
import io.github.apace100.apoli.power.PreventItemUsePower;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements MutableItemStack, EntityLinkedItemStack {

    @Shadow @Deprecated private Item item;

    @Shadow private int count;

    @Shadow public abstract @Nullable Entity getEntityRepresentation();

    @Shadow public abstract int getUseDuration(LivingEntity entity);

    @Shadow @Final @Mutable
    private PatchedDataComponentMap components;

    @Unique
    private ItemStack apoli$usedItemStack;

    @Unique
    private Entity apoli$holdingEntity;

    @Override
    public Entity getEntity() {
        Entity vanillaHolder = getEntityRepresentation();
        if(vanillaHolder == null) {
            return apoli$holdingEntity;
        }
        return vanillaHolder;
    }

    @Override
    public void setEntity(Entity entity) {
        this.apoli$holdingEntity = entity;
    }

    @Inject(method = "copy", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;setPopTime(I)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void copyNewParams(CallbackInfoReturnable<ItemStack> cir, ItemStack itemStack) {
        if (this.apoli$holdingEntity != null) {
            ((EntityLinkedItemStack) (Object) itemStack).setEntity(apoli$holdingEntity);
        }
    }

    @Inject(method = "finishUsingItem", at = @At("HEAD"))
    public void callActionOnUseFinishBefore(Level world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        apoli$usedItemStack = ((ItemStack)(Object)this).copy();
        if(user != null) {
            ActionOnItemUsePower.executeActions(user, (ItemStack)(Object)this, apoli$usedItemStack,
                    ActionOnItemUsePower.TriggerType.FINISH, ActionOnItemUsePower.PriorityPhase.BEFORE);
        }
    }

    @Inject(method = "finishUsingItem", at = @At("RETURN"))
    public void callActionOnUseFinishAfter(Level world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if(user != null) {
            ActionOnItemUsePower.executeActions(user, cir.getReturnValue(), apoli$usedItemStack,
                    ActionOnItemUsePower.TriggerType.FINISH, ActionOnItemUsePower.PriorityPhase.AFTER);
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void callActionOnUseInstantBefore(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if(user != null) {
            PowerHolderComponent component = PowerHolderComponent.KEY.get(user);
            ItemStack stackInHand = user.getItemInHand(hand);
            for(PreventItemUsePower piup : component.getPowers(PreventItemUsePower.class)) {
                if(piup.doesPrevent(stackInHand)) {
                    cir.setReturnValue(InteractionResultHolder.fail(stackInHand));
                    return;
                }
            }

            if(getUseDuration(user) == 0) {
                ActionOnItemUsePower.executeActions(user, (ItemStack)(Object)this, (ItemStack)(Object)this,
                        ActionOnItemUsePower.TriggerType.INSTANT, ActionOnItemUsePower.PriorityPhase.BEFORE);
            } else {
                ActionOnItemUsePower.executeActions(user, (ItemStack)(Object)this, (ItemStack)(Object)this,
                        ActionOnItemUsePower.TriggerType.START, ActionOnItemUsePower.PriorityPhase.BEFORE);
            }
        }
    }

    @Inject(method = "use", at = @At("RETURN"))
    private void callActionOnUseInstantAfter(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        var stack = (ItemStack) (Object) this;
        if(user != null) {
            InteractionResultHolder<ItemStack> ar = cir.getReturnValue();
            if(!ar.getResult().consumesAction()) {
                return;
            }
            if(getUseDuration(user) == 0) {
                ActionOnItemUsePower.executeActions(user, stack, stack,
                        ActionOnItemUsePower.TriggerType.INSTANT, ActionOnItemUsePower.PriorityPhase.AFTER);
            } else {
                ActionOnItemUsePower.executeActions(user, stack, stack,
                        ActionOnItemUsePower.TriggerType.START, ActionOnItemUsePower.PriorityPhase.AFTER);
            }
        }
    }

    @Inject(method = "releaseUsing", at = @At("HEAD"))
    private void callActionOnUseStopBefore(Level world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if(user != null) {
            ActionOnItemUsePower.executeActions(user, (ItemStack)(Object)this, (ItemStack)(Object)this,
                    ActionOnItemUsePower.TriggerType.STOP, ActionOnItemUsePower.PriorityPhase.BEFORE);
        }
    }

    @Inject(method = "releaseUsing", at = @At("RETURN"))
    private void callActionOnUseStopAfter(Level world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if(user != null) {
            ActionOnItemUsePower.executeActions(user, (ItemStack)(Object)this, (ItemStack)(Object)this,
                    ActionOnItemUsePower.TriggerType.STOP, ActionOnItemUsePower.PriorityPhase.AFTER);
        }
    }

    @Inject(method = "onUseTick", at = @At("HEAD"))
    private void callActionOnUseDuringBefore(Level world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if(user != null) {
            ActionOnItemUsePower.executeActions(user, (ItemStack)(Object)this, (ItemStack)(Object)this,
                    ActionOnItemUsePower.TriggerType.DURING, ActionOnItemUsePower.PriorityPhase.BEFORE);
        }
    }

    @Inject(method = "onUseTick", at = @At("RETURN"))
    private void callActionOnUseDuringAfter(Level world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if(user != null) {
            ActionOnItemUsePower.executeActions(user, (ItemStack)(Object)this, (ItemStack)(Object)this,
                    ActionOnItemUsePower.TriggerType.DURING, ActionOnItemUsePower.PriorityPhase.AFTER);
        }
    }

    @Override
    public void setItem(Item item) {
        this.item = item;
    }

    @Override
    public void setFrom(ItemStack stack) {
        setItem(stack.getItem());
        if (stack.getComponents() instanceof PatchedDataComponentMap map)
            components = map;
        count = stack.getCount();
    }
}
