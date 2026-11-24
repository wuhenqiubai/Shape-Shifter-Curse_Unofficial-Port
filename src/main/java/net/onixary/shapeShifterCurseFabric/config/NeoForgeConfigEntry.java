package net.onixary.shapeShifterCurseFabric.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

@Environment(EnvType.CLIENT)
public final class NeoForgeConfigEntry {
    private static final int MAX_RETRY_TICKS = 200;
    private static int retries;

    private NeoForgeConfigEntry() {}

    public static void register() {
        if (!hasNeoForge()) {
            return;
        }
        if (NeoForgeConfigEntryImpl.install()) {
            return;
        }
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (retries < MAX_RETRY_TICKS) {
                retries++;
                NeoForgeConfigEntryImpl.install();
            }
        });
    }

    private static boolean hasNeoForge() {
        try {
            Class.forName("net.neoforged.fml.ModList", false, NeoForgeConfigEntry.class.getClassLoader());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
