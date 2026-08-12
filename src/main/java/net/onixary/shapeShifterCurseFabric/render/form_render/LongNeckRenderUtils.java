package net.onixary.shapeShifterCurseFabric.render.form_render;

import dev.tr7zw.firstperson.FirstPersonModelCore;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.util.ClientUtils;


public class LongNeckRenderUtils {
    private static final boolean IS_FIRST_PERSON_MOD_LOADED = FabricLoader.getInstance().isModLoaded("firstperson");

    public static boolean isFirstPersonModelActiveForSelf(Player player) {
        Minecraft client = Minecraft.getInstance();
        return IS_FIRST_PERSON_MOD_LOADED
                && !ClientUtils.isOpenInventoryScreen
                && player.isLocalPlayer()
                && client.options.getCameraType().isFirstPerson()
                && FirstPersonModelCore.instance.isEnabled();
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