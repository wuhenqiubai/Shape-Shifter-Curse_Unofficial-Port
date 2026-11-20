package io.github.apace100.apoli.data;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.datafixers.util.Pair;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeReference;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.action.ActionType;
import io.github.apace100.apoli.power.factory.action.ActionTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionType;
import io.github.apace100.apoli.power.factory.condition.ConditionTypes;
import io.github.apace100.apoli.util.*;
import io.github.apace100.calio.ClassUtil;
import io.github.apace100.calio.SerializationHelper;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.util.ArgumentWrapper;
import io.github.apace100.calio.util.UpgradeUtils;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.PlayerAbility;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.material.FluidState;
import org.apache.commons.lang3.tuple.Triple;

import java.util.EnumSet;
import java.util.List;

public class ApoliDataTypes {

    public static final SerializableDataType<PowerTypeReference> POWER_TYPE = SerializableDataType.wrap(
        PowerTypeReference.class, SerializableDataTypes.IDENTIFIER,
        PowerType::getIdentifier, PowerTypeReference::new);

    public static final SerializableDataType<ConditionFactory<Entity>.Instance> ENTITY_CONDITION =
        condition(ClassUtil.castClass(ConditionFactory.Instance.class), ConditionTypes.ENTITY);

    public static final SerializableDataType<List<ConditionFactory<Entity>.Instance>> ENTITY_CONDITIONS =
        SerializableDataType.list(ENTITY_CONDITION);

    public static final SerializableDataType<ConditionFactory<Pair<Entity, Entity>>.Instance> BIENTITY_CONDITION =
        condition(ClassUtil.castClass(ConditionFactory.Instance.class), ConditionTypes.BIENTITY);

    public static final SerializableDataType<List<ConditionFactory<Pair<Entity, Entity>>.Instance>> BIENTITY_CONDITIONS =
        SerializableDataType.list(BIENTITY_CONDITION);

    public static final SerializableDataType<ConditionFactory<ItemStack>.Instance> ITEM_CONDITION =
        condition(ClassUtil.castClass(ConditionFactory.Instance.class), ConditionTypes.ITEM);

    public static final SerializableDataType<List<ConditionFactory<ItemStack>.Instance>> ITEM_CONDITIONS =
        SerializableDataType.list(ITEM_CONDITION);

    public static final SerializableDataType<ConditionFactory<BlockInWorld>.Instance> BLOCK_CONDITION =
        condition(ClassUtil.castClass(ConditionFactory.Instance.class), ConditionTypes.BLOCK);

    public static final SerializableDataType<List<ConditionFactory<BlockInWorld>.Instance>> BLOCK_CONDITIONS =
        SerializableDataType.list(BLOCK_CONDITION);

    public static final SerializableDataType<ConditionFactory<FluidState>.Instance> FLUID_CONDITION =
        condition(ClassUtil.castClass(ConditionFactory.Instance.class), ConditionTypes.FLUID);

    public static final SerializableDataType<List<ConditionFactory<FluidState>.Instance>> FLUID_CONDITIONS =
        SerializableDataType.list(FLUID_CONDITION);

    public static final SerializableDataType<ConditionFactory<Pair<DamageSource, Float>>.Instance> DAMAGE_CONDITION =
        condition(ClassUtil.castClass(ConditionFactory.Instance.class), ConditionTypes.DAMAGE);

    public static final SerializableDataType<List<ConditionFactory<Pair<DamageSource, Float>>.Instance>> DAMAGE_CONDITIONS =
        SerializableDataType.list(DAMAGE_CONDITION);

    public static final SerializableDataType<ConditionFactory<Holder<Biome>>.Instance> BIOME_CONDITION =
        condition(ClassUtil.castClass(ConditionFactory.Instance.class), ConditionTypes.BIOME);

    public static final SerializableDataType<List<ConditionFactory<Holder<Biome>>.Instance>> BIOME_CONDITIONS =
        SerializableDataType.list(BIOME_CONDITION);

    public static final SerializableDataType<ActionFactory<Entity>.Instance> ENTITY_ACTION =
        action(ClassUtil.castClass(ActionFactory.Instance.class), ActionTypes.ENTITY);

    public static final SerializableDataType<List<ActionFactory<Entity>.Instance>> ENTITY_ACTIONS =
        SerializableDataType.list(ENTITY_ACTION);

    public static final SerializableDataType<ActionFactory<Pair<Entity, Entity>>.Instance> BIENTITY_ACTION =
        action(ClassUtil.castClass(ActionFactory.Instance.class), ActionTypes.BIENTITY);

    public static final SerializableDataType<List<ActionFactory<Pair<Entity, Entity>>.Instance>> BIENTITY_ACTIONS =
        SerializableDataType.list(BIENTITY_ACTION);

    public static final SerializableDataType<ActionFactory<Triple<Level, BlockPos, Direction>>.Instance> BLOCK_ACTION =
        action(ClassUtil.castClass(ActionFactory.Instance.class), ActionTypes.BLOCK);

    public static final SerializableDataType<List<ActionFactory<Triple<Level, BlockPos, Direction>>.Instance>> BLOCK_ACTIONS =
        SerializableDataType.list(BLOCK_ACTION);

    public static final SerializableDataType<ActionFactory<Pair<Level, ItemStack>>.Instance> ITEM_ACTION =
        action(ClassUtil.castClass(ActionFactory.Instance.class), ActionTypes.ITEM);

    public static final SerializableDataType<List<ActionFactory<Pair<Level, ItemStack>>.Instance>> ITEM_ACTIONS =
        SerializableDataType.list(ITEM_ACTION);

    public static final SerializableDataType<Space> SPACE = SerializableDataType.enumValue(Space.class);

    public static final SerializableDataType<ResourceOperation> RESOURCE_OPERATION = SerializableDataType.enumValue(ResourceOperation.class);

    public static final SerializableDataType<InventoryUtil.InventoryType> INVENTORY_TYPE = SerializableDataType.enumValue(InventoryUtil.InventoryType.class);

    public static final SerializableDataType<EnumSet<InventoryUtil.InventoryType>> INVENTORY_TYPE_SET = SerializableDataType.enumSet(InventoryUtil.InventoryType.class, INVENTORY_TYPE);

    public static final SerializableDataType<InventoryUtil.ProcessMode> PROCESS_MODE = SerializableDataType.enumValue(InventoryUtil.ProcessMode.class);

    public static final SerializableDataType<AttributedEntityAttributeModifier> ATTRIBUTED_ATTRIBUTE_MODIFIER = SerializableDataType.compound(
        AttributedEntityAttributeModifier.class,
        new SerializableData()
            .add("attribute", SerializableDataTypes.ATTRIBUTE)
            .add("operation", SerializableDataTypes.MODIFIER_OPERATION)
            .add("value", SerializableDataTypes.DOUBLE)
            .add("name", SerializableDataTypes.STRING, "apoli:unnamed"),
        dataInst -> new AttributedEntityAttributeModifier(dataInst.get("attribute"),
            new AttributeModifier(
                SerializableDataTypes.convertNameToLocation(dataInst.getString("name")),
                dataInst.getDouble("value"),
                dataInst.get("operation"))),
        (data, inst) -> {
            SerializableData.Instance dataInst = data.new Instance();
            dataInst.set("attribute", inst.getAttribute());
            dataInst.set("operation", inst.getModifier().operation());
            dataInst.set("value", inst.getModifier().amount());
            dataInst.set("name", inst.getModifier().id().toString());
            return dataInst;
        });

    public static final SerializableDataType<List<AttributedEntityAttributeModifier>> ATTRIBUTED_ATTRIBUTE_MODIFIERS =
        SerializableDataType.list(ATTRIBUTED_ATTRIBUTE_MODIFIER);

    public static final SerializableDataType<Pair<Integer, ItemStack>> POSITIONED_ITEM_STACK = SerializableDataType.compound(ClassUtil.castClass(Pair.class),
        new SerializableData()
            .add("item", SerializableDataTypes.ITEM)
            .add("amount", SerializableDataTypes.INT, 1)
            .add("tag", SerializableDataTypes.NBT, null)
            .add("components", SerializableDataTypes.DATA_COMPONENTS, DataComponentPatch.EMPTY)
            .add("slot", SerializableDataTypes.INT, Integer.MIN_VALUE),
        (data) ->  {
            ItemStack stack = new ItemStack((Item)data.get("item"), data.getInt("amount"));
            if(data.isPresent("tag")) {
                CompoundTag oldTag = data.get("tag");
                CompoundTag recreatedTag = new CompoundTag();
                recreatedTag.putString("id", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                recreatedTag.putInt("Count", stack.getCount());
                recreatedTag.put("tag", oldTag);

                var convertedStackNbt = UpgradeUtils.upgradeStack(recreatedTag);
                var convertedStack = ItemStack.CODEC.decode(NbtOps.INSTANCE, convertedStackNbt).getOrThrow().getFirst();
                stack.applyComponents(convertedStack.getComponents());
            }
            if (data.isPresent("components")) {
                stack.applyComponents((DataComponentPatch) data.get("components"));
            }
            return new Pair<>(data.getInt("slot"), stack);
        },
        ((serializableData, positionedStack) -> {
            SerializableData.Instance data = serializableData.new Instance();
            data.set("item", positionedStack.getSecond().getItem());
            data.set("amount", positionedStack.getSecond().getCount());
            //data.set("tag", positionedStack.getB().hasTag() ? positionedStack.getB().getTag() : null);
            data.set("components", !positionedStack.getSecond().getComponentsPatch().isEmpty() ? positionedStack.getSecond().getComponentsPatch() : null);
            data.set("slot", positionedStack.getFirst());
            return data;
        }));

    public static final SerializableDataType<List<Pair<Integer, ItemStack>>> POSITIONED_ITEM_STACKS = SerializableDataType.list(POSITIONED_ITEM_STACK);

    public static final SerializableDataType<Active.Key> KEY = SerializableDataType.compound(Active.Key.class,
        new SerializableData()
            .add("key", SerializableDataTypes.STRING)
            .add("continuous", SerializableDataTypes.BOOLEAN, false),
        (data) ->  {
            Active.Key key = new Active.Key();
            key.key = data.getString("key");
            key.continuous = data.getBoolean("continuous");
            return key;
        },
        ((serializableData, key) -> {
            SerializableData.Instance data = serializableData.new Instance();
            data.set("key", key.key);
            data.set("continuous", key.continuous);
            return data;
        }));

    public static final SerializableDataType<Active.Key> BACKWARDS_COMPATIBLE_KEY = new SerializableDataType<>(Active.Key.class,
        KEY::send, KEY::receive, (jsonElement, provider) -> {
        if(jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isString()) {
            String keyString = jsonElement.getAsString();
            Active.Key key = new Active.Key();
            key.key = keyString;
            key.continuous = false;
            return key;
        }
        return KEY.read(jsonElement, provider);
    });

    public static final SerializableDataType<HudRender> HUD_RENDER = SerializableDataType.compound(HudRender.class, new
            SerializableData()
            .add("should_render", SerializableDataTypes.BOOLEAN, true)
            .add("bar_index", SerializableDataTypes.INT, 0)
            .add("sprite_location", SerializableDataTypes.IDENTIFIER, Identifier.fromNamespaceAndPath("origins", "textures/gui/resource_bar.png"))
            .add("condition", ENTITY_CONDITION, null)
            .add("inverted", SerializableDataTypes.BOOLEAN, false)
            .add("order", SerializableDataTypes.INT, 0),
        (dataInst) -> new HudRender(
            dataInst.getBoolean("should_render"),
            dataInst.getInt("bar_index"),
            dataInst.getId("sprite_location"),
            dataInst.get("condition"),
            dataInst.getBoolean("inverted"),
            dataInst.getInt("order")),
        (data, inst) -> {
            SerializableData.Instance dataInst = data.new Instance();
            dataInst.set("should_render", inst.shouldRender());
            dataInst.set("bar_index", inst.getBarIndex());
            dataInst.set("sprite_location", inst.getSpriteLocation());
            dataInst.set("condition", inst.getCondition());
            dataInst.set("inverted", inst.isInverted());
            dataInst.set("order", inst.getOrder());
            return dataInst;
        });

    public static final SerializableDataType<Comparison> COMPARISON = SerializableDataType.enumValue(Comparison.class,
        SerializationHelper.buildEnumMap(Comparison.class, Comparison::getComparisonString));

    public static final SerializableDataType<PlayerAbility> PLAYER_ABILITY = SerializableDataType.wrap(
        PlayerAbility.class, SerializableDataTypes.IDENTIFIER,
        PlayerAbility::getId, id -> Pal.provideRegisteredAbility(id).get());

    public static final SerializableDataType<ArgumentWrapper<Integer>> ITEM_SLOT = SerializableDataType.argumentType(SlotArgument.slot());

    public static final SerializableDataType<List<ArgumentWrapper<Integer>>> ITEM_SLOTS = SerializableDataType.list(ITEM_SLOT);

    public static final SerializableDataType<Explosion.BlockInteraction> BACKWARDS_COMPATIBLE_DESTRUCTION_TYPE = SerializableDataType.mapped(Explosion.BlockInteraction.class,
            HashBiMap.create(ImmutableBiMap.of(
                    "none", Explosion.BlockInteraction.KEEP,
                    "break", Explosion.BlockInteraction.DESTROY,
                    "destroy", Explosion.BlockInteraction.DESTROY_WITH_DECAY)
            ));

    public static final SerializableDataType<ArgumentWrapper<EntitySelector>> ENTITIES_SELECTOR = SerializableDataType.argumentType(EntityArgument.entities());

    public static final SerializableDataType<DamageSourceDescription> DAMAGE_SOURCE_DESCRIPTION = SerializableDataType.compound(DamageSourceDescription.class,
            DamageSourceDescription.DATA, DamageSourceDescription::fromData, DamageSourceDescription::toData);

    public static final SerializableDataType<LegacyMaterial> LEGACY_MATERIAL = SerializableDataType.wrap(
            LegacyMaterial.class, SerializableDataTypes.STRING,
            LegacyMaterial::getMaterial, LegacyMaterial::new
    );

    public static final SerializableDataType<List<LegacyMaterial>> LEGACY_MATERIALS = SerializableDataType.list(LEGACY_MATERIAL);

    public static <T> SerializableDataType<ConditionFactory<T>.Instance> condition(Class<ConditionFactory<T>.Instance> dataClass, ConditionType<T> conditionType) {
        return new SerializableDataType<>(dataClass, conditionType::write, conditionType::read, (json, provider) -> conditionType.read(json, provider));
    }

    public static <T> SerializableDataType<ActionFactory<T>.Instance> action(Class<ActionFactory<T>.Instance> dataClass, ActionType<T> actionType) {
        return new SerializableDataType<>(dataClass, actionType::write, actionType::read, (json, providers) -> actionType.read(json, providers));
    }
}
