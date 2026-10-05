package net.onixary.shapeShifterCurseFabric.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.data.StaticParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public final class ItemTooltipWrapping {
    private ItemTooltipWrapping() {}

    public static void register() {
        // 1.21.1 起 Fabric 的 ItemTooltipCallback 由 3 参变为 4 参（中间插入 TooltipType）。
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            // ⚠ 用 BuiltInRegistries（Registry<Item>，有 getKey），不是 Registries ——
            // Registries.ITEM 是 ResourceKey<Registry<Item>>，没有 getId/getKey。
            boolean modItem = BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace()
                    .equals(ShapeShifterCurseFabric.MOD_ID);
            StringSplitter handler = Minecraft.getInstance().font.getSplitter();
            // Keep the item name intact; wrap descriptions, including our potion/tool annotations.
            for (var iterator = lines.listIterator(Math.min(1, lines.size())); iterator.hasNext();) {
                Component line = iterator.next();
                boolean modDescription = line.getContents() instanceof TranslatableContents translation
                        && (translation.getKey().startsWith("tooltip.shape_shifter_curse.")
                        || translation.getKey().startsWith("item.shape-shifter-curse."));
                if (modItem || modDescription) {
                    List<Component> wrapped = wrap(handler, line, StaticParams.ITEM_TOOLTIP_MAX_WIDTH);
                    iterator.remove();
                    wrapped.forEach(iterator::add);
                }
            }
        });
    }

    static List<Component> wrap(StringSplitter handler, Component text, int maxWidth) {
        int width = Math.max(1, maxWidth);
        if (handler.stringWidth(text) <= width && !text.getString().contains("\n")) {
            return List.of(text);
        }
        List<Component> result = new ArrayList<>();
        for (var line : handler.splitLines(text, width, Style.EMPTY)) {
            MutableComponent wrapped = Component.empty();
            line.visit((style, content) -> {
                wrapped.append(Component.literal(content).setStyle(style));
                return Optional.empty();
            }, Style.EMPTY);
            result.add(wrapped);
        }
        return result;
    }
}
