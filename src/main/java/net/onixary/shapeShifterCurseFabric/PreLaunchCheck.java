package net.onixary.shapeShifterCurseFabric;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class PreLaunchCheck implements PreLaunchEntrypoint {

    private static final String LITHIUM_MOD_ID = "lithium";
    private static final String LITHIUM_CONFIG_FILE = "lithium.properties";

    private static final String ATTRIBUTE_MIXIN_KEY = "mixin.collections.attributes";

    @Override
    public void onPreLaunch() {
        checkLithiumAttributeOptimization();
    }

    private static void checkLithiumAttributeOptimization() {
        FabricLoader loader = FabricLoader.getInstance();
        if (!loader.isModLoaded(LITHIUM_MOD_ID)) {
            return;
        }

        Path configPath = loader.getConfigDir().resolve(LITHIUM_CONFIG_FILE);
        if (isOptimizationDisabled(configPath)) {
            return;
        }

        throw new LithiumAttributeConflictError(configPath);
    }

    private static boolean isOptimizationDisabled(Path configPath) {
        if (!Files.isReadable(configPath)) {
            return false;
        }

        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(configPath)) {
            properties.load(in);
        } catch (IOException e) {
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

    public static class LithiumAttributeConflictError extends RuntimeException {
        public LithiumAttributeConflictError(Path configPath) {
            super(buildMessage(configPath));
        }

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
