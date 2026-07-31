package net.onixary.shapeShifterCurseFabric.mana;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.additional_power.ChargePower;
import net.onixary.shapeShifterCurseFabric.util.UIPositionUtils;

public class WebResourceBar implements IManaRender {
    private ChargePower powerTemp_1;
    private ChargePower powerTemp_2;
    private int powerTempTimer = 0;

    private static final Minecraft mc = Minecraft.getInstance();
    private static final Identifier BarTexID = ShapeShifterCurseFabric.identifier("textures/gui/web_bar.png");

    @Override
    public boolean OverrideInstinctBar() {
        return false;
    }

    public void render(GuiGraphics context, float tickDelta) {
        if (!mc.options.hideGui) {
            Tuple<Integer, Integer> pos = UIPositionUtils.getCorrectPosition(ShapeShifterCurseFabric.clientConfig.manaBarPosType, ShapeShifterCurseFabric.clientConfig.manaBarPosOffsetX, ShapeShifterCurseFabric.clientConfig.manaBarPosOffsetY);
            this.renderBar(context, tickDelta, pos.getA(), pos.getB());
        }
    }

    public int getChargeLevel() {
        // 每帧查一次有点费性能 还是每60帧查一次吧(渲染帧)
        if (powerTempTimer > 60) {
            powerTemp_1 = null;
            powerTemp_2 = null;
            for (ChargePower power : PowerHolderComponent.getPowers(mc.player, ChargePower.class)) {
                if (ShapeShifterCurseFabric.identifier("web_charge_1").equals(power.chargePowerID)) {
                    powerTemp_1 = power;
                }
                if (ShapeShifterCurseFabric.identifier("web_charge_2").equals(power.chargePowerID)) {
                    powerTemp_2 = power;
                }
            }
            powerTempTimer = 0;
        }
        powerTempTimer++;
        int RTier = 0;
        if (powerTemp_1 != null) {
            RTier = Math.max(RTier, powerTemp_1.renderTier);
        }
        if (powerTemp_2 != null) {
            RTier = Math.max(RTier, powerTemp_2.renderTier);
        }
        return RTier;
    }

    private void renderBar(GuiGraphics context, float tickDelta, int x, int y) {
        if (mc.player == null) {
            return;
        }
        double mana = ManaUtils.getPlayerMana(mc.player);
        double maxMana = ManaUtils.getPlayerMaxMana(mc.player);
        double manaRegen = ManaUtils.getPlayerManaRegen(mc.player);

        int manaWidth = (int)Math.ceil((double)80.0F * ManaUtils.getManaPercent(mana, maxMana, 0.0F));
        context.blit(BarTexID, x, y, 0, 0, 80, 5, 80, 18);
        context.blit(BarTexID, x, y, 0, 5, manaWidth, 5, 80, 18);

        Component manaText = Component.literal((int) mana + "/" + (int) maxMana);
        context.drawString(mc.font, manaText, x + 10, y - 8, manaRegen == 0 ? 0xFF7F7F7F : 0xFF00CFFF, false);

        int chargeLevel = this.getChargeLevel();
        context.blit(BarTexID, x, y - 8, chargeLevel * 8, 10, 8, 8, 80, 18);
    }
}