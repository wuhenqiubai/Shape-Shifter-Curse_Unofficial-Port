package net.onixary.shapeShifterCurseFabric.util.util.cost;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EmptyCostType implements IFUSDrawableCostType<EmptyCostType> {
    private static final Identifier id = ShapeShifterCurseFabric.identifier("empty");
    public static final Identifier TEXTURE = ShapeShifterCurseFabric.identifier("textures/gui/shape_shifter_tuner_ui.png");
    public static final int TEXTURE_WIDTH = 454;
    public static final int TEXTURE_HEIGHT = 190;

    @Override
    public int checkInterval() {
        return -1;
    }


    @Override
    public void drawIcon(GuiGraphicsExtractor context, @NotNull ICost costObject, @Nullable Player player, int x, int y, int z) {
        CostTypeIcons.drawIcon(this, context, costObject, player, x, y, z);
    }

    @Override
    public Identifier getID() {
        return id;
    }

    @Override
    public Component getAmountText(@NotNull ICost costObject, @Nullable Player player) {
        return Component.empty();
    }

    @Override
    public boolean canPay(@NotNull ICost costObject, @Nullable Player player) {
        return true;
    }

    @Override
    public void pay(@NotNull ICost costObject, @NotNull Player player) {
        return;
    }
}