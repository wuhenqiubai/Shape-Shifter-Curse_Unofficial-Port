package net.onixary.shapeShifterCurseFabric.integration.origins.badge;

import io.github.apace100.calio.data.SerializableData;
import net.minecraft.resources.Identifier;

public record SpriteBadge(Identifier spriteId) implements Badge {

    public SpriteBadge(SerializableData.Instance instance) {
        this(instance.getId("sprite"));
    }

    @Override
    public boolean hasTooltip() {
        return false;
    }

    // 该 badge 本就没有 tooltip（原 getTooltipComponents 返回空列表）；
    // 客户端侧由 BadgeTooltipRenderers 同样返回空列表。

    @Override
    public SerializableData.Instance toData(SerializableData.Instance instance) {
        instance.set("sprite", spriteId);
        return instance;
    }

    @Override
    public BadgeFactory getBadgeFactory() {
        return BadgeFactories.SPRITE;
    }

}
