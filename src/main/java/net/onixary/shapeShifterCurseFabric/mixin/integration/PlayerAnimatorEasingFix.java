package net.onixary.shapeShifterCurseFabric.mixin.integration;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 修正 PlayerAnimator 的退化 {@code Easing.catmullRom}。
 *
 * <h2>问题</h2>
 * PA {@code 2.0.4+1.21.1} 的 {@code dev.kosmx.playerAnim.core.util.Easing#catmullRom(float)}
 * 实现展开后<b>恒等于 {@code n + 2}</b>（第 3、4 项的系数全消）：
 * <pre>
 * 0.5 * ( 2(n+1) + ((n+2)-n) + [0] + [0] ) = n + 2
 * </pre>
 * 它被 {@code Ease.CATMULLROM = easeInOut(Easing::catmullRom)} 包装后，曲线退化成
 * {@code 1 → 1.5 → -0.5 → 0}，在 t=0.5 处还不连续。而 PA 的关键帧插值是
 * {@code lerp(ease.invoke(f), before.value, after.value)} —— ease 在 t=0 时为 1，
 * 于是姿态<b>直接跳到下一个关键帧</b>，表现为「只播关键帧、帧间没有过渡」。
 *
 * <p>受影响的只有带 {@code "lerp_mode": "catmullrom"} 的动画（SSC 的 FERAL 形态全套）。
 * NORMAL 形态用普通 {@code vector}（默认 LINEAR）不受影响；PAL 的同名实现是正确的，
 * 所以纯 PAL 路径也没问题。</p>
 *
 * <h2>修法</h2>
 * 直接改基函数本身 —— 它是整个 PA 里唯一退化的 easing，且只有 {@code Ease.java:65}
 * 一个调用者，一处修复覆盖全部入口（JSON 的 {@code lerp_mode}/{@code easing} 字段、
 * 以及二进制格式按 id 36 取 {@code Ease.CATMULLROM} 的路径）。
 *
 * <p>⚠ {@code catmullRom(float)} 只收一个参数，<b>拿不到</b> Catmull-Rom 需要的四个控制点
 * （相邻关键帧的值），所以真正的曲线数学上做不到。这里用端点补齐的近似
 * （取 p0=p1=0、p2=p3=1 的四点插值化简）：</p>
 * <pre>
 * f(t) = 0.5 * (t + 3t² − 2t³)      f(0)=0, f(1)=1, 单调递增
 * </pre>
 * 它随后仍会被 {@code easeInOut} 再包一层，整体是一条有效的 S 形缓动曲线，
 * 保留了「两端缓、中间快」的观感。若实机觉得偏陡，可退回 {@code return n}（等于当 LINEAR 用）。</p>
 *
 * <p>⚠ 用 {@code targets} 字符串而不是 {@code @Mixin(Easing.class)}：后者会让本类的字节码里
 * 出现 PA 的 {@code CONSTANT_Class} 引用，违反仓库「常驻类不得静态引用 dev.kosmx.*」的约定
 * （见 {@code player_animator/PlayerAnimatorCompat} 的类注释）。{@code targets} 形式下本类
 * 不含任何 PA 类型引用。</p>
 */
@Mixin(value = dev.kosmx.playerAnim.core.util.Easing.class, remap = false)
public class PlayerAnimatorEasingFix {

    @ModifyReturnValue(method = "catmullRom(F)F", at = @At("RETURN"))
    private static float ssc$fixCatmullRom(float original, float n) {
        // 端点补齐的 Catmull-Rom 近似：p0=p1=0, p2=p3=1
        // 0.5 * (2*p1 + (p2-p0)t + (2p0-5p1+4p2-p3)t² + (-p0+3p1-3p2+p3)t³)
        //   = 0.5 * (t + 3t² - 2t³)
        return 0.5f * (n + 3.0f * n * n - 2.0f * n * n * n);
    }
}
