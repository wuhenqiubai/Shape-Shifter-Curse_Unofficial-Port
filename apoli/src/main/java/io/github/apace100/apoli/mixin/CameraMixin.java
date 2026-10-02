package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PhasingPower;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    public abstract @Nullable Entity entity();

    @Definition(id = "getCameraType", method = "Lnet/minecraft/client/Options;getCameraType()Lnet/minecraft/client/CameraType;")
    @Definition(id = "isFirstPerson", method = "Lnet/minecraft/client/CameraType;isFirstPerson()Z")
    @Definition(id = "isMirrored", method = "Lnet/minecraft/client/CameraType;isMirrored()Z")
    @Expression(value = "?.getCameraType().isFirstPerson() != 0", id = "detached")
    @Expression(value = "?.getCameraType().isMirrored()", id = "thirdPersonReverse")
    @ModifyExpressionValue(method = "alignWithEntity", at = {
        @At(value = "MIXINEXTRAS:EXPRESSION", id = "detached"),
        @At(value = "MIXINEXTRAS:EXPRESSION", id = "thirdPersonReverse")
    })
    private boolean disableDetachIfPhasing(boolean original) {
        if (PowerHolderComponent.getPowers(this.entity(), PhasingPower.class).stream().anyMatch(pp -> pp.getRenderType() == PhasingPower.RenderType.REMOVE_BLOCKS)) {
            return false;
        }

        return original;
    }

}
