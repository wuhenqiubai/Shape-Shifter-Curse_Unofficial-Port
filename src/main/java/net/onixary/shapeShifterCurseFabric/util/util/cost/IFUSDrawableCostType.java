package net.onixary.shapeShifterCurseFabric.util.util.cost;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 可在「形态升级」界面（FormUpgradeScreen）中展示的 cost 类型。
 *
 * <p><b>注意这里没有 drawIcon / drawOnHover</b>：那两个方法的签名含客户端类 {@code GuiGraphics}，
 * 而实现类（{@code EmptyCostType} / {@code XPCostType} / {@code ItemCostType}）会被
 * {@code RegCostType} 的静态初始化在<b>专用服务端</b>注册（{@code onInitialize} → {@code StudioGenerated.register()}
 * → {@code NormalPerk} → {@code BaseCost} → {@code RegCostType.<clinit>}），
 * JVM 链接期解析方法签名就会去加载客户端类 → {@code Cannot load class ... in environment type SERVER} → 服务端启动失败。</p>
 *
 * <p>图标绘制已整体移到 {@code CostTypeIcons}（{@code @Environment(CLIENT)}）。
 * 本接口只保留服务端也安全的部分。</p>
 */
public interface IFUSDrawableCostType<T extends ICostType<T>> extends ICostType<T> {

    public default Component getAmountText(@NotNull ICost costObject, @Nullable Player player) {
        return Component.nullToEmpty(String.valueOf(costObject.getAmount()));
    }
}
