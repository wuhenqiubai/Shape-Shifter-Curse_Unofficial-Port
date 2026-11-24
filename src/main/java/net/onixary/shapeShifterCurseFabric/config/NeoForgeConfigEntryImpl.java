package net.onixary.shapeShifterCurseFabric.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public final class NeoForgeConfigEntryImpl {
    private static final String FML_MOD_ID = "shape_shifter_curse_unofficial";
    private static final String MOD_LIST_CLASS = "net.neoforged.fml.ModList";
    private static final String FACTORY_CLASS = "net.neoforged.neoforge.client.gui.IConfigScreenFactory";
    private static boolean installed;
    private static boolean reportedMissing;
    private static boolean reportedFailure;

    private NeoForgeConfigEntryImpl() {}

    public static boolean install() {
        if (installed) {
            return true;
        }
        try {
            Class<?> modListClass = Class.forName(MOD_LIST_CLASS);
            Object modList = modListClass.getMethod("get").invoke(null);
            if (modList == null) {
                return false;
            }
            Object found = modListClass.getMethod("getModContainerById", String.class).invoke(modList, FML_MOD_ID);
            if (!(found instanceof Optional<?> optional)) {
                reportOnceFailure("unexpected container lookup result: " + found);
                return false;
            }
            if (optional.isEmpty()) {
                if (!reportedMissing) {
                    reportedMissing = true;
                    Object listed = modListClass.getMethod("isLoaded", String.class).invoke(modList, FML_MOD_ID);
                    ShapeShifterCurseFabric.LOGGER.warn("NeoForge config: no FML ModContainer for {} (listed in FML mod list: {})", FML_MOD_ID, listed);
                }
                return false;
            }
            Object container = optional.get();
            Class<?> factoryClass = Class.forName(FACTORY_CLASS);
            Object factory = createFactory(factoryClass);
            Method register = container.getClass().getMethod("registerExtensionPoint", Class.class, Supplier.class);
            register.invoke(container, factoryClass, (Supplier<Object>) () -> factory);
            installed = true;
            ShapeShifterCurseFabric.LOGGER.info("NeoForge config: registered config screen on {} via {}", FML_MOD_ID, container.getClass().getName());
            return true;
        } catch (Throwable t) {
            reportOnceFailure(t.toString());
            return false;
        }
    }

    private static void reportOnceFailure(String detail) {
        if (!reportedFailure) {
            reportedFailure = true;
            ShapeShifterCurseFabric.LOGGER.warn("NeoForge config: registration failed: {}", detail);
        }
    }

    private static Object createFactory(Class<?> factoryClass) {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("createScreen".equals(method.getName()) && args != null && args.length == 2 && args[1] instanceof Screen parent) {
                return new ConfigMenuScreen(parent);
            }
            if ("equals".equals(method.getName()) && args != null && args.length == 1) {
                return proxy == args[0];
            }
            if ("hashCode".equals(method.getName())) {
                return System.identityHashCode(proxy);
            }
            if ("toString".equals(method.getName())) {
                return "SSC NeoForge config screen factory";
            }
            return null;
        };
        return Proxy.newProxyInstance(factoryClass.getClassLoader(), new Class<?>[]{factoryClass}, handler);
    }
}
