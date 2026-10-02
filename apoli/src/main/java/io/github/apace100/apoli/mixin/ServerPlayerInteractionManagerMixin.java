package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ActionOnBlockBreakPower;
import io.github.apace100.apoli.power.ActionOnBlockUsePower;
import io.github.apace100.apoli.power.ModifyHarvestPower;
import io.github.apace100.apoli.power.PreventBlockUsePower;
import io.github.apace100.apoli.util.SavedBlockPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerInteractionManagerMixin {

    @Shadow
    public ServerLevel level;
    @Shadow
    public ServerPlayer player;
    private SavedBlockPosition savedBlockPosition;

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void cacheBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        this.savedBlockPosition = new SavedBlockPosition(level, pos);
    }

    @ModifyVariable(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;mineBlock(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)V"), ordinal = 1)
    private boolean modifyEffectiveTool(boolean original) {
        for (ModifyHarvestPower mhp : PowerHolderComponent.getPowers(player, ModifyHarvestPower.class)) {
            if (mhp.doesApply(savedBlockPosition)) {
                return mhp.isHarvestAllowed();
            }
        }
        return original;
    }

    @Inject(method = "destroyBlock", at = @At(value = "RETURN", ordinal = 4, shift = At.Shift.BEFORE))
    private void actionOnBlockBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0) boolean removingBlock, @Local(ordinal = 1) boolean hasCorrectTool) {
        PowerHolderComponent.getPowers(player, ActionOnBlockBreakPower.class).stream().filter(p -> p.doesApply(savedBlockPosition))
            .forEach(aobbp -> aobbp.executeActions(removingBlock && hasCorrectTool, pos, null));
    }


    @Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isSecondaryUseActive()Z"), cancellable = true)
    private void preventBlockInteraction(ServerPlayer player, Level world, ItemStack stack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if(PowerHolderComponent.getPowers(player, PreventBlockUsePower.class).stream().anyMatch(p -> p.doesPrevent(world, hitResult.getBlockPos()))) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
    private void executeBlockUseActions(ServerPlayer player, Level world, ItemStack stack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        PowerHolderComponent.getPowers(player, ActionOnBlockUsePower.class).stream()
            .filter(p -> p.shouldExecute(hitResult.getBlockPos(), hitResult.getDirection(), hand, stack))
            .forEach(p -> p.executeAction(hitResult.getBlockPos(), hitResult.getDirection(), hand));
    }
}
