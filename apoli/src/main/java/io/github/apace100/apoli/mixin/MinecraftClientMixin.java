package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.EntityGlowPower;
import io.github.apace100.apoli.power.SelfGlowPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @Shadow public LocalPlayer player;

    @ModifyReturnValue(method = "shouldEntityAppearGlowing", at = @At("RETURN"))
    private boolean makeEntitiesGlow(boolean original, @Local(argsOnly = true) Entity entity) {
        if(!original) {
            if(this.player != null) {
                if(player != entity) {
                    if(PowerHolderComponent.getPowers(player, EntityGlowPower.class).stream().anyMatch(p -> p.doesApply(entity))) {
                        return true;
                    }
                }
                if (entity instanceof LivingEntity) {
                    if (PowerHolderComponent.getPowers(entity, SelfGlowPower.class).stream().anyMatch(p -> p.doesApply(this.player))) {
                        return true;
                    }
                }
            }
        }
        return original;
    }
}
