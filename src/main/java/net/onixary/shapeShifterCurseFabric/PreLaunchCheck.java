package net.onixary.shapeShifterCurseFabric;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * 启动前检查已知的致命兼容性问题（在 Mixin 应用之前执行，故可以真正拦下启动）。
 *
 * <p>目前只查一条：Lithium 的 {@code collections.attributes} 优化。</p>
 *
 * <h2>为什么这条必须拦</h2>
 * <p>原版 {@code AttributeMap} 用 {@code ObjectOpenHashSet} 存 {@code attributesToUpdate}；
 * Lithium 的 {@code AttributeMapMixin} 把它换成了 fastutil 的 {@code ReferenceOpenHashSet(0)}
 * （容量 0，几乎每次 add 都 rehash）。而 {@code LivingEntity.refreshDirtyAttributes()} 是
 * 「一边 for-each 迭代这个集合、一边在循环体里调 {@code onAttributeUpdated}」——后者会连锁触发
 * {@code refreshDimensions()} / effect 增删等操作，可能往同一个集合里写入。</p>
 *
 * <p>换集合之前，这种「迭代中结构修改」最多是静默跳过；换成 {@code ReferenceOpenHashSet(0)} 后
 * 会让迭代器内部状态失效，抛
 * {@code NullPointerException: ... "this.wrapped" is null}（栈顶是
 * {@code ReferenceOpenHashSet$SetIterator.next}）。</p>
 *
 * <p>触发面是**所有生物**：新刷出的实体第一次 tick 时会有一批属性被标脏，
 * {@code LivingEntity.tick()} 末尾必然进入那段迭代。所以表现为「任意非玩家生物、看谁先加载就崩」，
 * 崩溃实体每次都不一样（实测 creeper / skeleton / glow_squid 都出现过）。</p>
 *
 * <p>注意这不是 SSC 自己的代码造成的：SSC 全量源码对那个脏属性集合零引用，
 * 真正让所有生物都进入这条危险路径的是随 Apoli 一起打包的 additionalentityattributes
 * （{@code LivingEntityScaleMixin} 注入 {@code LivingEntity.updateAttribute}）。
 * 但玩家看到的现象是「装了 SSC 才崩」，所以由 SSC 来做这个提示最合适。</p>
 */
public class PreLaunchCheck implements PreLaunchEntrypoint {

    private static final String LITHIUM_MOD_ID = "lithium";
    private static final String LITHIUM_CONFIG_FILE = "lithium.properties";

    /** Lithium 关闭该优化的配置键。@see <a href="https://github.com/CaffeineMC/lithium/blob/develop/lithium-fabric-mixin-config.md">Lithium Configuration File Summary</a> */
    private static final String ATTRIBUTE_MIXIN_KEY = "mixin.collections.attributes";

    @Override
    public void onPreLaunch() {
        checkLithiumAttributeOptimization();
    }

    private static void checkLithiumAttributeOptimization() {
        FabricLoader loader = FabricLoader.getInstance();
        if (!loader.isModLoaded(LITHIUM_MOD_ID)) {
            return; // 没装 Lithium，不会触发
        }

        Path configPath = loader.getConfigDir().resolve(LITHIUM_CONFIG_FILE);
        if (isOptimizationDisabled(configPath)) {
            return; // 玩家已经按提示关掉了
        }

        throw new LithiumAttributeConflictError(configPath);
    }

    /**
     * 判断玩家是否已关闭该 mixin。
     *
     * <p>Lithium 的配置里，选项为「关」有多种写法（{@code false} / {@code off}）；
     * 且部分版本用的是不带 {@code mixin.} 前缀的短键名，两种键都认。</p>
     */
    private static boolean isOptimizationDisabled(Path configPath) {
        if (!Files.isReadable(configPath)) {
            return false; // 配置文件还没生成 → 说明玩家没动过，优化处于默认开启状态
        }

        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(configPath)) {
            properties.load(in);
        } catch (IOException e) {
            // 读不出来就当作「没关」——宁可提示一次，也不要放过一个必然崩溃的配置
            return false;
        }

        return isFalse(properties.getProperty(ATTRIBUTE_MIXIN_KEY))
                || isFalse(properties.getProperty("collections.attributes"));
    }

    private static boolean isFalse(String value) {
        if (value == null) {
            return false;
        }
        String v = value.trim();
        return v.equalsIgnoreCase("false") || v.equalsIgnoreCase("off");
    }

    /** 用独立异常类型，便于玩家在日志里一眼认出这不是随机崩溃。 */
    public static class LithiumAttributeConflictError extends RuntimeException {
        public LithiumAttributeConflictError(Path configPath) {
            super(buildMessage(configPath));
        }

        // 面向国际玩家，信息一律用英文（中文在崩溃/启动日志里不通用）。
        private static String buildMessage(Path configPath) {
            String nl = System.lineSeparator();
            return nl
                    + "=========================================================================" + nl
                    + " Shape Shifter Curse Unofficial Port: startup blocked" + nl
                    + " Known conflict with Lithium detected" + nl
                    + "=========================================================================" + nl
                    + nl
                    + "Lithium's \"collections.attributes\" optimization makes this mod crash" + nl
                    + "immediately after you enter a world - any mob's tick throws a" + nl
                    + "NullPointerException with ReferenceOpenHashSet$SetIterator.next at the" + nl
                    + "top of the stack." + nl
                    + nl
                    + "TO FIX, edit this file:" + nl
                    + "  " + configPath + nl
                    + "and add (or change) the following line:" + nl
                    + nl
                    + "  " + ATTRIBUTE_MIXIN_KEY + "=false" + nl
                    + nl
                    + "Then restart the game." + nl
                    + nl
                    + "NOTE: this single optimization is the only conflict. Every other" + nl
                    + "Lithium optimization works fine - you do NOT need to remove Lithium." + nl
                    + "=========================================================================" + nl;
        }
    }
}
