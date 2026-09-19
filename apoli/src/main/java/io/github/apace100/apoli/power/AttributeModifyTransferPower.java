package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.ClassDataRegistry;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.List;

public class AttributeModifyTransferPower extends Power {

    private final Class<?> modifyClass;
    private final Holder<Attribute> attribute;
    private final double valueMultiplier;

    public AttributeModifyTransferPower(PowerType<?> type, LivingEntity entity, Class<?> modifyClass, Holder<Attribute> attribute, double valueMultiplier) {
        super(type, entity);
        this.modifyClass = modifyClass;
        this.attribute = attribute;
        this.valueMultiplier = valueMultiplier;
    }

    public boolean doesApply(Class<?> cls) {
        return cls.equals(modifyClass);
    }

    public void addModifiers(List<Modifier> modifiers) {
        AttributeMap attrContainer = entity.getAttributes();
        if(attrContainer.hasAttribute(attribute)) {
            AttributeInstance attributeInstance = attrContainer.getInstance(attribute);
            attributeInstance.getModifiers().forEach(mod -> {
                AttributeModifier transferMod =
                    new AttributeModifier(mod.id(), mod.amount() * valueMultiplier, mod.operation());
                modifiers.add(ModifierUtil.fromAttributeModifier(transferMod));
            });
        }
    }

    public void apply(List<AttributeModifier> modifiers) {
        AttributeMap attrContainer = entity.getAttributes();
        if(attrContainer.hasAttribute(attribute)) {
            AttributeInstance attributeInstance = attrContainer.getInstance(attribute);
            attributeInstance.getModifiers().forEach(mod -> {
                AttributeModifier transferMod =
                    new AttributeModifier(mod.id(), mod.amount() * valueMultiplier, mod.operation());
                modifiers.add(transferMod);
            });
        }
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("attribute_modify_transfer"),
            new SerializableData()
                .add("class", ClassDataRegistry.get(Power.class).get().getDataType())
                .add("attribute", SerializableDataTypes.ATTRIBUTE)
                .add("multiplier", SerializableDataTypes.DOUBLE, 1.0),
            data ->
                (type, player) -> new AttributeModifyTransferPower(type, player,
                    data.get("class"),
                    data.get("attribute"),
                    data.getDouble("multiplier")))
            .allowCondition();
    }
}
