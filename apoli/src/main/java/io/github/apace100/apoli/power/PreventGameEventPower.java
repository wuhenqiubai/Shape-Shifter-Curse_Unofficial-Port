package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.Holder;
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
    // 1.21.1 起 GameEvent 以 Holder<GameEvent> 表示（ServerWorldMixin.gameEvent 签名即 Holder<GameEvent>），
    // 故这里存 Holder<GameEvent>，与 doesPrevent(GameEvent) 传递的 event.value() 对齐。
    private final List<Holder<GameEvent>> list;
    private final Consumer<Entity> entityAction;

    public PreventGameEventPower(PowerType<?> type, LivingEntity entity, TagKey<GameEvent> tag, List<Holder<GameEvent>> list, Consumer<Entity> entityAction) {
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

    public boolean doesPrevent(GameEvent event) {
        if(tag != null && BuiltInRegistries.GAME_EVENT.wrapAsHolder(event).is(tag)) {
            return true;
        }
        if(list != null && list.stream().anyMatch(holder -> holder.value() == event)) {
            return true;
        }
        return false;
    }

    public static PowerFactory createFactory() {
        // 1.21.1: 用 calio 的 holder 数据类型读 event/events（返回 Holder<GameEvent>），
        // .add 存 Holder，data.get("event"/"events") 不再需要 (List<GameEvent>) 强转，
        // 从而避免"GameEvent cannot be cast to Holder"的 ClassCast。
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
                    if(data.isPresent("event")) {
                        if(eventList == null) {
                            eventList = new LinkedList<>();
                        }
                        eventList.add(data.get("event"));
                    }
                    return new PreventGameEventPower(type, player,
                        data.get("tag"), eventList,
                        data.get("entity_action"));
                })
            .allowCondition();
    }
}
