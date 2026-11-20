package io.github.apace100.apoli.mixin.integration.connector;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ActionOnBlockBreakPower;
import io.github.apace100.apoli.power.ActionOnBlockUsePower;
import io.github.apace100.apoli.power.PreventBlockUsePower;
import io.github.apace100.apoli.util.HarvestContext;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerInteractionManagerMixin {

    @Shadow
    public ServerLevel level;

    @Shadow
    public ServerPlayer player;

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void cacheBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        HarvestContext.setBlockPosition(level, pos);
        HarvestContext.setCanHarvest(player.hasCorrectToolForDrops(level.getBlockState(pos)));
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void actionOnBlockBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            boolean harvested = HarvestContext.getCanHarvest();
            PowerHolderComponent.getPowers(player, ActionOnBlockBreakPower.class)
                .stream()
                .filter(p -> p.doesApply(HarvestContext.getBlockPosition()))
                .forEach(p -> p.executeActions(harvested, pos, null));
        }
        HarvestContext.clearCanHarvest();
        HarvestContext.clearBlockPosition();
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
