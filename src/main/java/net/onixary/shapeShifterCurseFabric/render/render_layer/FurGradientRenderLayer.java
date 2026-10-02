package net.onixary.shapeShifterCurseFabric.render.render_layer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public abstract class FurGradientRenderLayer {

    // [未启用，勿再当作待办] Satin 无 1.21.11 版。
    // 关键事实：fur gradient 整套在 1.21.1/1.21.11 两个分支上都无调用点（getFurLayer 只有定义），从未启用。
    // 要恢复需自定义 core shader（RenderPipeline + shaders/core/fur_gradient_remap），
    // 但那会与光影包（Iris 等）冲突，故不采用着色器方案。如需恢复请先确认调用方与光影兼容性。
    // （原 Satin 的 ShaderEffectManager / ManagedCoreShader / Uniform1f / EntitiesPreRenderCallback 均已移除）
    private static int ticks;

    public static void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> ticks++);
    }
}
