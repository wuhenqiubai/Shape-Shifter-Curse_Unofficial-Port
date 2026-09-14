package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.onixary.shapeShifterCurseFabric.cursed_moon.CursedMoon;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 诅咒月期间把天空光照染成紫粉色。
 * <p>
 * 26.1 迁移说明：原实现 mixin 的是 {@code LightTexture}，并在 {@code updateLightTexture(F)V}
 * 里注入 FIELD {@code LightTexture.blockLightRedFlicker} 来拿到局部 {@code Vector3f}。
 * 26.1 把该类改名 {@code Lightmap}、光照计算整体搬到 {@link LightmapRenderStateExtractor}，
 * 且 {@code blockLightRedFlicker} 变成 {@code LightmapRenderStateExtractor.blockLightFlicker}（private）——
 * 原来的注入点整体消失（primer 26.1 的 {@code LightTexture -> Lightmap} 段只记录了改名，
 * 未提供「改 lightmap 颜色」的正规入口）。
 * <p>
 * 现改为在 {@code LightmapRenderStateExtractor#extract} 的 TAIL 注入，直接改写
 * {@link LightmapRenderState#skyLightColor}（public 字段）——语义等价：
 * 原局部变量就是这个天空光色，只是当时存在方法内、现在存进了 render state。
 * <p>
 * ⚠ 若日后要改为数据包驱动，正规载体是 {@code EnvironmentAttributes.SKY_LIGHT_COLOR}
 * （extract 里的取值来源），而不是继续改这个 mixin。
 */
@Mixin(LightmapRenderStateExtractor.class)
public abstract class CursedMoonLightmapMixin {
    @Inject(
        method = "extract(Lnet/minecraft/client/renderer/state/LightmapRenderState;F)V",
        at = @At("TAIL")
    )
    private void ssc$cursedMoonSkyLightColor(LightmapRenderState renderState, float partialTicks, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        // 原代码直接传 client.level（未判空）—— isCursedMoonDay 内部会解引用，故补上守卫
        if (client.level == null || !CursedMoon.isCursedMoonDay(client.level)) {
            return;
        }
        // 保留原有混合公式（原封不动从 updateLightTexture 那版搬过来）
        Vector3f modifiedColor = new Vector3f(1.0F, 0.24F, 0.82F);
        float skyBlend = 1.0F - partialTicks - client.level.getRainLevel(1.0F);
        // skyLightColor 是 Vector3fc（只读视图），须复制成可变的再 lerp
        Vector3f skyLightColor = new Vector3f(renderState.skyLightColor);
        skyLightColor.lerp(modifiedColor, skyBlend);
        renderState.skyLightColor = skyLightColor;
    }
}
