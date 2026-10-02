package net.onixary.shapeShifterCurseFabric.player_animator;

import com.zigythebird.playeranimcore.enums.TransformType;
import com.zigythebird.playeranimcore.math.Vec3f;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * PlayerAnimator 调用的抽象契约。
 *
 * <h2>这个接口存在的唯一理由：类加载隔离</h2>
 * 不是「双后端抽象」——实现永远只有一个（{@link PlayerAnimatorImpl}，用 PA 的 API）。
 * 它存在是为了让 {@link PlayerAnimatorCompat} 的**字节码里一个 {@code dev.kosmx} 引用都不出现**：
 * 只有这样才能保证 PlayerAnimator 缺席时，无论 JVM 在什么时机去验证/解析哪个方法，
 * 都不会触发对 PA 类的加载。
 *
 * <p>背景：曾经把 PA 调用直接写在 {@code PlayerAnimatorCompat} 里（方法体引用 + {@code available()}
 * 保护 + {@code ifeq} 跳过），在纯 PAL 环境下仍然 {@code NoClassDefFoundError}，
 * 崩点还是 Mixin 注入进 {@code AbstractClientPlayer} 的 {@code <init>} handler 上
 * ——说明「只要不执行就不会加载」这条经验规律在该环境（Mixin 重写 + 栈帧计算）下并不成立。
 * 所以改为：{@code PlayerAnimatorCompat} 只引用本接口与 PAL 类型，
 * {@link PlayerAnimatorImpl} 由 {@code Class.forName} 在**确认 PA 可用之后**才加载。
 *
 * <p>方法签名同样只用 PAL 类型 / {@code Object} / 基础类型——PAL 在 {@code fabric.mod.json}
 * 的 {@code depends} 里，必然存在。
 */
public interface PlayerAnimatorBridge {
    /** 为玩家建一个 PA 动画层（{@code ModifierLayer}），以 {@code Object} 返回。 */
    Object createLayer(AbstractClientPlayer player);

    /** 带淡入播放。{@code needsCleanup} 见 {@link PlayerAnimatorCompat#play}。 */
    boolean play(Object container, ResourceLocation animationID, float speed, int fade, boolean needsCleanup);

    /** 硬切播放（对应 skipFade）。 */
    boolean playImmediate(Object container, ResourceLocation animationID, boolean needsCleanup);

    /** 清空当前动画。 */
    void stop(Object container);

    /** 查询 PA 动画栈上某根骨骼的变换；PA 无数据时返回 {@code defaultValue}。 */
    Vec3f getBoneTransform(Player player, String rawBoneName, TransformType type, Vec3f defaultValue);
}
