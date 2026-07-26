package net.onixary.shapeShifterCurseFabric.player_form.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.cursed_moon.CursedMoon;
import net.onixary.shapeShifterCurseFabric.data.StaticParams;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.util.UIPositionUtils;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

public class InstinctBarRenderer {
    private static final ResourceLocation instinctBarID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/instinct_bar.png");
    private static final float increase1Threshold = StaticParams.INSTINCT_INCREASE_RATE + 0.005f;
    private static final float increase2Threshold = StaticParams.INSTINCT_INCREASE_RATE + 0.01f;
    private static final float increase3Threshold = StaticParams.INSTINCT_INCREASE_RATE + 0.1f;
    private static int currentBarY = 0;

    private static final Minecraft mc = Minecraft.getInstance();

    public void render(GuiGraphics context, float tickDelta) {
        if (Minecraft.getInstance().player == null) return;
        Player player = Minecraft.getInstance().player;
        IForm curForm = FormUtils.getPlayerForm(player);
        boolean showInstinctBar = !FormUtils.NoInstinct.hasFlag(curForm);
        if (!mc.options.hideGui && mc.gameMode != null && mc.gameMode.canHurtPlayer() && showInstinctBar) {
            Tuple<Integer, Integer> pos = UIPositionUtils.getCorrectPosition(ShapeShifterCurseFabric.clientConfig.instinctBarPosType, ShapeShifterCurseFabric.clientConfig.instinctBarPosOffsetX, ShapeShifterCurseFabric.clientConfig.instinctBarPosOffsetY);
            updateBarTextures(player);
            renderInstinctBar(context, tickDelta, pos.getA(), pos.getB(), player);
        }
    }

    public void updateBarTextures(Player player) {
        PlayerFormComponent component = PlayerFormComponent.COMPONENT.get(player);
        float rate = component.instinctRate;
        float baseRate = InstinctUtils.getBaseInstinctRate(player);
        if (rate > increase3Threshold) {
            currentBarY = 30;
        } else if (rate > increase2Threshold) {
            currentBarY = 25;
        } else if (rate > increase1Threshold) {
            currentBarY = 20;
        } else if (rate > baseRate) {
            currentBarY = 15;
        } else if (rate < 0) {
            currentBarY = 0;
        } else {
            currentBarY = 5;
        }
    }

    private void renderInstinctBar(GuiGraphics context, float tickDelta, int x, int y, Player player) {
        float instinctValue = InstinctUtils.getNowInstinct();
        float currentInstinct = Math.max(0.0f, Math.min(instinctValue, StaticParams.INSTINCT_MAX));
        float instinctProportion;
        IForm curForm = FormUtils.getPlayerForm(player);
        boolean isInstinctLock = FormUtils.LockInstinct.hasFlag(curForm) || CursedMoon.isInCursedMoon(player.level());
        instinctProportion = currentInstinct / StaticParams.INSTINCT_MAX;
        int instinctWidth = (int) Math.ceil(80 * instinctProportion);
        context.blit(instinctBarID, x, y, 0, currentBarY, 80 - instinctWidth, 5, 160, 40);
        context.blit(instinctBarID, x + 80 - instinctWidth, y, 160 - instinctWidth, currentBarY, instinctWidth, 5, 160, 40);
        if (isInstinctLock) {
            context.blit(instinctBarID, x, y, 0, 35, 80, 5, 160, 40);
        }
    }
}