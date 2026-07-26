package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.onixary.shapeShifterCurseFabric.client.ShapeShifterCurseFabricClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(Player.class)
public class PlayerClipAtLedgeMixin {
    @Inject(method = "isStayingOnGroundSurface", at = @At("HEAD"), cancellable = true)
    private void clipAtLedge(CallbackInfoReturnable<Boolean> cir) {
        if (ShapeShifterCurseFabricClient.isBlockingClipAtLedge) {
            cir.setReturnValue(false);
        }
    }

    /** In 1.21.1 adjustMovementForSneaking delegates the space check to isSpaceAroundPlayerEmpty.
     *  Wrap the World.isSpaceEmpty call inside that method to fix step-height clipping at ledges. */
    @WrapOperation(method = "canFallAtLeast", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;noCollision(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Z"))
    private boolean fixStepHeightClipAtLedge(Level world, Entity entity, AABB box, Operation<Boolean> original) {
        Player self = (Player) (Object) this;
        if (!box.contains(self.position())) {
            box = box.setMaxY(self.position().y);
        }
        return original.call(world, entity, box);
    }
}