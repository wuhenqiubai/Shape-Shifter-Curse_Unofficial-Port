package net.onixary.shapeShifterCurseFabric.mixin;

import com.mojang.authlib.GameProfile;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.layered.modifier.SpeedModifier;
import com.zigythebird.playeranimcore.easing.EasingType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_animation.AnimationHolder;
import net.onixary.shapeShifterCurseFabric.player_animation.ShortestArcFadeModifier;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(AbstractClientPlayer.class)
public abstract class PlayerEntityAnimOverrideMixin extends Player {
    @Unique
    PlayerAnimationController controller;

    public PlayerEntityAnimOverrideMixin(ClientLevel world, GameProfile gameProfile) {
        super(world, gameProfile);
    }

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void shape_shifter_curse$init(ClientLevel level, GameProfile profile, CallbackInfo info) {
        controller = new PlayerAnimationController((AbstractClientPlayer) (Object) this,
                (c, state, setter) -> null);
        PlayerAnimationAccess.getPlayerAnimManager(this).addAnimLayer(1, controller);
        currentAnimation = null;
    }

    @Unique
    Animation currentAnimation = null;

    @Unique
    AnimationHolder animToPlay = null;

    @Unique
    AnimSystem animSystem = new AnimSystem(this);

    @Unique
    Set<String> ssc$registeredExtraBones = new HashSet<>();

    @Unique
    private void ssc$ensureExtraBonesRegistered() {
        if (controller == null || AnimSystem.EXTRA_ANIM_BONES.isEmpty()) {
            return;
        }
        for (String name : AnimSystem.EXTRA_ANIM_BONES) {
            if (ssc$registeredExtraBones.add(name)) {
                controller.registerPlayerAnimBone(name);
                ShapeShifterCurseFabric.LOGGER.debug("[SSC] Registered extra anim bone to PAL controller: {}", name);
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    void tick(CallbackInfo ci) {
        ssc$ensureExtraBonesRegistered();
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
        controller.replaceAnimationWithFade(new ShortestArcFadeModifier(fade, easing), anim, true);
    }
}
