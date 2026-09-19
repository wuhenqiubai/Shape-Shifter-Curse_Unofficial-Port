package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class PreventGameEventPower extends Power {

    private final TagKey<GameEvent> tag;
    private final HolderSet<GameEvent> list;
    private final Consumer<Entity> entityAction;

    public PreventGameEventPower(PowerType<?> type, LivingEntity entity, TagKey<GameEvent> tag, HolderSet<GameEvent> list, Consumer<Entity> entityAction) {
        super(type, entity);
        this.tag = tag;
        this.list = list;
        this.entityAction = entityAction;
    }

    public void executeAction(Entity entity) {
        if(entityAction != null) {
            entityAction.accept(entity);
        }
    }

    public boolean doesPrevent(Holder<GameEvent> event) {
        if(tag != null && event.is(tag)) {
            return true;
        }
        if(list != null && list.contains(event)) {
            return true;
        }
        return false;
    }

    public static PowerFactory createFactory() {
        // 1.21.1: event/events 必须以 Holder<GameEvent> 表示 —— 本类内部存 HolderSet<GameEvent>、
        // doesPrevent 也收 Holder<GameEvent>。若沿用 SerializableDataTypes.GAME_EVENT（裸 GameEvent），
        // data.get("event") 会返回 GameEvent，塞进 List<Holder<GameEvent>> 时因泛型擦除不报错，
        // 直到 HolderSet.direct(eventList) 遍历才抛 "GameEvent cannot be cast to Holder"，
        // 导致整个 power 读取失败被 skip（velvet_paws 即此症状）。
        SerializableDataType<Holder<GameEvent>> gameEventHolder = SerializableDataType.holder(BuiltInRegistries.GAME_EVENT);
        return new PowerFactory<>(Apoli.identifier("prevent_game_event"),
            new SerializableData()
                .add("event", gameEventHolder, null)
                .add("events", SerializableDataType.list(gameEventHolder), null)
                .add("tag", SerializableDataTypes.GAME_EVENT_TAG, null)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null),
            data ->
                (type, player) -> {
                    List<Holder<GameEvent>> eventList = data.isPresent("events") ? data.get("events") : null;
                    if(eventList == null) {
                        eventList = new LinkedList<>();
                    }

                    if(data.isPresent("event")) {
                        eventList.add(data.get("event"));
                    }
                    return new PreventGameEventPower(type, player,
                        data.get("tag"), HolderSet.direct(eventList),
                        data.get("entity_action"));
                })
            .allowCondition();
    }
}
