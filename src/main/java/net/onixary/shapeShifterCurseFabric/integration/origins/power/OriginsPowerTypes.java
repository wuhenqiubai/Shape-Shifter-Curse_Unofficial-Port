package net.onixary.shapeShifterCurseFabric.integration.origins.power;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeReference;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.NamespaceAlias;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.Registry;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;

@SuppressWarnings("unchecked")
public class OriginsPowerTypes {

    public static final PowerType<?> LIKE_WATER = new PowerTypeReference<>(Origins.identifier("like_water"));
    public static final PowerType<?> WATER_BREATHING = new PowerTypeReference<>(Origins.identifier("water_breathing"));
    public static final PowerType<?> SCARE_CREEPERS = new PowerTypeReference<>(Origins.identifier("scare_creepers"));
    public static final PowerType<?> WATER_VISION = new PowerTypeReference<>(Origins.identifier("water_vision"));
    public static final PowerType<?> NO_COBWEB_SLOWDOWN = new PowerTypeReference<>(Origins.identifier("no_cobweb_slowdown"));
    public static final PowerType<?> MASTER_OF_WEBS_NO_SLOWDOWN = new PowerTypeReference<>(Origins.identifier("master_of_webs_no_slowdown"));
    public static final PowerType<?> CONDUIT_POWER_ON_LAND = new PowerTypeReference<>(Origins.identifier("conduit_power_on_land"));

    public static void register() {
        // Register namespace alias so origins:* types resolve to apoli:* equivalents.
        // Apoli-Legacy 用全局静态 NamespaceAlias（一次注册即覆盖 power/condition/action 数据加载解析）。
        NamespaceAlias.addAlias("origins", "apoli");

        // 1.21.11 修复：移除重复注册 apoli->origins 的 copy-registry 段。
        // Apoli 2.12.10 读取 power/condition/action 类型时已用 NamespaceAlias.resolveAlias 把 origins:* 解析到 apoli:*
        // （见 PowerTypes.readPower / ActionType / ConditionType），且 1.21.11 MappedRegistry 校验 value 唯一，
        // 把同一 factory 复制到 origins:* key 会抛 "Adding duplicate value"。故仅需上方 addAlias，无需复制 value。

        register(new PowerFactory<>(Origins.identifier("action_on_callback"),
            new SerializableData()
                .add("entity_action_respawned", ApoliDataTypes.ENTITY_ACTION, null)
                .add("entity_action_removed", ApoliDataTypes.ENTITY_ACTION, null)
                .add("entity_action_gained", ApoliDataTypes.ENTITY_ACTION, null)
                .add("entity_action_lost", ApoliDataTypes.ENTITY_ACTION, null)
                .add("entity_action_added", ApoliDataTypes.ENTITY_ACTION, null)
                .add("entity_action_chosen", ApoliDataTypes.ENTITY_ACTION, null)
                .add("execute_chosen_when_orb", SerializableDataTypes.BOOLEAN, true),
            data ->
                (type, player) -> new OriginsCallbackPower(type, player,
		                data.get("entity_action_respawned"),
		                data.get("entity_action_removed"),
		                data.get("entity_action_gained"),
		                data.get("entity_action_lost"),
		                data.get("entity_action_added"),
		                data.get("entity_action_chosen"),
                    data.getBoolean("execute_chosen_when_orb")))
            .allowCondition());

	    // apoli:modify_type_tag — makes entity be considered in the specified entity type tag
	    // Replacement for the removed apoli:entity_group power type
	    // 1.21.1: 改用 Legacy 原生 apoli:entity_group（数据已切换为 aquatic/arthropod 枚举值），此注册保留但不再使用
	    // register(new PowerFactory<>(Apoli.identifier("modify_type_tag"),
	    //         new SerializableData()
	    //                 .add("tag", SerializableDataTypes.ENTITY_TAG),
	    //         data ->
	    //                 (type, entity) -> {
	    //                     TagKey<EntityType<?>> tag = data.get("tag");
	    //                     return new ModifyTypeTagPower(type, entity, tag);
	    //                 }).allowCondition());
    }

    private static void register(PowerFactory<?> serializer) {
        Registry.register(ApoliRegistries.POWER_FACTORY, serializer.getSerializerId(), serializer);
    }
}