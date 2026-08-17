package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.util.SuperUserUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommandSourceStack.class)
public class SU_ServerCommandSourceMixin {
    @Shadow
    @Final
    @Nullable
    private Entity entity;

    @Inject(method = "permissions", at = @At("HEAD"), cancellable = true)
    private void injectPermissions(CallbackInfoReturnable<PermissionSet> cir) {
        Entity nowEntity = this.entity;
        if (nowEntity instanceof Player playerEntity) {
            int superUserLevel = SuperUserUtils.getCurrentPermissionLevel(playerEntity);
            if (superUserLevel != -1) {
                cir.setReturnValue(LevelBasedPermissionSet.forLevel(PermissionLevel.byId(superUserLevel)));
            }
        }
    }
}
