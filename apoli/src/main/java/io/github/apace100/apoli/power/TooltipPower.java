package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class TooltipPower extends Power {

    private final Predicate<ItemStack> itemCondition;
    private final List<Component> texts = new LinkedList<>();
    private final int order;

    public TooltipPower(PowerType<?> type, LivingEntity entity, Predicate<ItemStack> itemCondition, int order) {
        super(type, entity);
        this.itemCondition = itemCondition;
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public void addText(Component text) {
        texts.add(text);
    }

    public void addToTooltip(List<Component> tooltip) {
        tooltip.addAll(texts);
    }

    public boolean doesApply(ItemStack stack) {
        return itemCondition == null || itemCondition.test(stack);
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("tooltip"),
            new SerializableData()
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("text", SerializableDataTypes.TEXT, null)
                .add("texts", SerializableDataType.list(SerializableDataTypes.TEXT), null)
                .add("order", SerializableDataTypes.INT, 0),
            data ->
                (type, player) -> {
                    TooltipPower ttp = new TooltipPower(type, player,
                        data.isPresent("item_condition") ? data.get("item_condition") : null,
                        data.get("order"));
                    data.ifPresent("text", ttp::addText);
                    data.<List<Component>>ifPresent("texts", t -> t.forEach(ttp::addText));
                    return ttp;
                })
            .allowCondition();
    }
}
