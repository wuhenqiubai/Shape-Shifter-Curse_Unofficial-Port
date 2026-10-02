package io.github.apace100.calio.data;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.google.gson.internal.LazilyParsedNumber;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;
import io.github.apace100.calio.ClassUtil;
import io.github.apace100.calio.SerializationHelper;
import io.github.apace100.calio.util.*;
import io.github.apace100.calio.util.extensions.LegacyParticleOptionFactory;
import net.minecraft.IdentifierException;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;

import java.util.*;

@SuppressWarnings("unused")
public final class SerializableDataTypes {

    public static final SerializableDataType<Integer> INT = new SerializableDataType<>(
        Integer.class,
        FriendlyByteBuf::writeInt,
        FriendlyByteBuf::readInt,
        JsonElement::getAsInt);

    public static final SerializableDataType<List<Integer>> INTS = SerializableDataType.list(INT);

    public static final SerializableDataType<Boolean> BOOLEAN = new SerializableDataType<>(
        Boolean.class,
        FriendlyByteBuf::writeBoolean,
        FriendlyByteBuf::readBoolean,
        JsonElement::getAsBoolean);

    public static final SerializableDataType<Float> FLOAT = new SerializableDataType<>(
        Float.class,
        FriendlyByteBuf::writeFloat,
        FriendlyByteBuf::readFloat,
        JsonElement::getAsFloat);

    public static final SerializableDataType<List<Float>> FLOATS = SerializableDataType.list(FLOAT);

    public static final SerializableDataType<Double> DOUBLE = new SerializableDataType<>(
        Double.class,
        FriendlyByteBuf::writeDouble,
        FriendlyByteBuf::readDouble,
        JsonElement::getAsDouble);

    public static final SerializableDataType<List<Double>> DOUBLES = SerializableDataType.list(DOUBLE);

    public static final SerializableDataType<String> STRING = new SerializableDataType<>(
        String.class,
        FriendlyByteBuf::writeUtf,
        (buf) -> buf.readUtf(32767),
        JsonElement::getAsString);

    public static final SerializableDataType<List<String>> STRINGS = SerializableDataType.list(STRING);

    public static final SerializableDataType<Number> NUMBER = new SerializableDataType<>(
        Number.class,
        (buf, number) -> {
            if(number instanceof Double) {
                buf.writeByte(0);
                buf.writeDouble(number.doubleValue());
            } else if(number instanceof Float) {
                buf.writeByte(1);
                buf.writeFloat(number.floatValue());
            } else if(number instanceof Integer) {
                buf.writeByte(2);
                buf.writeInt(number.intValue());
            } else if(number instanceof Long) {
                buf.writeByte(3);
                buf.writeLong(number.longValue());
            } else {
                buf.writeByte(4);
                buf.writeUtf(number.toString());
            }
        },
        buf -> {
            byte type = buf.readByte();
            switch(type) {
                case 0:
                    return buf.readDouble();
                case 1:
                    return buf.readFloat();
                case 2:
                    return buf.readInt();
                case 3:
                    return buf.readLong();
                case 4:
                    return new LazilyParsedNumber(buf.readUtf());
            }
            throw new RuntimeException("Could not receive number, unexpected type id \"" + type + "\" (allowed range: [0-4])");
        },
        je -> {
            if(je.isJsonPrimitive()) {
                JsonPrimitive primitive = je.getAsJsonPrimitive();
                if(primitive.isNumber()) {
                    return primitive.getAsNumber();
                } else if(primitive.isBoolean()) {
                    return primitive.getAsBoolean() ? 1 : 0;
                }
            }
            throw new JsonParseException("Expected a primitive");
        });

    public static final SerializableDataType<List<Number>> NUMBERS = SerializableDataType.list(NUMBER);

    public static final SerializableDataType<Vec3> VECTOR = new SerializableDataType<>(Vec3.class,
        (packetByteBuf, vector3d) -> {
            packetByteBuf.writeDouble(vector3d.x);
            packetByteBuf.writeDouble(vector3d.y);
            packetByteBuf.writeDouble(vector3d.z);
        },
        (packetByteBuf -> new Vec3(
            packetByteBuf.readDouble(),
            packetByteBuf.readDouble(),
            packetByteBuf.readDouble())),
        (jsonElement -> {
            if(jsonElement.isJsonObject()) {
                JsonObject jo = jsonElement.getAsJsonObject();
                return new Vec3(
                    GsonHelper.getAsDouble(jo, "x", 0),
                    GsonHelper.getAsDouble(jo, "y", 0),
                    GsonHelper.getAsDouble(jo, "z", 0)
                );
            } else {
                throw new JsonParseException("Expected an object with x, y, and z fields.");
            }
        }));

    public static final SerializableDataType<Identifier> IDENTIFIER = new SerializableDataType<>(
        Identifier.class,
        FriendlyByteBuf::writeIdentifier,
        FriendlyByteBuf::readIdentifier,
        (json) -> {
            String idString = json.getAsString();
            if(idString.contains(":")) {
                String[] idSplit = idString.split(":");
                if(idSplit.length != 2) {
                    throw new IdentifierException("Incorrect number of `:` in identifier: \"" + idString + "\".");
                }
                if(idSplit[0].contains("*")) {
                    if(SerializableData.CURRENT_NAMESPACE != null) {
                        idSplit[0] = idSplit[0].replace("*", SerializableData.CURRENT_NAMESPACE);
                    } else {
                        throw new IdentifierException("Identifier may not contain a `*` in the namespace when read here.");
                    }
                }
                if(idSplit[1].contains("*")) {
                    if(SerializableData.CURRENT_PATH != null) {
                        idSplit[1] = idSplit[1].replace("*", SerializableData.CURRENT_PATH);
                    } else {
                        throw new IdentifierException("Identifier may only contain a `*` in the path inside of powers.");
                    }
                }
                idString = idSplit[0] + ":" + idSplit[1];
            } else {
                if(idString.contains("*")) {
                    if(SerializableData.CURRENT_PATH != null) {
                        idString = idString.replace("*", SerializableData.CURRENT_PATH);
                    } else {
                        throw new IdentifierException("Identifier may only contain a `*` in the path inside of powers.");
                    }
                }
            }
            return convertNameToLocation(idString);
        });

    public static final SerializableDataType<List<Identifier>> IDENTIFIERS = SerializableDataType.list(IDENTIFIER);

    public static final SerializableDataType<ResourceKey<Enchantment>> ENCHANTMENT = SerializableDataType.registryKey(Registries.ENCHANTMENT);

    public static SerializableDataType<ResourceKey<Level>> DIMENSION = SerializableDataType.registryKey(Registries.DIMENSION);


    private static final List<String> ATTRIBUTE_PREFIXES = List.of("generic.", "horse.", "player.", "zombie.");
    public static final SerializableDataType<Holder<Attribute>> ATTRIBUTE = SerializableDataType.registryHolderWithRemap(BuiltInRegistries.ATTRIBUTE, id -> {
        if (id.getNamespace().equals("reach-entity-attributes") && id.getPath().equals("reach")) {
            return Attributes.BLOCK_INTERACTION_RANGE; // TODO O-L: merge reach
        }

        if (id.getNamespace().equals("reach-entity-attributes") && id.getPath().equals("attack_range")) {
            return Attributes.ENTITY_INTERACTION_RANGE;
        }

        if (ATTRIBUTE_PREFIXES.stream().anyMatch(e -> id.getPath().startsWith(e))) {
            var prefix = ATTRIBUTE_PREFIXES.stream().filter(e -> id.getPath().startsWith(e)).findFirst().orElseThrow();
            return BuiltInRegistries.ATTRIBUTE.get(Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath().substring(prefix.length()))).orElseThrow();
        }

        return null;
    });

    public static final SerializableDataType<AttributeModifier.Operation> MODIFIER_OPERATION = SerializableDataType.enumValue(AttributeModifier.Operation.class, new HashMap<>(Map.of(
        "addition", AttributeModifier.Operation.ADD_VALUE,
        "ADDITION", AttributeModifier.Operation.ADD_VALUE,
        "multiply_base", AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
        "MULTIPLY_BASE", AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
        "multiply_total", AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
        "MULTIPLY_TOTAL", AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    )));

    public static final SerializableDataType<AttributeModifier> ATTRIBUTE_MODIFIER = SerializableDataType.compound(AttributeModifier.class, new SerializableData()
            .add("name", STRING, "calio:unnamed")
            .add("operation", MODIFIER_OPERATION)
            .add("value", DOUBLE),
        data -> new AttributeModifier(
            SerializableDataTypes.convertNameToLocation(data.getString("name")),
            data.getDouble("value"),
            data.get("operation")
        ),
        (serializableData, modifier) -> {
            SerializableData.Instance inst = serializableData.new Instance();
            inst.set("name", modifier.id().toString());
            inst.set("value", modifier.amount());
            inst.set("operation", modifier.operation());
            return inst;
        });

    public static final SerializableDataType<List<AttributeModifier>> ATTRIBUTE_MODIFIERS =
        SerializableDataType.list(ATTRIBUTE_MODIFIER);

    public static final SerializableDataType<Item> ITEM = SerializableDataType.registry(Item.class, BuiltInRegistries.ITEM);

    public static final SerializableDataType<MobEffect> STATUS_EFFECT = SerializableDataType.registry(MobEffect.class, BuiltInRegistries.MOB_EFFECT);

    public static final SerializableDataType<List<MobEffect>> STATUS_EFFECTS =
        SerializableDataType.list(STATUS_EFFECT);

    public static final SerializableDataType<MobEffectInstance> STATUS_EFFECT_INSTANCE = new SerializableDataType<>(
        MobEffectInstance.class,
        SerializationHelper::writeStatusEffect,
        SerializationHelper::readStatusEffect,
        SerializationHelper::readStatusEffect);

    public static final SerializableDataType<List<MobEffectInstance>> STATUS_EFFECT_INSTANCES =
        SerializableDataType.list(STATUS_EFFECT_INSTANCE);

    public static final SerializableDataType<TagKey<Item>> ITEM_TAG = SerializableDataType.tag(Registries.ITEM);

    public static final SerializableDataType<TagKey<Fluid>> FLUID_TAG = SerializableDataType.tag(Registries.FLUID);

    public static final SerializableDataType<TagKey<Block>> BLOCK_TAG = SerializableDataType.tag(Registries.BLOCK);

    public static final SerializableDataType<TagKey<EntityType<?>>> ENTITY_TAG = SerializableDataType.tag(Registries.ENTITY_TYPE);

    public static final SerializableDataType<IngredientValue> INGREDIENT_ENTRY = SerializableDataType.compound(ClassUtil.castClass(IngredientValue.class),
        new SerializableData()
            .add("item", ITEM, null)
            .add("tag", ITEM_TAG, null),
        dataInstance -> {
            boolean tagPresent = dataInstance.isPresent("tag");
            boolean itemPresent = dataInstance.isPresent("item");
            if(tagPresent == itemPresent) {
                throw new JsonParseException("An ingredient entry is either a tag or an item, " + (tagPresent ? "not both" : "one has to be provided."));
            }
            if(tagPresent) {
                TagKey<Item> tag = dataInstance.get("tag");
                return new IngredientValue.TagValue(tag);
            } else {
                return new IngredientValue.ItemValue(dataInstance.get("item"));
            }
        }, (data, provider, entry) -> data.read(entry.serialize(), provider));

    public static final SerializableDataType<List<IngredientValue>> INGREDIENT_ENTRIES = SerializableDataType.list(INGREDIENT_ENTRY);

    // An alternative version of an ingredient deserializer which allows `minecraft:air`
    public static final SerializableDataType<Ingredient> INGREDIENT = new SerializableDataType<>(
        Ingredient.class,
        Ingredient.CONTENTS_STREAM_CODEC::encode,
        Ingredient.CONTENTS_STREAM_CODEC::decode,
        (jsonElement, provider) -> {
            List<IngredientValue> entryList = INGREDIENT_ENTRIES.read(jsonElement, provider);
            return Ingredient.of(new MergedHolderSet<>(entryList.stream().map(e -> e.getItems(provider)).toList()));
        });

    // The regular vanilla Minecraft ingredient.
    public static final SerializableDataType<Ingredient> VANILLA_INGREDIENT = new SerializableDataType<>(
        Ingredient.class,
        Ingredient.CONTENTS_STREAM_CODEC::encode,
        Ingredient.CONTENTS_STREAM_CODEC::decode,
        (jsonElement, provider) -> Ingredient.CODEC.decode(provider.createSerializationContext(JsonOps.INSTANCE), jsonElement).getOrThrow().getFirst());

    public static final SerializableDataType<Block> BLOCK = SerializableDataType.registry(Block.class, BuiltInRegistries.BLOCK);

    public static final SerializableDataType<BlockState> BLOCK_STATE = SerializableDataType.wrap(BlockState.class, STRING,
        BlockStateParser::serialize,
        string -> {
            try {
                return BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, string, false).blockState();
            } catch (CommandSyntaxException e) {
                throw new JsonParseException(e);
            }
        });

    public static final SerializableDataType<ResourceKey<DamageType>> DAMAGE_TYPE = SerializableDataType.registryKey(Registries.DAMAGE_TYPE);

    public static final SerializableDataType<List<TagKey<EntityType<?>>>> ENTITY_GROUP =
        SerializableDataType.mapped((Class<List<TagKey<EntityType<?>>>>) (Object) List.class, HashBiMap.create(ImmutableMap.of(
            "default", List.of(CalioTags.DEFAULT_ENTITY_TYPE),
            "undead", List.of(EntityTypeTags.UNDEAD, EntityTypeTags.SENSITIVE_TO_SMITE),
            "arthropod", List.of(EntityTypeTags.ARTHROPOD, EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS),
            "illager", List.of(EntityTypeTags.ILLAGER),
            "aquatic", List.of(EntityTypeTags.AQUATIC, EntityTypeTags.SENSITIVE_TO_IMPALING)
        )));

    public static final SerializableDataType<EquipmentSlot> EQUIPMENT_SLOT = SerializableDataType.enumValue(EquipmentSlot.class);

    public static final SerializableDataType<SoundEvent> SOUND_EVENT = SerializableDataType.registry(SoundEvent.class, BuiltInRegistries.SOUND_EVENT);

    public static final SerializableDataType<EntityType<?>> ENTITY_TYPE = SerializableDataType.registry(ClassUtil.castClass(EntityType.class), BuiltInRegistries.ENTITY_TYPE);

    public static final SerializableDataType<ParticleType<?>> PARTICLE_TYPE = SerializableDataType.registry(ClassUtil.castClass(ParticleType.class), BuiltInRegistries.PARTICLE_TYPE);

    public static final SerializableDataType<ParticleOptions> PARTICLE_EFFECT = new SerializableDataType<>(ParticleOptions.class,
        ParticleTypes.STREAM_CODEC::encode,
        ParticleTypes.STREAM_CODEC::decode,
        (json, provider) -> {
            var jsonObject = json.getAsJsonObject();
            ParticleType<? extends ParticleOptions> particleType = PARTICLE_TYPE.read(jsonObject.get("type"), provider);
            var codec = particleType.codec();
            ParticleOptions effect = null;
            try {
                if (particleType instanceof LegacyParticleOptionFactory factory)
                    effect = factory.calio$createFromParams(jsonObject.get("params").getAsString());
                else
                    effect = particleType.codec().codec().decode(provider.createSerializationContext(JsonOps.INSTANCE), jsonObject.get("params")).getOrThrow().getFirst();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return effect;
        }
    );

    public static final SerializableDataType<ParticleOptions> PARTICLE_EFFECT_OR_TYPE = new SerializableDataType<>(ParticleOptions.class,
        PARTICLE_EFFECT::send,
        PARTICLE_EFFECT::receive,
        (jsonElement, provider) -> {
            if(jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isString()) {
                ParticleType<?> type = PARTICLE_TYPE.read(jsonElement, provider);
                if(type instanceof ParticleOptions) {
                    return (ParticleOptions) type;
                }
                throw new RuntimeException("Expected either a string with a parameter-less particle effect, or an object.");
            } else if(jsonElement.isJsonObject()) {
                return PARTICLE_EFFECT.read(jsonElement, provider);
            }
            throw new RuntimeException("Expected either a string with a parameter-less particle effect, or an object.");
        });

    public static final SerializableDataType<CompoundTag> NBT = new SerializableDataType<>(
        CompoundTag.class,
        (buf, tag) -> buf.writeNbt(tag),
        (buf) -> buf.readNbt(),
        jsonElement -> {

            if (!(jsonElement.isJsonObject()|| jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isString()))
                throw new JsonSyntaxException("Expected either a string or an object.");

            try {
                String stringifiedJsonElement = jsonElement.isJsonObject() ? jsonElement.getAsJsonObject().toString() : jsonElement.getAsJsonPrimitive().getAsString();
                return TagParser.parseCompoundFully(stringifiedJsonElement);
            }
            catch (CommandSyntaxException e) {
                throw new JsonSyntaxException("Could not parse NBT: " + e.getMessage());
            }

        }
    );

    public static final SerializableDataType<LazyItemStack> ITEM_STACK = new SerializableDataType<>(LazyItemStack.class,
        LazyItemStack.STREAM_CODEC::encode,
        LazyItemStack.STREAM_CODEC::decode,
        (data, provider) ->  {
            if (data.isJsonObject()) {
                var json = data.getAsJsonObject();
                if (json.has("item") || json.has("tag")) {
                    // Convert legacy item stack to modern item stack
                    data = UpgradeUtils.upgradeStack(json);
                }
            }

            return LazyItemStack.CODEC.decode(provider.createSerializationContext(JsonOps.INSTANCE), data).getOrThrow().getFirst();
        }
    );

    public static final SerializableDataType<List<LazyItemStack>> ITEM_STACKS = SerializableDataType.list(ITEM_STACK);

    public static final SerializableDataType<ItemStackTemplate> ITEM_STACK_TEMPLATE = new SerializableDataType<>(ItemStackTemplate.class,
        ItemStackTemplate.STREAM_CODEC::encode,
        ItemStackTemplate.STREAM_CODEC::decode,
        (data, provider) ->  {
            if (data.isJsonObject()) {
                var json = data.getAsJsonObject();
                if (json.has("item") || json.has("tag")) {
                    // Convert legacy item stack to modern item stack
                    data = UpgradeUtils.upgradeStack(json);
                }
            }

            return ItemStackTemplate.CODEC.decode(provider.createSerializationContext(JsonOps.INSTANCE), data).getOrThrow().getFirst();
        }
    );

    public static final SerializableDataType<Component> TEXT = new SerializableDataType<>(Component.class,
        ComponentSerialization.STREAM_CODEC::encode,
        ComponentSerialization.STREAM_CODEC::decode,
        (text, provider) -> ComponentSerialization.CODEC.decode(provider.createSerializationContext(JsonOps.INSTANCE), text).getOrThrow().getFirst());

    public static final SerializableDataType<List<Component>> TEXTS = SerializableDataType.list(TEXT);

    public static final SerializableDataType<Recipe> RECIPE = new SerializableDataType<>(Recipe.class,
        Recipe.STREAM_CODEC::encode,
        Recipe.STREAM_CODEC::decode,
        (jsonElement, provider) -> {
            if(!jsonElement.isJsonObject()) {
                throw new RuntimeException("Expected recipe to be a JSON object.");
            }
            JsonObject json = UpgradeUtils.upgradeRecipe(jsonElement.getAsJsonObject());
            Identifier recipeSerializerId = Identifier.tryParse(GsonHelper.getAsString(json, "type"));
            Identifier recipeId = Identifier.tryParse(GsonHelper.getAsString(json, "id"));
            RecipeSerializer<?> serializer = BuiltInRegistries.RECIPE_SERIALIZER.getValue(recipeSerializerId);
            return serializer.codec().codec().decode(provider.createSerializationContext(JsonOps.INSTANCE), json).getOrThrow().getFirst();
        });

    public static final SerializableDataType<Holder<GameEvent>> GAME_EVENT = SerializableDataType.registryHolder(BuiltInRegistries.GAME_EVENT);

    public static final SerializableDataType<List<Holder<GameEvent>>> GAME_EVENTS =
        SerializableDataType.list(GAME_EVENT);

    public static final SerializableDataType<TagKey<GameEvent>> GAME_EVENT_TAG = SerializableDataType.tag(Registries.GAME_EVENT);

    public static final SerializableDataType<Fluid> FLUID = SerializableDataType.registry(Fluid.class, BuiltInRegistries.FLUID);

    public static final SerializableDataType<FogType> CAMERA_SUBMERSION_TYPE = SerializableDataType.enumValue(FogType.class);

    public static final SerializableDataType<InteractionHand> HAND = SerializableDataType.enumValue(InteractionHand.class);

    public static final SerializableDataType<EnumSet<InteractionHand>> HAND_SET = SerializableDataType.enumSet(InteractionHand.class, HAND);

    public static final SerializableDataType<EnumSet<EquipmentSlot>> EQUIPMENT_SLOT_SET = SerializableDataType.enumSet(EquipmentSlot.class, EQUIPMENT_SLOT);

    public static final SerializableDataType<InteractionResult> ACTION_RESULT = SerializableDataType.mapped(InteractionResult.class, HashBiMap.create(Map.of(
        "success", InteractionResult.SUCCESS,
        "success_server", InteractionResult.SUCCESS_SERVER,
        "consume", InteractionResult.CONSUME,
        //"consume_partial", InteractionResult.CONSUME,
        "pass", InteractionResult.PASS,
        "fail", InteractionResult.FAIL
    )));

    public static final SerializableDataType<ItemUseAnimation> USE_ACTION = SerializableDataType.enumValue(ItemUseAnimation.class);

    public static final SerializableDataType<StatusEffectChance> STATUS_EFFECT_CHANCE =
        SerializableDataType.compound(StatusEffectChance.class, new SerializableData()
            .add("effect", STATUS_EFFECT_INSTANCE)
            .add("chance", FLOAT, 1.0F),
            (data) -> {
                StatusEffectChance sec = new StatusEffectChance();
                sec.statusEffectInstance = data.get("effect");
                sec.chance = data.getFloat("chance");
                return sec;
            },
            (data, csei) -> {
                SerializableData.Instance inst = data.new Instance();
                inst.set("effect", csei.statusEffectInstance);
                inst.set("chance", csei.chance);
                return inst;
            });

    public static final SerializableDataType<List<StatusEffectChance>> STATUS_EFFECT_CHANCES = SerializableDataType.list(STATUS_EFFECT_CHANCE);

    public static final SerializableDataType<DataComponentMap> FOOD_COMPONENT = SerializableDataType.compound(DataComponentMap.class, new SerializableData()
            .add("hunger", INT)
            .add("saturation", FLOAT)
            .add("meat", BOOLEAN, false)
            .add("always_edible", BOOLEAN, false)
            .add("snack", BOOLEAN, false)
            .add("consume_seconds", FLOAT, Consumable.DEFAULT_CONSUME_SECONDS)
            .add("effect", STATUS_EFFECT_CHANCE, null)
            .add("effects", STATUS_EFFECT_CHANCES, null),
        (data) -> {
            var patch = DataComponentMap.builder();
            var consumeSeconds = Consumable.DEFAULT_CONSUME_SECONDS;
            var effects = new ArrayList<ConsumeEffect>();

            FoodProperties.Builder builder = new FoodProperties.Builder().nutrition(data.getInt("hunger")).saturationModifier(data.getFloat("saturation"));
            if (data.getBoolean("meat")) {
                //builder.meat(); // TODO: how to replicate?
            }
            if (data.getBoolean("always_edible")) {
                builder.alwaysEdible();
            }
            if (data.isPresent("consume_seconds")) {
                consumeSeconds = data.getFloat("consume_seconds");
            }
            if (data.getBoolean("snack")) {
                consumeSeconds = Consumable.DEFAULT_CONSUME_SECONDS / 2f;
            }
            data.<StatusEffectChance>ifPresent("effect", sec -> {
                effects.add(new ApplyStatusEffectsConsumeEffect(sec.statusEffectInstance, sec.chance));
            });
            data.<List<StatusEffectChance>>ifPresent("effects", secs -> secs.forEach(sec -> {
                effects.add(new ApplyStatusEffectsConsumeEffect(sec.statusEffectInstance, sec.chance));
            }));
            patch.set(DataComponents.FOOD, builder.build());
            patch.set(DataComponents.CONSUMABLE, new Consumable(consumeSeconds, ItemUseAnimation.EAT, SoundEvents.GENERIC_EAT, true, effects));

            return patch.build();
        },
        (data, patch) -> {
            var fc = patch.get(DataComponents.FOOD);
            var consumable = patch.get(DataComponents.CONSUMABLE);
            SerializableData.Instance inst = data.new Instance();
            inst.set("hunger", fc.nutrition());
            inst.set("saturation", fc.saturation());
            //inst.set("meat", fc.isMeat());
            inst.set("always_edible", fc.canAlwaysEat());
            inst.set("consume_seconds", fc);
            inst.set("effect", null);
            List<StatusEffectChance> statusEffectChances = new LinkedList<>();
            consumable.onConsumeEffects().forEach(pair -> {
                StatusEffectChance sec = new StatusEffectChance();
                sec.statusEffectInstance = ((ApplyStatusEffectsConsumeEffect) pair).effects().get(0);
                sec.chance = ((ApplyStatusEffectsConsumeEffect) pair).probability();
                statusEffectChances.add(sec);
            });
            if(!statusEffectChances.isEmpty()) {
                inst.set("effects", statusEffectChances);
            } else {
                inst.set("effects", null);
            }
            return inst;
        });

    public static final SerializableDataType<Direction> DIRECTION = SerializableDataType.enumValue(Direction.class);

    public static final SerializableDataType<EnumSet<Direction>> DIRECTION_SET = SerializableDataType.enumSet(Direction.class, DIRECTION);

    public static final SerializableDataType<Class<?>> CLASS = SerializableDataType.wrap(ClassUtil.castClass(Class.class), SerializableDataTypes.STRING,
        Class::getName,
        str -> {
            try {
                return Class.forName(str);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Specified class does not exist: \"" + str + "\".");
            }
        });

    public static final SerializableDataType<ClipContext.Block> SHAPE_TYPE = SerializableDataType.enumValue(ClipContext.Block.class);

    public static final SerializableDataType<ClipContext.Fluid> FLUID_HANDLING = SerializableDataType.enumValue(ClipContext.Fluid.class);

    public static final SerializableDataType<Explosion.BlockInteraction> DESTRUCTION_TYPE = SerializableDataType.enumValue(Explosion.BlockInteraction.class);

    public static final SerializableDataType<Direction.Axis> AXIS = SerializableDataType.enumValue(Direction.Axis.class);

    public static final SerializableDataType<EnumSet<Direction.Axis>> AXIS_SET = SerializableDataType.enumSet(Direction.Axis.class, AXIS);

    public static final SerializableDataType<ArgumentWrapper<NbtPathArgument.NbtPath>> NBT_PATH =
        SerializableDataType.argumentType(NbtPathArgument.nbtPath());

    public static final SerializableDataType<ClipContext.Block> RAYCAST_SHAPE_TYPE = SerializableDataType.enumValue(ClipContext.Block.class);

    public static final SerializableDataType<ClipContext.Fluid> RAYCAST_FLUID_HANDLING = SerializableDataType.enumValue(ClipContext.Fluid.class);

    public static final SerializableDataType<Stat<?>> STAT = SerializableDataType.compound(ClassUtil.castClass(Stat.class),
        new SerializableData()
            .add("type", SerializableDataType.registry(ClassUtil.castClass(StatType.class), BuiltInRegistries.STAT_TYPE))
            .add("id", SerializableDataTypes.IDENTIFIER),
        data -> {
            StatType statType = data.get("type");
            Registry<?> statRegistry = statType.getRegistry();
            Identifier statId = data.get("id");
            if(statRegistry.containsKey(statId)) {
                Object statObject = statRegistry.get(statId);
                return statType.get(statObject);
            }
            throw new IllegalArgumentException("Desired stat \"" + statId + "\" does not exist in stat type ");
        },
        (data, stat) -> {
            SerializableData.Instance inst = data.new Instance();
            inst.set("type", stat.getType());
            Registry reg = stat.getType().getRegistry();
            Identifier statId = reg.getKey(stat.getValue());
            inst.set("id", statId);
            return inst;
        });

    public static final SerializableDataType<TagKey<Biome>> BIOME_TAG = SerializableDataType.tag(Registries.BIOME);

    public static final SerializableDataType<TagLike<Item>> ITEM_TAG_LIKE = SerializableDataType.tagLike(BuiltInRegistries.ITEM);

    public static final SerializableDataType<TagLike<Block>> BLOCK_TAG_LIKE = SerializableDataType.tagLike(BuiltInRegistries.BLOCK);

    public static final SerializableDataType<TagLike<EntityType<?>>> ENTITY_TYPE_TAG_LIKE = SerializableDataType.tagLike(BuiltInRegistries.ENTITY_TYPE);

    public static final SerializableDataType<DataComponentPatch> DATA_COMPONENTS = new SerializableDataType<>(DataComponentPatch.class,
        DataComponentPatch.STREAM_CODEC::encode,
        DataComponentPatch.STREAM_CODEC::decode,
        (element, provider) -> DataComponentPatch.CODEC.decode(provider.createSerializationContext(JsonOps.INSTANCE), element).getOrThrow().getFirst()
    );

    public static Identifier convertNameToLocation(String name) {
        if (!name.contains(" "))
            return Identifier.parse(name.toLowerCase());

        return Identifier.fromNamespaceAndPath("calio", name.toLowerCase().replace(" ", "_"));
    }
}
