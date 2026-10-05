package net.onixary.shapeShifterCurseFabric.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.StringSplitter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.Optional;

/** ./gradlew checkTooltipWrapping; fixed glyph widths avoid requiring a running client. */
public final class ItemTooltipWrappingCheck {
    public static void main(String[] args) {
        StringSplitter handler = new StringSplitter((codePoint, style) -> 1);
        Component description = Component.literal("abcdef").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("ghijkl").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        var lines = ItemTooltipWrapping.wrap(handler, description, 4);
        check(lines.size() == 3, "Long text should wrap at the requested width");
        check(lines.stream().map(Component::getString).reduce("", String::concat).equals("abcdefghijkl"), "Do not lose characters");
        StringBuilder yellow = new StringBuilder();
        StringBuilder red = new StringBuilder();
        for (Component line : lines) {
            check(handler.stringWidth(line) <= 4, "Each line must fit");
            line.visit((style, content) -> {
                if (content.isEmpty()) return Optional.empty();
                if (style.getColor().equals(Style.EMPTY.withColor(ChatFormatting.YELLOW).getColor())) {
                    yellow.append(content);
                } else {
                    check(style.getColor().equals(Style.EMPTY.withColor(ChatFormatting.RED).getColor()) && style.isBold(), "Preserve nested styles");
                    red.append(content);
                }
                return Optional.empty();
            }, Style.EMPTY);
        }
        check(yellow.toString().equals("abcdef") && red.toString().equals("ghijkl"), "Keep styles on their original spans");
        var explicit = ItemTooltipWrapping.wrap(handler, Component.literal("one\ntwo"), 220);
        check(explicit.size() == 2 && explicit.get(1).getString().equals("two"), "Honor explicit line breaks");
        Component shortLine = Component.literal("short");
        check(ItemTooltipWrapping.wrap(handler, shortLine, 220).get(0) == shortLine, "Retain short lines");
        check(ItemTooltipWrapping.wrap(handler, Component.empty(), 220).size() == 1, "Retain blank separators");
        System.out.println("Tooltip wrapping checks passed.");
    }

    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
