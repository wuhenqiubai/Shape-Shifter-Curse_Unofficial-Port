package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ThrowablePotionItem.class)
public abstract  class ThrowablePotionStackMixin {
    @Inject(method = "use", at = @At("RETURN"))
    private void addCooldown(Level world, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!world.isClientSide()) {
            player.getCooldowns().addCooldown((ThrowablePotionItem) (Object) this, 20); // 20 ticks = 1 second
        }
    }
}