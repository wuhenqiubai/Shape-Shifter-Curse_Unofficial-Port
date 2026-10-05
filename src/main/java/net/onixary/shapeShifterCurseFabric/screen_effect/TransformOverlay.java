package net.onixary.shapeShifterCurseFabric.screen_effect;

import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

/**
 * 变身叠加层的**状态**部分（纯数据，服务端安全）。
 *
 * <p><b>为什么渲染要拆出去</b>：本类原先自带 {@code renderHud(GuiGraphics)}，其方法签名含客户端类
 * {@code GuiGraphics}。而 {@code ShapeShifterCurseFabric#onInitialize}（main entrypoint，专用服务端同样执行）
 * 第 247 行会调 {@link #INSTANCE}.{@link #init()}，触发本类的加载 —— JVM 链接期解析方法签名时就会去加载
 * {@code GuiGraphics}（进而拉起 {@code Screen} 等），报
 * {@code Cannot load class ... in environment type SERVER} → {@code ExceptionInInitializerError} → 服务端启动失败。</p>
 *
 * <p>注意方法体里的客户端调用**不影响**类加载（只在执行到该行时才解析），只有**签名**会。
 * 因此只需把带客户端签名的方法移出，渲染实现见 {@code TransformOverlayRenderer}（{@code @Environment(CLIENT)}）。</p>
 */
public final class TransformOverlay {
    public static final TransformOverlay INSTANCE = new TransformOverlay();
    final Identifier nausea_texture = Identifier.fromNamespaceAndPath(MOD_ID, "textures/overlay/nausea_black.png");
    final Identifier black_texture = Identifier.fromNamespaceAndPath(MOD_ID, "textures/overlay/black.png");

    // 渲染器（同包、客户端侧）直接读取这些状态，故为包级可见
    boolean enableOverlay = false;
    float strength_nausea = 0.0f;
    float strength_black = 0.0f;

    public void init() {
        enableOverlay = false;
        strength_nausea = 0.0f;
        strength_black = 0.0f;
    }

    public void setEnableOverlay(boolean enableOverlay) {
        this.enableOverlay = enableOverlay;
    }

    public void setNauesaStrength(float strength) {
        this.strength_nausea = Mth.clamp(strength, 0.0f, 1.0f);
    }

    public void setBlackStrength(float strength) {
        this.strength_black = Mth.clamp(strength, 0.0f, 1.0f);
    }
}
