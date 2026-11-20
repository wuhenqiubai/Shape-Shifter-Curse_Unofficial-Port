package io.github.apace100.apoli;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class ApoliMixinPlugin implements IMixinConfigPlugin {

    private boolean isNeoForge = false;

    /** Connector 环境下禁用、改由 {@code integration.connector} 包替代的主包 mixin。 */
    private List<String> preventMixins = List.of(
            ".mixin.ServerPlayerInteractionManagerMixin",
            ".mixin.PhantomSpawnerMixin"
    );

    @Override
    public void onLoad(String mixinPackage) {
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
