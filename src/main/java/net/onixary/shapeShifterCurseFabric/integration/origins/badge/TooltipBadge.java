package net.onixary.shapeShifterCurseFabric.integration.origins.badge;

import io.github.apace100.calio.data.SerializableData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public record TooltipBadge(Identifier spriteId, Component text) implements Badge {

    public TooltipBadge(SerializableData.Instance instance) {
        this(instance.getId("sprite"), instance.get("text"));
    }

    @Override
    public boolean hasTooltip() {
        return true;
    }

    // tooltip 构建（原 static addLines / getTooltipComponents，签名含 Font 等客户端类）
    // 已移到纯客户端的 BadgeTooltipRenderers。

    @Override
    public SerializableData.Instance toData(SerializableData.Instance instance) {
        instance.set("sprite", spriteId);
        instance.set("text", text);
        return instance;
    }

    @Override
    public BadgeFactory getBadgeFactory() {
        return BadgeFactories.TOOLTIP;
    }

}
