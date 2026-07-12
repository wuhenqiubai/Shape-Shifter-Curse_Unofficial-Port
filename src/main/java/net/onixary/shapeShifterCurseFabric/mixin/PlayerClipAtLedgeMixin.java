package net.onixary.shapeShifterCurseFabric.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.onixary.shapeShifterCurseFabric.client.ShapeShifterCurseFabricClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Environment(EnvType.CLIENT)
@Mixin(PlayerEntity.class)
public class PlayerClipAtLedgeMixin {
    @Inject(method = "clipAtLedge", at = @At("HEAD"), cancellable = true)
    private void clipAtLedge(CallbackInfoReturnable<Boolean> cir) {
        if (!ShapeShifterCurseFabricClient.isClipAtLedge) {
            cir.setReturnValue(false);
        }
    }

    /** In 1.21.1 adjustMovementForSneaking delegates the space check to isSpaceAroundPlayerEmpty.
     *  Wrap the World.isSpaceEmpty call inside that method to fix step-height clipping at ledges. */
    @WrapOperation(method = "isSpaceAroundPlayerEmpty", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isSpaceEmpty(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Z"))
    private boolean fixStepHeightClipAtLedge(World world, Entity entity, Box box, Operation<Boolean> original) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (!box.contains(self.getPos())) {
            box = box.withMaxY(self.getPos().y);
        }
        return original.call(world, entity, box);
    }
}
