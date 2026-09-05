package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.onixary.shapeShifterCurseFabric.client.ClientPlayerStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
// 1.21.11: KeyboardInput 改为继承 ClientInput，keyPresses 是父类 ClientInput 的 public 字段
// 通过继承 ClientInput 直接访问该字段，避免 @Shadow 父类字段
public abstract class KeyboardInputMixin extends ClientInput {

    @Inject(method = "tick", at = @At("TAIL"))
    private void forceSneakInput(CallbackInfo ci) {
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