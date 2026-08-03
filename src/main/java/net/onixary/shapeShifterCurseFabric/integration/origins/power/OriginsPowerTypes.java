package net.onixary.shapeShifterCurseFabric.integration.origins.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeReference;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.NamespaceAlias;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
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
		// 1.21.11 (Apoli-Legacy 2.12.10)：origins:* 类型的 power/condition/action 别名解析到 apoli:*
		// （2.12.10 没有 PowerFactories.ALIASES，改用 NamespaceAlias；origins:toggle → apoli:toggle 等）
		NamespaceAlias.addAlias("origins", "apoli");

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
		// Replacement for the removed apoli:entity_group power type (SSC 1.21.1 迁移时丢失，这里恢复)
		register(new PowerFactory<>(Apoli.identifier("modify_type_tag"),
				new SerializableData()
						.add("tag", SerializableDataTypes.ENTITY_TAG),
				data ->
						(type, entity) -> {
							TagKey<EntityType<?>> tag = data.get("tag");
							return new ModifyTypeTagPower(type, entity, tag);
						}).allowCondition());
	}

    private static void register(PowerFactory<?> serializer) {
        Registry.register(ApoliRegistries.POWER_FACTORY, serializer.getSerializerId(), serializer);
    }
}
