package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyExperiencePower;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrb.class)
public class ExperienceOrbEntityMixin {

    @Shadow private int count;

    @Inject(method = "playerTouch", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;takeXpDelay:I", ordinal = 1))
    private void modifyXpAmount(Player player, CallbackInfo ci) {
        this.count = (int) PowerHolderComponent.modify(player, ModifyExperiencePower.class, this.count);
    }
}
