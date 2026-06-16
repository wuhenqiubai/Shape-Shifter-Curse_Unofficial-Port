package net.onixary.shapeShifterCurseFabric.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimSystem;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.IAnimSystemAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class PlayerEntityAnimOverrideMixin extends PlayerEntity implements IAnimSystemAccessor {

    public PlayerEntityAnimOverrideMixin(ClientWorld world, GameProfile gameProfile) {
        super(world, world.getSpawnPos(), world.getSpawnAngle(), gameProfile);
    }

    @Unique
    AnimSystem animSystem = new AnimSystem(this);

    @Override
    public AnimSystem shape_shifter_curse$getAnimSystem() {
        return animSystem;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    void tick(CallbackInfo ci) {
        // Resolve animation for current frame — writes to AnimationState for GeckoLib bridge
        this.animSystem.getAnimation();
        this.animSystem.getUpperBodyOverride();
    }
}
