package net.onixary.shapeShifterCurseFabric.render.render_layer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public abstract class FurGradientRenderLayer {

    private static int ticks;

    public static void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> ticks++);
    }
}
