package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.Identifier;
import net.onixary.shapeShifterCurseFabric.cursed_moon.CursedMoon;
import net.onixary.shapeShifterCurseFabric.cursed_moon.CursedMoonClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

@Mixin(value = LevelRenderer.class, priority = 949)
public class MoonPhaseRenderMixin {
	@Unique
	private final Identifier Vanilla_MOON_PHASES = Identifier.parse("textures/environment/moon_phases.png");

	@Unique
	private final Identifier CURSED_MOON_PHASES = Identifier.fromNamespaceAndPath(MOD_ID,"textures/environment/cursed_moon_phases.png");

	@Unique
	public Identifier getMoonIdentifier() {
		Minecraft client = Minecraft.getInstance();
		if (client.level != null) {
			return CursedMoon.isCursedMoonDay(client.level) ? CURSED_MOON_PHASES : Vanilla_MOON_PHASES;
		}
		return CursedMoonClient.isCursedMoon ? CURSED_MOON_PHASES : Vanilla_MOON_PHASES;
	}

	// TODO: 1.21.11 天空渲染已重构，注入点无法映射，旧逻辑暂时禁用：
	// 1. LevelRenderer.renderSky() 已移除，天空渲染移入 SkyRenderer（renderSunMoonAndStars/renderMoon）；
	// 2. 月亮相位改为构造时从 celestials 图集烘焙 GpuBuffer（SkyRenderer.buildMoonPhases），
	//    RenderSystem.setShaderTexture 已删除，无法再用 @ModifyArg 替换 moon_phases.png。
	// 待后续迁移：通过资源包覆盖 minecraft:moon/<phase> 精灵，或在新 SkyRenderer 管线中另找钩子。
	//@ModifyArg(method = "renderSky(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V",
	//        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/Identifier;)V", ordinal = 1))
	//private Identifier getMoonPhaseTexture(Identifier identifier) {
	//    Identifier moonId = getMoonIdentifier();
	//    return moonId != null ? moonId : identifier;
	//}
}
