package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.onixary.shapeShifterCurseFabric.util.SuperUserUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class SU_ServerPlayerEntityMixin {
    @Inject(method = "permissions", at = @At("HEAD"), cancellable = true)
    private void injectPermissions(CallbackInfoReturnable<PermissionSet> cir) {
        int superUserLevel = SuperUserUtils.getCurrentPermissionLevel((ServerPlayer) (Object) this);
        if (superUserLevel != -1) {
            cir.setReturnValue(LevelBasedPermissionSet.forLevel(PermissionLevel.byId(superUserLevel)));
        }
    }
}
