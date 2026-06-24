package net.onixary.shapeShifterCurseFabric.mixin;

import com.mojang.authlib.GameProfile;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractFadeModifier;
import com.zigythebird.playeranimcore.animation.layered.modifier.SpeedModifier;
import com.zigythebird.playeranimcore.easing.EasingType;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.onixary.shapeShifterCurseFabric.player_animation.AnimationHolder;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class PlayerEntityAnimOverrideMixin extends PlayerEntity {
    @Unique
    PlayerAnimationController controller;
    @Unique
    PlayerAnimationController upperController;

    public PlayerEntityAnimOverrideMixin(ClientWorld world, GameProfile gameProfile) {
        super(world, world.getSpawnPos(), world.getSpawnAngle(), gameProfile);
    }

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void shape_shifter_curse$init(ClientWorld level, GameProfile profile, CallbackInfo info) {
        var player = (AbstractClientPlayerEntity) (Object) this;
        var animManager = PlayerAnimationAccess.getPlayerAnimManager(player);
        controller = new PlayerAnimationController(player, (c, state, setter) -> null);
        animManager.addAnimLayer(1, controller);
        upperController = new PlayerAnimationController(player, (c, state, setter) -> null);
        animManager.addAnimLayer(2, upperController);
        currentAnimation = null;
    }

    @Unique
    Animation currentAnimation = null;
    @Unique
    Animation currentUpperAnimation = null;

    @Unique
    AnimationHolder animToPlay = null;

    @Unique
    AnimSystem animSystem = new AnimSystem(this);

    @Inject(method = "tick", at = @At("TAIL"))
    void tick(CallbackInfo ci) {
        animToPlay = this.animSystem.getAnimation();
        if (animToPlay != null) {
            if (animToPlay.isSkipFade()) {
                if (currentAnimation != animToPlay.getAnimation()) {
                    controller.triggerAnimation(animToPlay.getAnimation(), 0);
                    currentAnimation = animToPlay.getAnimation();
                }
            } else {
                var easing = animToPlay.getEasingType();
                playAnimation(animToPlay.getAnimation(), animToPlay.getSpeed(), animToPlay.getFade(),
                        easing != null ? easing : EasingType.LINEAR);
            }
        } else {
            currentAnimation = null;
            controller.stop();
        }
        // 上半身覆盖层
        AnimationHolder upperAnim = this.animSystem.getUpperBodyOverride();
        if (upperAnim != null) {
            Animation palAnim = upperAnim.getAnimation();
            if (currentUpperAnimation != palAnim) {
                currentUpperAnimation = palAnim;
                if (palAnim != null) {
                    upperController.triggerAnimation(palAnim, 0);
                } else {
                    upperController.stop();
                }
            }
        } else {
            currentUpperAnimation = null;
            upperController.stop();
        }
    }

    @Unique
    public void playAnimation(Animation anim) {
        playAnimation(anim, 1.0f, 10, EasingType.LINEAR);
    }

    @Unique
    private boolean modified = false;

    @Unique
    public void playAnimation(Animation anim, float speed, int fade, EasingType easing) {
        if (currentAnimation == anim || anim == null) return;
        currentAnimation = anim;
        if (modified) controller.removeModifier(0);
        modified = true;
        controller.addModifierBefore(new SpeedModifier(speed));
        controller.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(fade, easing), anim, true);
    }
}
