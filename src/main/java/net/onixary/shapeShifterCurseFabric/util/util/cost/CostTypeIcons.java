package net.onixary.shapeShifterCurseFabric.util.util.cost;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.custom_ui.FormUpgradeScreen;
import net.onixary.shapeShifterCurseFabric.util.util.BaseSprite;
import net.onixary.shapeShifterCurseFabric.util.util.ISprite;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * cost 类型的图标绘制——**纯客户端**实现。
 *
 * <p><b>为什么从各 CostType 类里搬出来</b>：{@code EmptyCostType} / {@code XPCostType} / {@code ItemCostType}
 * 原先各自持有 {@code new BaseSprite(FormUpgradeScreen.TEXTURE, …)} 静态字段，并实现带 {@code GuiGraphicsExtractor} 签名的
 * {@code drawIcon / drawOnHover}。而 {@code FormUpgradeScreen} 是 {@code extends Screen} 的客户端类，
 * 这些 CostType 又会在<b>专用服务端</b>被 {@code RegCostType} 的静态初始化注册
 * （{@code onInitialize} → {@code StudioGenerated.register()} → {@code NormalPerk} → {@code BaseCost}
 * → {@code RegCostType.<clinit>}）→ 链接期加载客户端类 → 服务端启动失败。</p>
 *
 * <p>所以把「贴图 + 绘制」整体挪到本类，CostType 只保留服务端也需要的判定/结算逻辑。</p>
 */
@Environment(EnvType.CLIENT)
public final class CostTypeIcons {

    private static final ISprite XP_ICON_SPRITE =
            new BaseSprite(FormUpgradeScreen.TEXTURE, FormUpgradeScreen.TEXTURE_WIDTH, FormUpgradeScreen.TEXTURE_HEIGHT, 434, 17, 18, 18);
    private static final ISprite EMPTY_ICON_SPRITE =
            new BaseSprite(FormUpgradeScreen.TEXTURE, FormUpgradeScreen.TEXTURE_WIDTH, FormUpgradeScreen.TEXTURE_HEIGHT, 434, 35, 18, 18);
    private static final ISprite ITEM_ICON_SPRITE =
            new BaseSprite(FormUpgradeScreen.TEXTURE, FormUpgradeScreen.TEXTURE_WIDTH, FormUpgradeScreen.TEXTURE_HEIGHT, 434, 54, 18, 18);

    private CostTypeIcons() {
    }

    /** 对应原 {@code IFUSDrawableCostType#drawIcon}。 */
    public static void drawIcon(ICostType<?> type, GuiGraphicsExtractor context, @NotNull ICost costObject,
                                @Nullable Player player, int x, int y, int z) {
        if (type instanceof XPCostType) {
            XP_ICON_SPRITE.draw(context, x, y, z, 0, 0, 18, 18);
        } else if (type instanceof EmptyCostType) {
            EMPTY_ICON_SPRITE.draw(context, x, y, z, 0, 0, 18, 18);
        } else if (type instanceof ItemCostType) {
            ITEM_ICON_SPRITE.draw(context, x, y, z, 0, 0, 18, 18);
            if (costObject instanceof ItemCost cost) {
                ItemStack stack = cost.getExampleStack();
                if (!stack.isEmpty()) {
                    // 26.1: renderItem 改名为 item(ItemStack, int, int)
                    context.item(stack, x + 1, y + 1);
                }
            }
        }
    }

    /** 对应原 {@code IFUSDrawableCostType#drawOnHover}。 */
    public static void drawOnHover(ICostType<?> type, GuiGraphicsExtractor context, @NotNull ICost costObject,
                                   @Nullable Player player, int x, int y, int z, int mouseX, int mouseY) {
        if (!(type instanceof ItemCostType)) {
            return;
        }
        if (mouseX <= 0 || mouseX >= 18 || mouseY <= 0 || mouseY >= 18) {
            return;
        }
        if (!(costObject instanceof ItemCost cost)) {
            return;
        }
        ItemStack stack = cost.getExampleStack();
        if (stack.isEmpty()) {
            return;
        }
        // 1.21.11 移除了 renderTooltip(Font, ItemStack, x, y)；对应物是 setTooltipForNextFrame。
        context.setTooltipForNextFrame(Minecraft.getInstance().font, stack, x + mouseX, y + mouseY);
    }
}
