package net.onixary.shapeShifterCurseFabric.player_form.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.cursed_moon.CursedMoon;
import net.onixary.shapeShifterCurseFabric.data.StaticParams;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.util.UIPositionUtils;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

public class InstinctBarRenderer {
    private static final Identifier instinctBarID = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/instinct_bar.png");
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
        // 1.21.11: 无管线9参 blit 语义已变（归一化 UV），改用带管线13参（像素 UV，uWidth/vHeight=显示尺寸）
        context.blit(RenderPipelines.GUI_TEXTURED, instinctBarID, x, y, 0, currentBarY, 80 - instinctWidth, 5, 80 - instinctWidth, 5, 160, 40, -1);
        context.blit(RenderPipelines.GUI_TEXTURED, instinctBarID, x + 80 - instinctWidth, y, 160 - instinctWidth, currentBarY, instinctWidth, 5, instinctWidth, 5, 160, 40, -1);
        if (isInstinctLock) {
            context.blit(RenderPipelines.GUI_TEXTURED, instinctBarID, x, y, 0, 35, 80, 5, 80, 5, 160, 40, -1);
        }
    }
}