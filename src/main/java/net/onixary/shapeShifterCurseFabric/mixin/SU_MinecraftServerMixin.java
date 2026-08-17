package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.players.NameAndId;
import net.onixary.shapeShifterCurseFabric.util.SuperUserUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public class SU_MinecraftServerMixin {
    @Inject(method = "getProfilePermissions", at = @At("HEAD"), cancellable = true)
    private void getProfilePermissions(NameAndId nameAndId, CallbackInfoReturnable<LevelBasedPermissionSet> cir) {
        int superUserLevel = SuperUserUtils.getCurrentPermissionLevel(nameAndId.id());
        if (superUserLevel != -1) {
            cir.setReturnValue(LevelBasedPermissionSet.forLevel(PermissionLevel.byId(superUserLevel)));
        }
    }
}
