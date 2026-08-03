package net.onixary.shapeShifterCurseFabric.render.form_render;

import dev.tr7zw.firstperson.api.FirstPersonAPI;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.util.ClientUtils;


public class LongNeckRenderUtils {
    private static final boolean IS_FIRST_PERSON_MOD_LOADED = FabricLoader.getInstance().isModLoaded("firstperson");

    public static boolean isFirstPersonModelActiveForSelf(Player player) {
        // FPM 2.7.2：配置级判断（本地玩家 + 第一人称相机 + FPM 启用）排除 GUI 玩家模型预览。
        // 用 ClientUtils.isOpenInventoryScreen（InventoryScreenMixin 在 renderEntityInInventoryFollowsMouse
        // HEAD/RETURN 置位，覆盖物品栏/书/配色界面预览）区分：GUI 预览渲染中保留头部，世界背景（第一人称视角）隐藏头部。
        // 不能用 screen == null：GUI 打开时世界背景的玩家 Geo 身体仍在第一人称视角渲染，screen 判断会漏隐藏 → 开 GUI 穿模。
        // 不能用 FirstPersonAPI.isRenderingPlayer()——SSC 形态渲染（实体渲染阶段）不在其置位区间，会失效。
        Minecraft client = Minecraft.getInstance();
        return IS_FIRST_PERSON_MOD_LOADED && player.isLocalPlayer() && !ClientUtils.isOpenInventoryScreen
                && client.options.getCameraType().isFirstPerson() && FirstPersonAPI.isEnabled();
    }

    public static float lerpAngle(float delta, float start, float end) {
        return start + Mth.wrapDegrees(end - start) * delta;
    }

    public static float lerpAngleAwayFrom(float delta, float start, float end, float avoidAngle) {
        if (Math.abs(Mth.wrapDegrees(avoidAngle - end)) < 0.0001F) {
            return lerpAngle(delta, start, end);
        }
        start = Mth.wrapDegrees(start);
        end = Mth.wrapDegrees(end);
        float diff = Mth.wrapDegrees(end - start);
        float avoidDiff = Mth.wrapDegrees(avoidAngle - start);
        boolean flipDir = Math.signum(diff) == Math.signum(avoidDiff) && Math.abs(diff) > Math.abs(avoidDiff);
        if (flipDir) {
            diff = Math.copySign(360.0F - Math.abs(diff), -diff);
        }
        return Mth.wrapDegrees(start + diff * delta);
    }
}
