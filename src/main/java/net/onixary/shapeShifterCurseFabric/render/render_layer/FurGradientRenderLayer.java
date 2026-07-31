package net.onixary.shapeShifterCurseFabric.render.render_layer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public abstract class FurGradientRenderLayer {

    // TODO: Satin 无 1.21.11 版，fur_gradient_remap core shader 需改用原版 RenderPipeline 注册。
    // 复刻方向：在 RenderPipelines 类似的静态注册里创建自定义 RenderPipeline（引用 shaders/core/fur_gradient_remap），
    // 然后通过 RenderSystem/GPU 设备 pipeline cache 注册。原 Satin 的
    // ShaderEffectManager/ManagedCoreShader/Uniform1f/EntitiesPreRenderCallback 已移除。
    private static int ticks;

    public static void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> ticks++);
    }
}
