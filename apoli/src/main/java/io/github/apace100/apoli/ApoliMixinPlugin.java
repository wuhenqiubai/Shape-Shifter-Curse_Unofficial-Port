package io.github.apace100.apoli;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * NeoForge/Connector 兼容（对应 1.21.1 提交 ea98804）。
 *
 * <p>NeoForge 会重新编译 MC 源码，使局部变量顺序、lambda 编号、匿名内部类编号（$1/$2/...）发生变化，
 * 导致 {@code @ModifyVariable(ordinal = N)} 与 {@code @Mixin(targets = "...$N")} 这类注入点**静默失效**。
 * 本插件在 Connector 环境下：禁用主包里不兼容的那几个 mixin，改用 {@code integration.connector} 下的替代实现；
 * Fabric 环境下一切照旧。
 *
 * <p><b>26.1 相对 1.21.1 的精简</b>：1.21.1 版还列了
 * {@code .mixin.EntityMixin} / {@code .mixin.ServerPlayerEntityMixin} / {@code .mixin.ElytraFeatureRendererMixin}，
 * 但：
 * <ul>
 *   <li>前两个的 connector 副本与「已硬化注入点」的主版**逐字节等价**（diff 只差注释）——
 *       主版注入点已改成 isInWall HEAD / startSleepInBed HEAD 这类跨环境稳定的位置，副本纯属冗余，故不再列；</li>
 *   <li>{@code ElytraFeatureRendererMixin} 在 26.1 已被上游整个重写成
 *       {@code @ModifyExpressionValue(... ItemStack.get(DataComponentType))}（打 {@code WingsLayer.submit}），
 *       注入点是稳定的接口调用而非匿名类/lambda，暂不需要替代版。</li>
 * </ul>
 * 磨石的 3 个 mixin 也已改成注入外层 {@code GrindstoneMenu}（构造器 + createResult），跨环境单轨稳定，
 * 因此同样**不列入** —— 否则 NeoForge 下禁用后没有替代，{@code modify_grindstone} 功能会直接丢失。
 */
public class ApoliMixinPlugin implements IMixinConfigPlugin {

    private boolean isNeoForge = false;

    /** Connector 环境下禁用、改由 {@code integration.connector} 包替代的主包 mixin。 */
    private List<String> preventMixins = List.of(
            ".mixin.ServerPlayerInteractionManagerMixin",
            ".mixin.PhantomSpawnerMixin"
    );

    @Override
    public void onLoad(String mixinPackage) {
        // Sinytra Connector 的 FML mod id 是 "connector"（不是 "connectormod"）。
        // 先试 FabricLoader，再退回 FML ModList（反射，避免 Fabric 侧硬依赖 FML 类）。
        isNeoForge = isConnectorLoaded();
        System.out.println("[ApoliMixinPlugin] onLoad: mixinPackage=" + mixinPackage + ", isNeoForge=" + isNeoForge);
    }

    private boolean isConnectorLoaded() {
        try {
            if (FabricLoader.getInstance().isModLoaded("connector")) {
                return true;
            }
        } catch (Throwable ignored) {
        }
        try {
            Class<?> modListClass;
            try {
                modListClass = Class.forName("net.neoforged.fml.loading.moddiscovery.ModList");
            } catch (ClassNotFoundException cnfe) {
                modListClass = Class.forName("net.minecraftforge.fml.loading.moddiscovery.ModList");
            }
            Object modList = modListClass.getMethod("get").invoke(null);
            return (Boolean) modListClass.getMethod("isLoaded", String.class).invoke(modList, "connector");
        } catch (Throwable ignored) {
        }
        return false;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // 主包不兼容 mixin：NeoForge 下禁用，Fabric 下保留。
        // 注意 ".mixin.X" 不会匹配 ".mixin.integration.connector.X"（后者 ".mixin." 之后是 "integration"）。
        if (preventMixins.stream().anyMatch(mixinClassName::contains)) {
            return !isNeoForge;
        }
        // connector 包替代实现：仅 NeoForge 下启用
        if (mixinClassName.contains(".mixin.integration.connector")) {
            return isNeoForge;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
