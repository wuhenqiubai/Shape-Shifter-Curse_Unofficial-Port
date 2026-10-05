package net.onixary.shapeShifterCurseFabric.integration.origins.badge;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyCraftingPower;
import io.github.apace100.apoli.power.PowerType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;
import net.onixary.shapeShifterCurseFabric.integration.origins.screen.tooltip.CraftingRecipeTooltipComponent;
import net.onixary.shapeShifterCurseFabric.integration.origins.util.PowerKeyManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Badge 的 tooltip 构建——**纯客户端**实现。
 *
 * <p><b>为什么单独抽一个类</b>：{@link Badge} 接口原先自带
 * {@code getTooltipComponents(PowerType, int, float, Font)}，其签名含 {@code Font} 与
 * {@code ClientTooltipComponent} 两个客户端类。而该接口在<b>专用服务端</b>也会被加载
 * （{@code Origins#registerResourceListeners} → {@code BadgeManager.init()} 注册 badge 工厂，
 * 且 {@code BadgeManager} 的静态字段引用了 {@code Badge.class}），
 * JVM 解析方法签名时就会去加载那些客户端类 → {@code Cannot load class ... in environment type SERVER}
 * → {@code BootstrapMethodError} → {@code ExceptionInInitializerError} → 服务端启动失败。</p>
 *
 * <p>注意 {@code @Environment(EnvType.CLIENT)} 注解**并不能**阻止链接期的签名解析，
 * 所以必须把带客户端类型的方法整体移出接口/实现类，而不是只加注解。</p>
 */
@Environment(EnvType.CLIENT)
public final class BadgeTooltipRenderers {

    private BadgeTooltipRenderers() {
    }

    /** 按 badge 具体类型分发 tooltip，替代原先 {@code Badge#getTooltipComponents} 的多态调用。 */
    public static List<ClientTooltipComponent> getTooltipComponents(Badge badge, PowerType<?> powerType,
                                                                    int widthLimit, float time, Font textRenderer) {
        if (badge instanceof TooltipBadge tooltipBadge) {
            List<ClientTooltipComponent> tooltips = new LinkedList<>();
            addLines(tooltips, tooltipBadge.text(), textRenderer, widthLimit);
            return tooltips;
        }
        if (badge instanceof SpriteBadge) {
            return new ArrayList<>();
        }
        if (badge instanceof KeybindBadge keybindBadge) {
            List<ClientTooltipComponent> tooltips = new LinkedList<>();
            // Component.nullToEmpty 返回的是 Component 而非 MutableComponent，必须强转后才能链式 append
            MutableComponent keyText = ((MutableComponent) Component.nullToEmpty("["))
                    .append(KeyMapping.createNameSupplier(PowerKeyManager.getKeyIdentifier(powerType.getIdentifier())).get())
                    .append(Component.nullToEmpty("]"));
            addLines(tooltips, Component.translatable(keybindBadge.text(), keyText), textRenderer, widthLimit);
            return tooltips;
        }
        if (badge instanceof CraftingRecipeBadge craftingRecipeBadge) {
            return craftingRecipeTooltip(craftingRecipeBadge, widthLimit, time, textRenderer);
        }
        return new ArrayList<>();
    }

    /** TooltipBadge / KeybindBadge 共用的折行逻辑（原来两边各抄了一份）。 */
    public static void addLines(List<ClientTooltipComponent> tooltips, Component text, Font textRenderer, int widthLimit) {
        if (textRenderer.width(text) > widthLimit) {
            for (FormattedCharSequence orderedText : textRenderer.split(text, widthLimit)) {
                tooltips.add(new ClientTextTooltip(orderedText));
            }
        } else {
            tooltips.add(new ClientTextTooltip(text.getVisualOrderText()));
        }
    }

    private static List<ClientTooltipComponent> craftingRecipeTooltip(CraftingRecipeBadge badge,
                                                                      int widthLimit, float time, Font textRenderer) {
        Minecraft client = Minecraft.getInstance();
        List<ClientTooltipComponent> tooltips = new LinkedList<>();
        if (client.level == null) {
            Origins.LOGGER.warn("Could not construct crafting recipe badge, because world was null");
            return tooltips;
        }
        NonNullList<ItemStack> inputs = peekInputs(badge, time);
        int recipeWidth = getRecipeWidth(badge);
        int recipeHeight = getRecipeHeight(badge);
        ItemStack output = getResultStack(badge);

        // 应用 ModifyCraftingPower 修改输出
        ItemStack[] outputRef = { output };
        CraftingInput craftingInput = CraftingInput.of(recipeWidth, recipeHeight,
                new ArrayList<>(inputs.subList(0, Math.min(recipeWidth * recipeHeight, inputs.size()))));
        if (client.player != null) {
            PowerHolderComponent.getPowers(client.player, ModifyCraftingPower.class)
                    .stream()
                    .filter(p -> p.doesApply(craftingInput, badge.recipe()))
                    .findFirst()
                    .ifPresent(p -> outputRef[0] = p.getNewResult(craftingInput, badge.recipe().value()));
        }

        if (client.options.advancedItemTooltips) {
            Component recipeIdText = Component.literal(badge.recipe().id().toString()).withStyle(ChatFormatting.DARK_GRAY);
            widthLimit = Math.max(130, textRenderer.width(recipeIdText));
            if (badge.prefix() != null) addLines(tooltips, badge.prefix(), textRenderer, widthLimit);
            tooltips.add(new CraftingRecipeTooltipComponent(recipeWidth, inputs, outputRef[0]));
            if (badge.suffix() != null) addLines(tooltips, badge.suffix(), textRenderer, widthLimit);
            addLines(tooltips, recipeIdText, textRenderer, widthLimit);
        } else {
            widthLimit = 130;
            if (badge.prefix() != null) addLines(tooltips, badge.prefix(), textRenderer, widthLimit);
            tooltips.add(new CraftingRecipeTooltipComponent(recipeWidth, inputs, outputRef[0]));
            if (badge.suffix() != null) addLines(tooltips, badge.suffix(), textRenderer, widthLimit);
        }
        return tooltips;
    }

    private static List<SlotDisplay> getIngredientDisplays(CraftingRecipeBadge badge) {
        for (RecipeDisplay display : badge.recipe().value().display()) {
            if (display instanceof ShapedCraftingRecipeDisplay shaped) {
                return shaped.ingredients();
            } else if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
                return shapeless.ingredients();
            }
        }
        return List.of();
    }

    private static int getRecipeWidth(CraftingRecipeBadge badge) {
        for (RecipeDisplay display : badge.recipe().value().display()) {
            if (display instanceof ShapedCraftingRecipeDisplay shaped) {
                return shaped.width();
            }
        }
        return 3;
    }

    private static int getRecipeHeight(CraftingRecipeBadge badge) {
        for (RecipeDisplay display : badge.recipe().value().display()) {
            if (display instanceof ShapedCraftingRecipeDisplay shaped) {
                return shaped.height();
            }
        }
        return 3;
    }

    private static ItemStack getResultStack(CraftingRecipeBadge badge) {
        for (RecipeDisplay display : badge.recipe().value().display()) {
            SlotDisplay result;
            if (display instanceof ShapedCraftingRecipeDisplay shaped) {
                result = shaped.result();
            } else if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
                result = shapeless.result();
            } else {
                continue;
            }
            List<ItemStack> stacks = result.resolveForStacks(SlotDisplayContext.fromLevel(Minecraft.getInstance().level));
            if (!stacks.isEmpty()) return stacks.get(0);
        }
        return ItemStack.EMPTY;
    }

    private static NonNullList<ItemStack> peekInputs(CraftingRecipeBadge badge, float time) {
        int seed = Mth.floor(time / 30);
        NonNullList<ItemStack> inputs = NonNullList.withSize(9, ItemStack.EMPTY);
        List<SlotDisplay> slotDisplays = getIngredientDisplays(badge);
        if (slotDisplays.isEmpty()) return inputs;
        ContextMap contextMap = SlotDisplayContext.fromLevel(Minecraft.getInstance().level);
        for (int index = 0; index < slotDisplays.size() && index < 9; ++index) {
            List<ItemStack> stacks = slotDisplays.get(index).resolveForStacks(contextMap);
            if (!stacks.isEmpty()) inputs.set(index, stacks.get(seed % stacks.size()));
        }
        return inputs;
    }
}
