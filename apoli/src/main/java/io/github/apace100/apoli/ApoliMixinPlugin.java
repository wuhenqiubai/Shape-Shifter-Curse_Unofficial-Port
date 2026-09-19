package io.github.apace100.apoli;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * NeoForge/Connector 兼容：
 * NeoForge 重编译 MC 源码会使匿名内部类（$1/$2/...）重新编号，导致 @Mixin(targets = "...$N") 注入点失效崩服。
 * Connector 环境下禁用这些匿名类 mixin（功能降级：ModifyGrindstonePower 相关），Fabric 环境保持原样。
 * 参考 Apoli 2.9.2 的 ApoliMixinPlugin 机制。
 */
public class ApoliMixinPlugin implements IMixinConfigPlugin {

    private boolean isNeoForge = false;

    // NeoForge 下禁用的主包 mixin（不兼容：lambda/匿名类/@ModifyVariable 在 NeoForge 重编译后失效），由 connector 包替代。
    // 注意：Grindstone 3 个已改外层 GrindstoneMenu 注入（createResult/构造器，跨 Fabric/NeoForge 稳定单轨），不再列入——否则 NeoForge 下禁用后无替代，modify_grindstone 功能丢失。
    private List<String> preventMixins = List.of(
            ".mixin.EntityMixin",
            ".mixin.ServerPlayerEntityMixin",
            ".mixin.ElytraFeatureRendererMixin",
            ".mixin.ServerPlayerInteractionManagerMixin",
            ".mixin.PhantomSpawnerMixin"
    );

    @Override
    public void onLoad(String mixinPackage) {
        // Sinytra Connector 文档：Connector 的 FML mod id 是 "connector"（不是 "connectormod"）。
        // 用 FML ModList（反射）+ FabricLoader 检测。
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
        // 主包不兼容 mixin：NeoForge 下禁用（.mixin.X 不匹配 .mixin.integration.connector.X），Fabric 下保留
        if (preventMixins.stream().anyMatch(mixinClassName::contains)) {
            return !isNeoForge;
        }
        // connector 包替代：仅 NeoForge 下启用
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
