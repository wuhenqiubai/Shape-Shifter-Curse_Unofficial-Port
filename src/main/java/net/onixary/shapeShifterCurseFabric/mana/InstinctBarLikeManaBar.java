package net.onixary.shapeShifterCurseFabric.mana;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.util.UIPositionUtils;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

@Environment(EnvType.CLIENT)
public class InstinctBarLikeManaBar implements IManaRender{
    private static final Minecraft mc = Minecraft.getInstance();
    private static final ResourceLocation BarTexFullID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/instinct_bar_full.png");
    private static final ResourceLocation BarTexEmptyID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/instinct_bar_empty.png");

    @Override
    public boolean OverrideInstinctBar() {
        return true;
    }

    @Override
    public void render(GuiGraphics context, float tickDelta) {
        if (!mc.options.hideGui) {
            Tuple<Integer, Integer> pos = UIPositionUtils.getCorrectPosition(ShapeShifterCurseFabric.clientConfig.instinctBarPosType, ShapeShifterCurseFabric.clientConfig.instinctBarPosOffsetX, ShapeShifterCurseFabric.clientConfig.instinctBarPosOffsetY);
            this.renderBar(context, tickDelta, pos.getA(), pos.getB());
        }
    }

    private void renderBar(GuiGraphics context, float tickDelta, int x, int y) {
        double mana = ManaUtils.getPlayerMana(mc.player);
        double maxMana = ManaUtils.getPlayerMaxMana(mc.player);
        double manaRegen = ManaUtils.getPlayerManaRegen(mc.player);
        int remainTicks = -1;
        if (manaRegen > 0) {
            remainTicks = (int) Math.ceil((maxMana - mana) / manaRegen);
        }
        int instinctWidth = (int) Math.ceil(80 * ManaUtils.getManaPercent(mana, maxMana, 0.0d));
        context.blit(BarTexEmptyID, x, y, 0, 0, 80, 5, 80, 5);
        context.blit(BarTexFullID, x, y, 0, 0, instinctWidth, 5, 80, 5);
        StringBuilder manaString = new StringBuilder();
        manaString.append((int) mana).append("/").append((int) maxMana);
        if (remainTicks > 0) {
            manaString.append(" (").append(remainTicks).append(")");
        } else if (remainTicks < 0) {
            manaString.append(" (").append("?").append(")");
        }
        Component manaText = Component.literal(manaString.toString());
        int manaTextWidth = mc.font.width(manaText);
        context.drawString(mc.font, manaText, x + (80 - manaTextWidth) / 2, y - 2, 0xFFFFFF, false);
    }
}
