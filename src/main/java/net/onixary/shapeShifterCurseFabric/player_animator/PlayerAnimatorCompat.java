package net.onixary.shapeShifterCurseFabric.player_animator;

import com.zigythebird.playeranimcore.enums.TransformType;
import com.zigythebird.playeranimcore.math.Vec3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PlayerAnimator（{@code dev.kosmx.playerAnim}）兼容层门面。
 *
 * <h2>为什么要它</h2>
 * SSCU 的形态动画跑在 PAL 上，而第三方动作 mod（TacZ、Emotecraft、BetterCombat 等）跑在
 * PlayerAnimator 上。两个库各有自己的 {@code AnimationStack}，互不相通——双方各自去写 vanilla
 * {@code PlayerModel} 的同一批 {@code ModelPart}，结果是「一方覆盖另一方」，而不是按层优先级叠加。
 * <p>
 * 本类让 SSCU 在装了 PlayerAnimator 时把形态动画也放进 <b>PA 的 stack</b>（优先级 1）。
 * PA 的 {@code AnimationStack.get3DTransform} 是链式覆盖：逐层把上一层的输出当作 {@code value0}
 * 传下去，而 {@code KeyframeAnimationPlayer} 对**未定义骨骼 / 未启用的轴**原样返回 {@code value0}。
 * 所以 SSCU 低优先级打底、第三方 mod 用更高优先级只覆盖它自己定义的那几根骨骼——这正是「兼容」的机制。
 *
 * <h2>类加载安全（改这个文件前务必读）</h2>
 * PlayerAnimator 是<b>可选</b>依赖。本类的字节码里<b>绝不能出现任何指向 {@code dev.kosmx.*} 的
 * {@code CONSTANT_Class}</b>——PA 类型只允许以<b>字符串</b>形式出现（字符串常量不会触发类加载）。
 * <p>
 * 这条规矩是被实际崩溃逼出来的：最初把 PA 调用直接写在本类的方法体里（有 {@code available()}
 * 保护，字节码上是干净的 {@code ifeq} 跳过），在纯 PAL 环境下**仍然** {@code NoClassDefFoundError}，
 * 且崩点是 Mixin 注入进 {@code AbstractClientPlayer.<init>} 的 handler——说明「方法体里的引用只要
 * 不执行就不解析」这条经验规律，在 Mixin 重写目标类并计算栈帧的情形下并不成立。
 * <p>
 * 所以现在：PA 的实现全部放进 {@link PlayerAnimatorImpl}（一个静态引用它的地方都没有），
 * 本类只在**确认 PA 的类真的能加载之后**，用 {@code Class.forName} 把它动态加载成
 * {@link PlayerAnimatorBridge}。这样即使 JVM 提前验证/解析本类的任意方法，也只会看到
 * PAL 类型、{@code Object}、{@code PlayerAnimatorBridge} 和字符串。
 *
 * <h2>判据不用 {@code isModLoaded}</h2>
 * 只信「类是否真的可加载」：{@code isModLoaded} 会把别的 mod 声明的 {@code provides} 也算进来，
 * 一旦误判为「有 PA」，后续走到 PA 分支就是崩。反过来说，只要这些类真的能加载，是谁提供的并不重要。
 */
public final class PlayerAnimatorCompat {
    private PlayerAnimatorCompat() {}

    private static final Logger LOGGER = LoggerFactory.getLogger("shape-shifter-curse/playeranimator");

    /** PA 的核心类清单。这些是<b>字符串</b>，不是类引用，不会触发类加载。 */
    private static final String[] REQUIRED_CLASSES = {
            "dev.kosmx.playerAnim.api.layered.ModifierLayer",
            "dev.kosmx.playerAnim.api.layered.IAnimation",
            "dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier",
            "dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier",
            "dev.kosmx.playerAnim.api.IPlayable",
            "dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess",
            "dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry",
            "dev.kosmx.playerAnim.impl.IAnimatedPlayer",
            "dev.kosmx.playerAnim.impl.animation.AnimationApplier",
            "dev.kosmx.playerAnim.core.util.Ease",
    };

    private static final String IMPL_CLASS =
            "net.onixary.shapeShifterCurseFabric.player_animator.PlayerAnimatorImpl";

    /** PA 可用时为 {@link PlayerAnimatorImpl} 实例，否则为 null。 */
    private static final @Nullable PlayerAnimatorBridge BRIDGE = loadBridge();

    private static @Nullable PlayerAnimatorBridge loadBridge() {
        try {
            // 专用服务端固定走 PAL：AnimationHolder 双端都会构造（AnimStateControllerDP 的构造函数
            // 由 AnimRegistries 静态注册触发），而 PA 的 PlayerAnimationRegistry 标了 @Environment(CLIENT)。
            if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) return null;
            ClassLoader loader = PlayerAnimatorCompat.class.getClassLoader();
            for (String className : REQUIRED_CLASSES) {
                // initialize=false：只确认可加载，不触发其静态初始化
                Class.forName(className, false, loader);
            }
            PlayerAnimatorBridge bridge =
                    (PlayerAnimatorBridge) Class.forName(IMPL_CLASS, true, loader)
                            .getDeclaredConstructor().newInstance();
            LOGGER.info("[SSC] PlayerAnimator 已就位，形态动画将放入 PA 的 stack（与第三方动作 mod 按层混合）");
            return bridge;
        } catch (Throwable t) {
            // 两种情况都落这里：① 没装 PA；② 有 mod 用 provides 声明了 playeranimator 却没提供这些类。
            // 都退回 PAL 路径——不能让兼容层本身成为崩溃源。
            LOGGER.info("[SSC] 未启用 PlayerAnimator 路径（{}），形态动画使用 PAL。", t.toString());
            return null;
        }
    }

    /**
     * PlayerAnimator 是否可用（客户端且其类真的可加载）。为 true 时 SSCU 的动画全部改走 PA 路径。
     * <p>
     * 所有 PA 相关的调用都<b>必须</b>先过这个判断——虽然下面每个方法内部也都做了 null 检查。
     */
    public static boolean available() {
        return BRIDGE != null;
    }

    // ------------------------------------------------------------------
    // 骨骼查询
    // ------------------------------------------------------------------

    /**
     * 查询 PA 动画栈上某根骨骼的变换
     * （{@link net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimSystem#getPlayerBone3DTransform} 的 PA 分支）。
     *
     * @param rawBoneName 动画里的<b>原始骨骼名</b>，直接用 camelCase，<b>不要归一化</b>。
     *                    PA 的 {@code GeckoLibSerializer} 对骨骼 key 做 {@code snake2Camel}，
     *                    而 SSCU 的动画资源本来就是 camelCase，所以那是恒等映射。
     *                    这与 PAL 侧恰好相反——PAL 要 snake_case，归一化只发生在 PAL 分支内部。
     * @return PA 无数据时返回 {@code defaultValue}
     */
    public static @NotNull Vec3f getBoneTransform(@NotNull Player player, @NotNull String rawBoneName,
                                                  @NotNull TransformType type, @NotNull Vec3f defaultValue) {
        if (BRIDGE == null) return defaultValue;
        return BRIDGE.getBoneTransform(player, rawBoneName, type, defaultValue);
    }

    // ------------------------------------------------------------------
    // 播放驱动
    // ------------------------------------------------------------------

    /**
     * 为玩家建一个 PA 动画层并挂到 PA 的 stack 上，在玩家的 {@code <init>} 里调用一次。
     *
     * @return 该玩家的层容器（{@code ModifierLayer<IAnimation>}，以 {@code Object} 返回）；不可用时为 null
     */
    public static @Nullable Object createLayer(@NotNull AbstractClientPlayer player) {
        if (BRIDGE == null) return null;
        return BRIDGE.createLayer(player);
    }

    /**
     * 带淡入地播放动画。
     *
     * @param needsCleanup 上一次调用后容器里是否已经存在 modifier
     * @return 本次调用后容器里是否已存在 modifier（供调用方存回）
     */
    public static boolean play(@NotNull Object containerObj, @NotNull ResourceLocation animationID,
                               float speed, int fade, boolean needsCleanup) {
        if (BRIDGE == null) return needsCleanup;
        return BRIDGE.play(containerObj, animationID, speed, fade, needsCleanup);
    }

    /**
     * 硬切动画（对应 {@code AnimationHolder#isSkipFade()}）。
     *
     * @return 本次调用后容器里是否已存在 modifier——恒为 false，因为没有加任何 modifier
     */
    public static boolean playImmediate(@NotNull Object containerObj, @NotNull ResourceLocation animationID,
                                        boolean needsCleanup) {
        if (BRIDGE == null) return needsCleanup;
        return BRIDGE.playImmediate(containerObj, animationID, needsCleanup);
    }

    /** 清空当前动画（对应 {@code AnimSystem#getAnimation()} 返回 null 的情况）。 */
    public static void stop(@NotNull Object containerObj) {
        if (BRIDGE == null) return;
        BRIDGE.stop(containerObj);
    }
}
