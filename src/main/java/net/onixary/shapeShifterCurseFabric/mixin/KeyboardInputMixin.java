package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.onixary.shapeShifterCurseFabric.client.ClientPlayerStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin {
    @Shadow
    private Input keyPresses;

    @Inject(method = "tick", at = @At("TAIL"))
    private void forceSneakInput(boolean slowDown, float f, CallbackInfo ci) {
        if (ClientPlayerStateManager.shouldForceSneak) {
            this.keyPresses = new Input(
                this.keyPresses.forward(),
                this.keyPresses.backward(),
                this.keyPresses.left(),
                this.keyPresses.right(),
                this.keyPresses.jump(),
                true,
                this.keyPresses.sprint()
            );
        }
    }
}