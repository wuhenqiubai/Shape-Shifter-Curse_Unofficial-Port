package net.onixary.shapeShifterCurseFabric.mixin;

import com.mojang.authlib.GameProfile;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.layered.modifier.SpeedModifier;
import com.zigythebird.playeranimcore.easing.EasingType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_animation.AnimationHolder;
import net.onixary.shapeShifterCurseFabric.player_animation.ShortestArcFadeModifier;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimSystem;
import net.onixary.shapeShifterCurseFabric.player_animator.PlayerAnimatorCompat;
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
        // PlayerAnimator 路径：把 SSCU 的层挂到 PA 的 stack 上（优先级 1），
        // 这样第三方动作 mod 用更高优先级就能与形态动画按层混合，而不是互相覆盖。
        if (PlayerAnimatorCompat.available()) {
            ssc$paContainer = PlayerAnimatorCompat.createLayer((AbstractClientPlayer) (Object) this);
            return;
        }
        // ↓↓↓ 以下为 PAL 路径 ↓↓↓
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
        // PlayerAnimator 路径：形态动画跑在 PA 的 stack 上，逻辑见 ssc$paTick
        if (PlayerAnimatorCompat.available()) {
            ssc$paTick();
            return;
        }
        // ↓↓↓ 以下为 PAL 路径 ↓↓↓
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

    // ==================================================================
    // PlayerAnimator 路径（仅当 PlayerAnimatorCompat.available() 时启用）
    // 播放逻辑照搬 SSCU 迁移到 PAL 之前的版本（commit 24786162^，当时是 PA 2.0.4 + 1.21.1）。
    // ==================================================================

    /**
     * PA 侧的动画层容器（实际类型 {@code ModifierLayer<IAnimation>}）。
     * <p>
     * 必须声明成 {@link Object} 而不是 PA 的 {@code ModifierLayer}：字段类型会写进类文件，
     * 有可能在类加载/验证阶段就被解析，而 PlayerAnimator 是可选依赖。
     * 完整说明见 {@link PlayerAnimatorCompat} 的类注释「类加载安全」。
     */
    @Unique
    Object ssc$paContainer = null;

    /**
     * PA 容器里当前是否已经存在 modifier。
     * <p>
     * 必须跨 tick 记住：PA 的 {@code ModifierLayer.removeModifier(int)} 是裸的 {@code List.remove}，
     * <b>容器为空时会越界抛异常</b>。而首播时容器本来就是空的。
     */
    @Unique
    boolean ssc$paModified = false;

    /** 当前已下发给 PA 的动画 ID，作用等同于 PAL 路径的 {@code currentAnimation} 去重。 */
    @Unique
    ResourceLocation ssc$paCurrentAnimationID = null;

    @Unique
    private void ssc$paTick() {
        // 与 PAL 路径一致：getAnimation() 必须每 tick 调一次，否则 NPPA（power animation）计时会出错。
        // PA 路径不需要 ssc$ensureExtraBonesRegistered —— PA 按动画里的原始骨骼名直查，无需注册。
        animToPlay = this.animSystem.getAnimation();
        if (ssc$paContainer == null) {
            return;
        }
        // 取 ResourceLocation 而不是 AnimationHolder#getAnimation()：后者返回的是 PAL 的 Animation 对象，
        // PA 路径下不该碰它。animationID 由 AnimationHolder(ResourceLocation, ...) 构造时写入，
        // 而实际参与播放的 holder 全都是走那个构造的（只有 EMPTY 哨兵不是，它本来就不播）。
        ResourceLocation animID = animToPlay == null ? null : animToPlay.animationID;
        if (animID == null) {
            // 只在「从有到无」时下发一次 stop，避免每 tick 重复 setAnimation(null)
            if (ssc$paCurrentAnimationID != null) {
                ssc$paCurrentAnimationID = null;
                PlayerAnimatorCompat.stop(ssc$paContainer);
            }
            return;
        }
        if (animID.equals(ssc$paCurrentAnimationID)) {
            return;
        }
        ssc$paCurrentAnimationID = animID;
        if (animToPlay.isSkipFade()) {
            // 硬切。PAL 路径的 skipFade 分支同样不处理速度（走 triggerAnimation），此处保持一致。
            ssc$paModified = PlayerAnimatorCompat.playImmediate(ssc$paContainer, animID, ssc$paModified);
        } else {
            ssc$paModified = PlayerAnimatorCompat.play(ssc$paContainer, animID,
                    animToPlay.getSpeed(), animToPlay.getFade(), ssc$paModified);
        }
    }
}
