package net.onixary.shapeShifterCurseFabric.player_animator;

import com.zigythebird.playeranimcore.enums.TransformType;
import com.zigythebird.playeranimcore.math.Vec3f;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link PlayerAnimatorBridge} 的唯一实现，直接使用 PlayerAnimator 的 API。
 *
 * <h2>这个类为什么必须独立存在</h2>
 * 本类的方法体里全是 {@code dev.kosmx.playerAnim.*} 引用。它<b>绝不能被静态引用</b>
 * （那会让引用者的字节码也带上 PA 类型），只能由 {@link PlayerAnimatorCompat}
 * 在确认 PA 的类真的可加载之后用 {@code Class.forName} 动态加载。
 * 理由见 {@link PlayerAnimatorBridge} 的类注释。
 *
 * <h2>实现来源</h2>
 * 播放逻辑照搬 SSCU 迁移到 PAL 之前的版本（commit {@code 24786162^}：当时是
 * PA {@code 2.0.4+1.21.1}，Yarn 映射），以保证行为与上游一致；名称已转成 Mojmap。
 * 其中 {@code disableArmAnimations()} / {@code armAnimationsEnabled} 是死代码
 * （无任何外部调用者），未予恢复。
 */
public final class PlayerAnimatorImpl implements PlayerAnimatorBridge {

    @Override
    public @Nullable Object createLayer(@NotNull AbstractClientPlayer player) {
        dev.kosmx.playerAnim.api.layered.ModifierLayer<dev.kosmx.playerAnim.api.layered.IAnimation> container =
                new dev.kosmx.playerAnim.api.layered.ModifierLayer<>();
        // 优先级 1：低于第三方动作 mod 的层，使其能覆盖 SSCU 的骨骼
        dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess.getPlayerAnimLayer(player).addAnimLayer(1, container);
        container.setAnimation(null);
        return container;
    }

    @Override
    public boolean play(@NotNull Object containerObj, @NotNull ResourceLocation animationID,
                        float speed, int fade, boolean needsCleanup) {
        dev.kosmx.playerAnim.api.IPlayable playable =
                dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry.getAnimation(animationID);
        if (playable == null) return needsCleanup;

        var container = (dev.kosmx.playerAnim.api.layered.ModifierLayer<dev.kosmx.playerAnim.api.layered.IAnimation>) containerObj;
        // 注意 PA 的 ModifierLayer.removeModifier(int) 是裸的 List.remove，容器为空时越界，
        // 所以 needsCleanup 这个状态必须由调用方跨 tick 记住（照搬 24786162^ 的 modified）。
        if (needsCleanup) container.removeModifier(0);
        container.addModifierBefore(new dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier(speed));
        // 缓动先固定 LINEAR：PA 侧的最短弧淡入修复留作后续任务。
        container.replaceAnimationWithFade(
                dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier.standardFadeIn(
                        fade, dev.kosmx.playerAnim.core.util.Ease.LINEAR),
                playable.playAnimation());
        container.setupAnim(1.0f / 20.0f);
        return true;
    }

    @Override
    public boolean playImmediate(@NotNull Object containerObj, @NotNull ResourceLocation animationID,
                                 boolean needsCleanup) {
        dev.kosmx.playerAnim.api.IPlayable playable =
                dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry.getAnimation(animationID);
        if (playable == null) return needsCleanup;

        var container = (dev.kosmx.playerAnim.api.layered.ModifierLayer<dev.kosmx.playerAnim.api.layered.IAnimation>) containerObj;
        if (needsCleanup) container.removeModifier(0);
        container.setAnimation(playable.playAnimation());
        container.setupAnim(1.0f / 20.0f);
        return false;
    }

    @Override
    public void stop(@NotNull Object containerObj) {
        ((dev.kosmx.playerAnim.api.layered.ModifierLayer<dev.kosmx.playerAnim.api.layered.IAnimation>) containerObj)
                .setAnimation(null);
    }

    @Override
    public @NotNull Vec3f getBoneTransform(@NotNull Player player, @NotNull String rawBoneName,
                                           @NotNull TransformType type, @NotNull Vec3f defaultValue) {
        if (!(player instanceof AbstractClientPlayer clientPlayer)) return defaultValue;
        if (!(clientPlayer instanceof dev.kosmx.playerAnim.impl.IAnimatedPlayer paPlayer)) return defaultValue;

        dev.kosmx.playerAnim.impl.animation.AnimationApplier applier = paPlayer.playerAnimator_getAnimation();
        if (applier == null || !applier.isActive()) return defaultValue;

        // 传原始骨骼名（extra_parts_map 的 camelCase key），不做归一化 —— 归一化只对 PAL 有意义；
        // PA 的 GeckoLibSerializer 对骨骼 key 做 snake2Camel，而 SSCU 这批名字本来就是 camelCase，
        // 所以那是恒等映射。
        dev.kosmx.playerAnim.api.TransformType paType = switch (type) {
            case POSITION -> dev.kosmx.playerAnim.api.TransformType.POSITION;
            case ROTATION -> dev.kosmx.playerAnim.api.TransformType.ROTATION;
            case SCALE -> dev.kosmx.playerAnim.api.TransformType.SCALE;
            case BEND -> dev.kosmx.playerAnim.api.TransformType.BEND;
        };

        dev.kosmx.playerAnim.core.util.Vec3f result = applier.get3DTransform(
                rawBoneName, paType,
                new dev.kosmx.playerAnim.core.util.Vec3f(defaultValue.x(), defaultValue.y(), defaultValue.z()));
        return new Vec3f(result.getX(), result.getY(), result.getZ());
    }
}
