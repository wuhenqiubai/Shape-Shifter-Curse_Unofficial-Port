package net.onixary.shapeShifterCurseFabric.util.util.cost;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EmptyCostType implements IFUSDrawableCostType<EmptyCostType> {
    private static final Identifier id = ShapeShifterCurseFabric.identifier("empty");

    @Override
    public Identifier getID() {
        return id;
    }

    // 图标绘制（原 drawIcon / drawOnHover 与 xpIconSprite 静态字段）已移到纯客户端的 CostTypeIcons
    // —— 原静态字段引用的 FormUpgradeScreen 是 extends Screen 的客户端类，会让本类在专用服务端加载失败。

    @Override
    public Component getAmountText(@NotNull ICost costObject, @Nullable Player player) {
        return Component.literal("");
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
