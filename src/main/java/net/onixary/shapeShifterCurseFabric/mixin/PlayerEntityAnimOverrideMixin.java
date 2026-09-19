package net.onixary.shapeShifterCurseFabric.mixin;

import com.mojang.authlib.GameProfile;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.layered.modifier.SpeedModifier;
import com.zigythebird.playeranimcore.easing.EasingType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
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
        PlayerAnimationAccess.getPlayerAnimManager((AbstractClientPlayer) (Object) this).addAnimLayer(1, controller);
        currentAnimation = null;
    }

    @Unique
    Animation currentAnimation = null;

    @Unique
    AnimationHolder animToPlay = null;

    @Unique
    AnimSystem animSystem = new AnimSystem(this);

    /**
     * 本 controller 上已经注册过的额外骨骼名。
     * 用它去重是必须的：{@code registerPlayerAnimBone(String)} 每次都会 new 一个
     * {@code AdvancedPlayerAnimBone} 覆盖旧对象，重复调用会把 per-bone 的 enable / 各轴开关状态重置。
     */
    @Unique
    Set<String> ssc$registeredExtraBones = new HashSet<>();

    /**
     * 把形态 {@code extra_parts_map} 里的额外骨骼注册进 PAL 控制器。
     * <p>
     * PAL 的 {@code setupNewAnimation} 只把「控制器注册表里有的名字」放进 {@code activeBones}；
     * 而 {@code PlayerAnimationController.registerBones()} 硬编码只注册 11 根 vanilla 骨，
     * 未注册的骨骼在 {@code get3DTransformRaw} 里被**静默返回零值**（不报错不打日志）——
     * 这正是 {@code bipedRightHindLeg} / {@code bipedLeftHindLeg} 这类额外骨骼完全没有动画的原因。
     * <p>
     * 上游用 PlayerAnimator 时按动画原始骨骼名直查、不需要注册，所以这是换用 PAL 之后引入的。
     * <p>
     * 必须在本方法内**先于** {@code triggerAnimation} / {@code playAnimation} 执行：
     * {@code activeBones} 是在 {@code setupNewAnimation} 里算出来的，注册晚了要等下一次换动画才生效。
     */
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
        // 必须放在触发动画之前，见 ssc$ensureExtraBonesRegistered 的说明
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
        // 上半身覆盖层
//        AnimationHolder upperAnim = this.animSystem.getUpperBodyOverride();
//        if (upperAnim != null) {
//            Animation palAnim = upperAnim.getAnimation();
//            if (currentUpperAnimation != palAnim) {
//                currentUpperAnimation = palAnim;
//                if (palAnim != null) {
//                    upperController.triggerAnimation(palAnim, 0);
//                } else {
//                    upperController.stop();
//                }
//            }
//        } else {
//            currentUpperAnimation = null;
//            upperController.stop();
//        }
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
