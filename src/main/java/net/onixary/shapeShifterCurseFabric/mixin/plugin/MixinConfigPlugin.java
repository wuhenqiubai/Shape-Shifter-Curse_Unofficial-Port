package net.onixary.shapeShifterCurseFabric.mixin.plugin;

import net.fabricmc.loader.api.FabricLoader;
import net.onixary.shapeShifterCurseFabric.util.Accessory.AccessoryPriorityUtils;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MixinConfigPlugin implements IMixinConfigPlugin {
    public static final Logger LOGGER = LoggerFactory.getLogger("shape-shifter-curse-mixin");

    private record MixinRequiredMods(String[] value, String[] not) { }
    private static final HashMap<String, MixinRequiredMods> mixinRequiredMods = new HashMap<>();

    private static final HashMap<String, String> mixinAccessoryMixin = new HashMap<>();

    static {
        mixinRequiredMods.put("net.onixary.shapeShifterCurseFabric.mixin.PlayerEntityRendererFallFlyingMixin", new MixinRequiredMods(new String[]{}, new String[]{"vivecraft"}));
        var isNeoForge = FabricLoader.getInstance().isModLoaded("neoforge");
        // TODO: NeoForge 版 AppleSkin API 与 Fabric 版不兼容 (method 签名不同)
        // 先完全跳过，避免 Mixin 崩溃
        mixinRequiredMods.put("net.onixary.shapeShifterCurseFabric.mixin.integration.AppleSkin",
            new MixinRequiredMods(new String[]{"appleskin"}, isNeoForge ? new String[]{"neoforge"} : new String[]{}));
        mixinRequiredMods.put("net.onixary.shapeShifterCurseFabric.mixin.accessory.TrinketImpl", new MixinRequiredMods(new String[]{"trinkets"}, new String[]{}));
        mixinRequiredMods.put("net.onixary.shapeShifterCurseFabric.mixin.accessory.TrinketItemMixin", new MixinRequiredMods(new String[]{"trinkets"}, new String[]{}));
        mixinRequiredMods.put("net.onixary.shapeShifterCurseFabric.mixin.accessory.TrinketSlotMixin", new MixinRequiredMods(new String[]{"trinkets"}, new String[]{}));
        mixinAccessoryMixin.put("net.onixary.shapeShifterCurseFabric.mixin.accessory.TrinketImpl", "trinkets");
    }

    @Override
    public void onLoad(String mixinPackage) {
        // 插件加载时调用
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    private static Boolean isNeoForgeCached = null;

    private static boolean isNeoForge() {
        if (isNeoForgeCached == null) {
            isNeoForgeCached = FabricLoader.getInstance().isModLoaded("neoforge");
        }
        return isNeoForgeCached;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // NeoForge 下 Forge 的 net.minecraftforge.common.util.LazyOptional 不存在，
        // 跳过所有 forge 包下的 mixin
        if (mixinClassName.startsWith("net.onixary.shapeShifterCurseFabric.mixin.forge.")
            && isNeoForge()) {
            LOGGER.info("NeoForge detected, skipping Forge-specific mixin: {}", mixinClassName);
            return false;
        }
        // 原先的代码 使用硬编码的方式
        // 检查是否为 PlayerEntityRendererFallFlyingMixin
        // if (mixinClassName.endsWith("PlayerEntityRendererFallFlyingMixin")) {
        //     boolean isViveCraftLoaded = FabricLoader.getInstance().isModLoaded("vivecraft");
        //     if (isViveCraftLoaded) {
        //         System.out.println("[ShapeShifterCurse] ViveCraft detected, skipping PlayerEntityRendererFallFlyingMixin");
        //         return false; // 完全跳过这个 mixin
        //     }
        // }

        // 检查是否符合MixinRequiredMods注解的要求 注解会导致Mixin提前加载 临时使用static注册
        if (mixinRequiredMods.containsKey(mixinClassName)) {
            MixinRequiredMods requiredMods = mixinRequiredMods.get(mixinClassName);
            for (String mod : requiredMods.value) {
                if (!FabricLoader.getInstance().isModLoaded(mod)) {
                    LOGGER.info("{} not detected, skipping {}", mod, mixinClassName);
                    return false; // 完全跳过这个 mixin
                }
            }
            for (String mod : requiredMods.not) {
                if (FabricLoader.getInstance().isModLoaded(mod)) {
                    LOGGER.info("{} detected, skipping {}", mod, mixinClassName);
                    return false; // 完全跳过这个 mixin
                }
            }
        }
        if (mixinAccessoryMixin.containsKey(mixinClassName)) {
            // NeoForge 下 Forge Curios 不可用，强制回退到 Trinkets
            if (isNeoForge() && "trinkets".equals(mixinAccessoryMixin.get(mixinClassName))) {
                return true;
            }
            if (!Objects.equals(AccessoryPriorityUtils.getHighestPriorityPlugin(), mixinAccessoryMixin.get(mixinClassName))) {
                LOGGER.info("{} is highest priority, skipping {}", AccessoryPriorityUtils.getHighestPriorityPlugin(), mixinClassName);
                return false;
            }
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // 不需要特殊处理
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // 应用前处理（可选）
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // 应用后处理（可选）
    }
}