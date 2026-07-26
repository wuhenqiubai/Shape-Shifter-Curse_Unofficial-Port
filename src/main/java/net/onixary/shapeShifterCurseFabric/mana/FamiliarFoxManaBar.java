package net.onixary.shapeShifterCurseFabric.mana;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.util.UIPositionUtils;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

@Environment(EnvType.CLIENT)
public class FamiliarFoxManaBar implements IManaRender{
    private static final Minecraft mc = Minecraft.getInstance();

    private static final ResourceLocation BarTexFullID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/familiar_fox_mana_bar_full.png");
    private static final ResourceLocation BarTexEmptyID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/familiar_fox_mana_bar_empty.png");

    @Override
    public boolean OverrideInstinctBar() {
        return false;
    }

    @Override
    public void render(GuiGraphics context, float tickDelta) {
        if (!mc.options.hideGui) {
            // int width = mc.getWindow().getScaledWidth();
            // int height = mc.getWindow().getScaledHeight();
            // //float x = (float) width / 2 + 11;
            // float x = (float)width / 2 + 100;
            // // 39 is the height of the health bar
            // float y = height - 39;
            // y += 22;
            Tuple<Integer, Integer> pos = UIPositionUtils.getCorrectPosition(ShapeShifterCurseFabric.clientConfig.manaBarPosType, ShapeShifterCurseFabric.clientConfig.manaBarPosOffsetX, ShapeShifterCurseFabric.clientConfig.manaBarPosOffsetY);
            this.renderBar(context, tickDelta, pos.getA(), pos.getB());
        }
    }

    private void renderBar(GuiGraphics context, float tickDelta, int x, int y) {
        int instinctWidth = (int) Math.ceil(80 * ManaUtils.getPlayerManaPercent(mc.player, 0.0d));
        context.blit(BarTexEmptyID, x, y, 0, 0, 80, 5, 80, 5);
        context.blit(BarTexFullID, x, y, 0, 0, instinctWidth, 5, 80, 5);
    }
}
