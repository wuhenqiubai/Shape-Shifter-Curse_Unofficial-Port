package net.onixary.shapeShifterCurseFabric.perk;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.List;

public interface IDependent {
    int nodeBaseX = 25;
    int posXPerTier = 50;
    int nodeLineRootXOffset = 11;
    int nodeLineDependXOffset = -10;
    int LineColor = 0xFF9F9F9F;

    int NodeDrawStartX = -7;
    int NodeDrawStartY = -7;

    boolean isDependentPerk(@NotNull ResourceLocation perk);

    boolean isAllDependentGained(Player player, @Nullable List<ResourceLocation> playerGainedPerk);

    default void drawDependentLine(GuiGraphics drawContext, Vector2i nodeCenter, PerkTree tree, PerkTree.PerkNode perkNode) {
        return;
    }
}
