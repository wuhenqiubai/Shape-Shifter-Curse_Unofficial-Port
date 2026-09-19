package io.github.apace100.apoli.util;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AttributedEntityAttributeModifier {

    private final Holder<Attribute> attribute;
    private final AttributeModifier modifier;

    public AttributedEntityAttributeModifier(Holder<Attribute> attribute, AttributeModifier modifier) {
        this.attribute = attribute;
        this.modifier = modifier;
    }

    public AttributeModifier getModifier() {
        return modifier;
    }

    public Holder<Attribute> getAttribute() {
        return attribute;
    }

    public Holder<Attribute> getAttributeHolder() {
        return attribute;
    }
}
