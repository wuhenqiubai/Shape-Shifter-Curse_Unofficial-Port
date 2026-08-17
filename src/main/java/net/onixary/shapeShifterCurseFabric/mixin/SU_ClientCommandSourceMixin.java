package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.onixary.shapeShifterCurseFabric.util.SuperUserUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientSuggestionProvider.class)
public class SU_ClientCommandSourceMixin {
    @Inject(method = "hasPermission", at = @At("HEAD"), cancellable = true)
    private void hasPermission(int permissionLevel, CallbackInfoReturnable<Boolean> cir) {
        int superUserLevel = SuperUserUtils.getClientPermissionLevel();
        if (superUserLevel != -1) {
            cir.setReturnValue(superUserLevel >= permissionLevel);
        }
    }
}
