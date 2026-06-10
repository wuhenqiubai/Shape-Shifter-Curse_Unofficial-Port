package net.onixary.shapeShifterCurseFabric.integration.origins.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeReference;
import io.github.apace100.apoli.power.factory.PowerFactories;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.action.BiEntityActions;
import io.github.apace100.apoli.power.factory.action.BlockActions;
import io.github.apace100.apoli.power.factory.action.EntityActions;
import io.github.apace100.apoli.power.factory.action.ItemActions;
import io.github.apace100.apoli.power.factory.condition.BiEntityConditions;
import io.github.apace100.apoli.power.factory.condition.BlockConditions;
import io.github.apace100.apoli.power.factory.condition.DamageConditions;
import io.github.apace100.apoli.power.factory.condition.EntityConditions;
import io.github.apace100.apoli.power.factory.condition.FluidConditions;
import io.github.apace100.apoli.power.factory.condition.ItemConditions;
import io.github.apace100.apoli.power.factory.condition.BiomeConditions;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;

import java.util.function.BiFunction;
import java.util.function.Function;

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
        // Register namespace alias so origins:* power types resolve to apoli:* equivalents
        // This uses PowerFactories.ALIASES (not the registry) so it works regardless of init order,
        // which is critical for NeoForge/Connector where Apoli may not have populated its registries yet.
        PowerFactories.ALIASES.addNamespaceAlias("origins", "apoli");
        // Also add namespace aliases for all condition and action registries, since they
        // each have their own ALIASES instance used during data loading (see ApoliDataTypes).
        EntityConditions.ALIASES.addNamespaceAlias("origins", "apoli");
        BiEntityConditions.ALIASES.addNamespaceAlias("origins", "apoli");
        ItemConditions.ALIASES.addNamespaceAlias("origins", "apoli");
        BlockConditions.ALIASES.addNamespaceAlias("origins", "apoli");
        DamageConditions.ALIASES.addNamespaceAlias("origins", "apoli");
        FluidConditions.ALIASES.addNamespaceAlias("origins", "apoli");
        BiomeConditions.ALIASES.addNamespaceAlias("origins", "apoli");
        EntityActions.ALIASES.addNamespaceAlias("origins", "apoli");
        BiEntityActions.ALIASES.addNamespaceAlias("origins", "apoli");
        BlockActions.ALIASES.addNamespaceAlias("origins", "apoli");
        ItemActions.ALIASES.addNamespaceAlias("origins", "apoli");

        // Register all apoli:* types as origins:* aliases
        // Needed because SSC JSONs use origins: namespace but Origins mod is not installed
        for (var registry : new Registry[]{
            ApoliRegistries.POWER_FACTORY,
            ApoliRegistries.ENTITY_CONDITION,
            ApoliRegistries.BIENTITY_CONDITION,
            ApoliRegistries.ITEM_CONDITION,
            ApoliRegistries.BLOCK_CONDITION,
            ApoliRegistries.DAMAGE_CONDITION,
            ApoliRegistries.FLUID_CONDITION,
            ApoliRegistries.BIOME_CONDITION,
            ApoliRegistries.ENTITY_ACTION,
            ApoliRegistries.ITEM_ACTION,
            ApoliRegistries.BLOCK_ACTION,
            ApoliRegistries.BIENTITY_ACTION,
        }) {
            try {
                // Copy values first to avoid ConcurrentModificationException
                var values = new java.util.ArrayList<>();
                registry.forEach(values::add);
                for (var value : values) {
                    Identifier apoliId = switch (value) {
                        case io.github.apace100.apoli.power.factory.PowerFactory pf -> pf.getSerializerId();
                        case io.github.apace100.apoli.power.factory.condition.ConditionFactory<?> cf -> cf.getSerializerId();
                        case io.github.apace100.apoli.power.factory.action.ActionFactory<?> af -> af.getSerializerId();
                        default -> null;
                    };
                    if (apoliId != null && "apoli".equals(apoliId.getNamespace())) {
                        Identifier originsId = Origins.identifier(apoliId.getPath());
                        if (!registry.containsId(originsId)) {
                            Registry.register(registry, originsId, value);
                        }
                    }
                }
                Origins.LOGGER.info("Aliased {} apoli->origins types in registry", values.size());
            } catch (Exception e) {
                Origins.LOGGER.error("Failed to alias registry", e);
            }
            // Debug: check if apoli:multiple was aliased
            if (!ApoliRegistries.POWER_FACTORY.containsId(Origins.identifier("multiple"))) {
                Origins.LOGGER.warn("origins:multiple not found in POWER_FACTORY after alias!");
            }
        }

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
