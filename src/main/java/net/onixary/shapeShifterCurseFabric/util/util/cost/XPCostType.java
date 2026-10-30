package net.onixary.shapeShifterCurseFabric.util.util.cost;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.util.ClientUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class XPCostType implements IFUSDrawableCostType<XPCostType> {
    private static final Identifier id = ShapeShifterCurseFabric.identifier("xp");
    public static final Identifier TEXTURE = ShapeShifterCurseFabric.identifier("textures/gui/shape_shifter_tuner_ui.png");
    public static final int TEXTURE_WIDTH = 454;
    public static final int TEXTURE_HEIGHT = 190;


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
        if (player == null) {
            return IFUSDrawableCostType.super.getAmountText(costObject, player);
        }
        return Component.literal(String.valueOf(costObject.getAmount()))
                .append(Component.translatable("gui.shape-shifter-curse.perk.cost_separator"))
                .append(String.valueOf(player.totalExperience));
    }

    @Override
    public boolean canPay(@NotNull ICost costObject, @Nullable Player player) {
        int costAmount = costObject.getAmount();
        if (player == null) {
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                player = ClientUtils.getPlayer();
            } else {
                throw new RuntimeException("CostType.canPay Player Argument In ServerSide Must NotNull");
            }
        }
        return player.totalExperience >= costAmount;
    }

    @Override
    public void pay(@NotNull ICost costObject, @NotNull Player player) {
        player.giveExperiencePoints(-costObject.getAmount());
    }

    @Override
    public Component getResourceNotEnoughText(@NotNull ICost costObject, @Nullable Player player) {
        return Component.translatable("gui.shape-shifter-curse.perk.insufficient_xp");
    }
}
