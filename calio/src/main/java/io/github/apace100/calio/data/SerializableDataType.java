package io.github.apace100.calio.data;

import com.google.common.collect.BiMap;
import com.google.gson.*;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.apace100.calio.ClassUtil;
import io.github.apace100.calio.FilterableWeightedList;
import io.github.apace100.calio.mixin.WeightedListEntryAccessor;
import io.github.apace100.calio.util.ArgumentWrapper;
import io.github.apace100.calio.util.TagLike;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.function.TriFunction;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SerializableDataType<T> {

    private final Class<T> dataClass;
    private final BiConsumer<RegistryFriendlyByteBuf, T> send;
    private final Function<RegistryFriendlyByteBuf, T> receive;
    private final BiFunction<JsonElement, HolderLookup.Provider, T> read;

    public SerializableDataType(Class<T> dataClass,
                                BiConsumer<RegistryFriendlyByteBuf, T> send,
                                Function<RegistryFriendlyByteBuf, T> receive,
                                Function<JsonElement, T> read) {
        this(dataClass, send, receive, (json, provider) -> read.apply(json));
    }

    public SerializableDataType(Class<T> dataClass,
                                BiConsumer<RegistryFriendlyByteBuf, T> send,
                                Function<RegistryFriendlyByteBuf, T> receive,
                                BiFunction<JsonElement, HolderLookup.Provider, T> read) {
        this.dataClass = dataClass;
        this.send = send;
        this.receive = receive;
        this.read = read;
    }

    public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return StreamCodec.of(this::send, this::receive);
    }

    public void send(RegistryFriendlyByteBuf buffer, Object value) {
        send.accept(buffer, cast(value));
    }

    public T receive(RegistryFriendlyByteBuf buffer) {
        return receive.apply(buffer);
    }

    public T read(JsonElement jsonElement, HolderLookup.Provider provider) {
        return read.apply(jsonElement, provider);
    }

    public T cast(Object data) {
        return dataClass.cast(data);
    }

    public static <T> SerializableDataType<List<T>> list(SerializableDataType<T> singleDataType) {
        return new SerializableDataType<>(ClassUtil.castClass(List.class), (buf, list) -> {
            buf.writeInt(list.size());
            int i = 0;
            for(T elem : list) {
                try {
                    singleDataType.send(buf, elem);
                } catch(DataException e) {
                    throw e.prepend("[" + i + "]");
                } catch(Exception e) {
                    throw new DataException(DataException.Phase.WRITING, "[" + i + "]", e);
                }
                i++;
            }
        }, (buf) -> {
            int count = buf.readInt();
            LinkedList<T> list = new LinkedList<>();
            for(int i = 0; i < count; i++) {
                try {
                    list.add(singleDataType.receive(buf));
                } catch(DataException e) {
                    throw e.prepend("[" + i + "]");
                } catch(Exception e) {
                    throw new DataException(DataException.Phase.RECEIVING, "[" + i + "]", e);
                }
            }
            return list;
        }, (json, provider) -> {
            LinkedList<T> list = new LinkedList<>();
            if(json.isJsonArray()) {
                int i = 0;
                for(JsonElement je : json.getAsJsonArray()) {
                    try {
                        list.add(singleDataType.read(je, provider));
                    } catch(DataException e) {
                        throw e.prepend("[" + i + "]");
                    } catch(Exception e) {
                        throw new DataException(DataException.Phase.READING, "[" + i + "]", e);
                    }
                    i++;
                }
            } else {
                list.add(singleDataType.read(json, provider));
            }
            return list;
        });
    }

    public static <T> SerializableDataType<FilterableWeightedList<T>> weightedList(SerializableDataType<T> singleDataType) {
        return new SerializableDataType<>(ClassUtil.castClass(FilterableWeightedList.class), (buf, list) -> {
            buf.writeInt(list.size());
            AtomicInteger i = new AtomicInteger();
            list.entryStream().forEach(entry -> {
                try {
                    singleDataType.send(buf, entry.getData());
                    buf.writeInt(((WeightedListEntryAccessor) entry).getWeight());
                } catch(DataException e) {
                    throw e.prepend("[" + i.get() + "]");
                } catch(Exception e) {
                    throw new DataException(DataException.Phase.WRITING, "[" + i.get() + "]", e);
                }
                i.getAndIncrement();
            });
        }, (buf) -> {
            int count = buf.readInt();
            FilterableWeightedList<T> list = new FilterableWeightedList<>();
            for (int i = 0; i < count; i++) {
                try {
                    T t = singleDataType.receive(buf);
                    int weight = buf.readInt();
                    list.add(t, weight);
                } catch(DataException e) {
                    throw e.prepend("[" + i + "]");
                } catch(Exception e) {
                    throw new DataException(DataException.Phase.RECEIVING, "[" + i + "]", e);
                }
            }
            return list;
        }, (json, provider) -> {
            FilterableWeightedList<T> list = new FilterableWeightedList<>();
            if (json.isJsonArray()) {
                int i = 0;
                for (JsonElement je : json.getAsJsonArray()) {
                    try {
                        JsonObject weightedObj = je.getAsJsonObject();
                        T elem = singleDataType.read(weightedObj.get("element"), provider);
                        int weight = GsonHelper.getAsInt(weightedObj, "weight");
                        list.add(elem, weight);
                    } catch(DataException e) {
                        throw e.prepend("[" + i + "]");
                    } catch(Exception e) {
                        throw new DataException(DataException.Phase.READING, "[" + i + "]", e);
                    }
                    i++;
                }
            }
            return list;
        });
    }

    public static <T> SerializableDataType<T> registry(Class<T> dataClass, Registry<T> registry) {
        return wrap(dataClass, SerializableDataTypes.IDENTIFIER, registry::getKey, id -> {
            Optional<T> optional = registry.getOptional(id);
            if(optional.isPresent()) {
                return optional.get();
            } else {
                throw new RuntimeException(
                    "Identifier \"" + id + "\" was not registered in registry \"" + registry.key().location() + "\".");
            }
        });
    }

    public static <T> SerializableDataType<T> registryWithRemap(Class<T> dataClass, Registry<T> registry, Function<ResourceLocation, T> remap) {
        return wrap(dataClass, SerializableDataTypes.IDENTIFIER, registry::getKey, id -> {
            var remapped = remap.apply(id);

            if (remapped != null)
                return remapped;

            Optional<T> optional = registry.getOptional(id);
            if(optional.isPresent()) {
                return optional.get();
            } else {
                throw new RuntimeException(
                    "Identifier \"" + id + "\" was not registered in registry \"" + registry.key().location() + "\".");
            }
        });
    }

    public static <T> SerializableDataType<Holder<T>> registryHolderWithRemap(Registry<T> registry, Function<ResourceLocation, Holder<T>> remap) {
        return wrap(ClassUtil.castClass(Holder.class), SerializableDataTypes.IDENTIFIER,
            e -> e.unwrapKey().orElseThrow().location(), id -> {
            var remapped = remap.apply(id);

            if (remapped != null)
                return remapped;

            Optional<Holder.Reference<T>> optional = registry.getHolder(id);
            if(optional.isPresent()) {
                return optional.get();
            } else {
                throw new RuntimeException(
                    "Identifier \"" + id + "\" was not registered in registry \"" + registry.key().location() + "\".");
            }
        });
    }

    public static <T> SerializableDataType<T> compound(Class<T> dataClass, SerializableData data, Function<SerializableData.Instance, T> toInstance, BiFunction<SerializableData, T, SerializableData.Instance> toData) {
        return new SerializableDataType<>(dataClass,
            (buf, t) -> data.write(buf, toData.apply(data, t)),
            (buf) -> toInstance.apply(data.read(buf)),
            (json, provider) -> toInstance.apply(data.read(json.getAsJsonObject(), provider)));
    }

    public static <T> SerializableDataType<T> compound(Class<T> dataClass, SerializableData data, Function<SerializableData.Instance, T> toInstance, TriFunction<SerializableData, HolderLookup.Provider, T, SerializableData.Instance> toData) {
        return new SerializableDataType<>(dataClass,
            (buf, t) -> data.write(buf, toData.apply(data, buf.registryAccess(), t)),
            (buf) -> toInstance.apply(data.read(buf)),
            (json, provider) -> toInstance.apply(data.read(json.getAsJsonObject(), provider)));
    }

    public static <T extends Enum<T>> SerializableDataType<T> enumValue(Class<T> dataClass) {
        return enumValue(dataClass, null);
    }

    public static <T extends Enum<T>> SerializableDataType<T> enumValue(Class<T> dataClass, HashMap<String, T> additionalMap) {
        return new SerializableDataType<>(dataClass,
            (buf, t) -> buf.writeInt(t.ordinal()),
            (buf) -> dataClass.getEnumConstants()[buf.readInt()],
            (json) -> {
                if(json.isJsonPrimitive()) {
                    JsonPrimitive primitive = json.getAsJsonPrimitive();
                    if(primitive.isNumber()) {
                        int enumOrdinal = primitive.getAsInt();
                        T[] enumValues = dataClass.getEnumConstants();
                        if(enumOrdinal < 0 || enumOrdinal >= enumValues.length) {
                            throw new JsonSyntaxException("Expected to be in the range of 0 - " + (enumValues.length - 1));
                        }
                        return enumValues[enumOrdinal];
                    } else if(primitive.isString()) {
                        String enumName = primitive.getAsString();
                        try {
                            T t = Enum.valueOf(dataClass, enumName);
                            return t;
                        } catch(IllegalArgumentException e0) {
                            try {
                                T t = Enum.valueOf(dataClass, enumName.toUpperCase(Locale.ROOT));
                                return t;
                            } catch (IllegalArgumentException e1) {
                                try {
                                    if(additionalMap == null || !additionalMap.containsKey(enumName)) {
                                        throw new IllegalArgumentException();
                                    }
                                    T t = additionalMap.get(enumName);
                                    return t;
                                } catch (IllegalArgumentException e2) {
                                    T[] enumValues = dataClass.getEnumConstants();
                                    String stringOf = enumValues[0].name() + ", " + enumValues[0].name().toLowerCase(Locale.ROOT);
                                    for(int i = 1; i < enumValues.length; i++) {
                                        stringOf += ", " + enumValues[i].name() + ", " + enumValues[i].name().toLowerCase(Locale.ROOT);
                                    }
                                    throw new JsonSyntaxException("Expected value to be a string of: " + stringOf);
                                }
                            }
                        }
                    }
                }
                throw new JsonSyntaxException("Expected value to be either an integer or a string.");
            });
    }

    public static <T> SerializableDataType<T> mapped(Class<T> dataClass, BiMap<String, T> map) {
        return new SerializableDataType<>(dataClass,
            (buf, t) -> buf.writeUtf(map.inverse().get(t)),
            (buf) -> map.get(buf.readUtf(32767)),
            (json) -> {
                if(json.isJsonPrimitive()) {
                    JsonPrimitive primitive = json.getAsJsonPrimitive();
                    if(primitive.isString()) {
                        String name = primitive.getAsString();
                        try {
                            if(map == null || !map.containsKey(name)) {
                                throw new IllegalArgumentException();
                            }
                            T t = map.get(name);
                            return t;
                        } catch (IllegalArgumentException e2) {
                            throw new JsonSyntaxException("Expected value to be a string of: " + map.keySet().stream().reduce((s0, s1) -> s0 + ", " + s1));
                        }
                    }
                }
                throw new JsonSyntaxException("Expected value to be a string.");
            });
    }

    public static <T, U> SerializableDataType<T> wrap(Class<T> dataClass, SerializableDataType<U> base, Function<T, U> toFunction, Function<U, T> fromFunction) {
        return new SerializableDataType<>(dataClass,
            (buf, t) -> base.send(buf, toFunction.apply(t)),
            (buf) -> fromFunction.apply(base.receive(buf)),
            (json, provider) -> fromFunction.apply(base.read(json, provider)));
    }

    public static <T, U> SerializableDataType<T> wrap(Class<T> dataClass, SerializableDataType<U> base, BiFunction<T, HolderLookup.Provider, U> toFunction, BiFunction<U, HolderLookup.Provider, T> fromFunction) {
        return new SerializableDataType<>(dataClass,
            (buf, t) -> base.send(buf, toFunction.apply(t, buf.registryAccess())),
            (buf) -> fromFunction.apply(base.receive(buf), buf.registryAccess()),
            (json, provider) -> fromFunction.apply(base.read(json, provider), provider));
    }

    public static <T> SerializableDataType<TagKey<T>> tag(ResourceKey<? extends Registry<T>> registryKey) {
        return SerializableDataType.wrap(ClassUtil.castClass(TagKey.class), SerializableDataTypes.IDENTIFIER,
            TagKey::location,
            id -> TagKey.create(registryKey, id));
    }

    public static <T> SerializableDataType<Holder<T>> holder(Registry<T> registry) {
        return SerializableDataType.wrap(ClassUtil.castClass(Holder.class), SerializableDataTypes.IDENTIFIER,
            e -> e.unwrapKey().orElseThrow().location(),
            id -> registry.getHolder(id).orElseThrow());
    }

    public static <T> SerializableDataType<ResourceKey<T>> registryKey(ResourceKey<Registry<T>> registryKeyRegistry) {
        return SerializableDataType.wrap(
            ClassUtil.castClass(ResourceKey.class),
            SerializableDataTypes.IDENTIFIER,
            ResourceKey::location, identifier -> ResourceKey.create(registryKeyRegistry, identifier)
        );
    }

    public static <T extends Enum<T>> SerializableDataType<EnumSet<T>> enumSet(Class<T> enumClass, SerializableDataType<T> enumDataType) {
        return new SerializableDataType<>(ClassUtil.castClass(EnumSet.class),
            (buf, set) -> {
                buf.writeInt(set.size());
                set.forEach(t -> buf.writeInt(t.ordinal()));
            },
            (buf) -> {
                int size = buf.readInt();
                EnumSet<T> set = EnumSet.noneOf(enumClass);
                T[] allValues = enumClass.getEnumConstants();
                for(int i = 0; i < size; i++) {
                    int ordinal = buf.readInt();
                    set.add(allValues[ordinal]);
                }
                return set;
            },
            (json, provider) -> {
                EnumSet<T> set = EnumSet.noneOf(enumClass);
                if(json.isJsonPrimitive()) {
                    T t = enumDataType.read.apply(json, provider);
                    set.add(t);
                } else
                if(json.isJsonArray()) {
                    JsonArray array = json.getAsJsonArray();
                    for (JsonElement jsonElement : array) {
                        T t = enumDataType.read.apply(jsonElement, provider);
                        set.add(t);
                    }
                } else {
                    throw new RuntimeException("Expected enum set to be either an array or a primitive.");
                }
                return set;
            });
    }

    public static <T, U extends ArgumentType<T>> SerializableDataType<ArgumentWrapper<T>> argumentType(U argumentType) {
        return wrap(ClassUtil.castClass(ArgumentWrapper.class), SerializableDataTypes.STRING,
            ArgumentWrapper::rawArgument,
            str -> {
                try {
                    T t = argumentType.parse(new StringReader(str));
                    return new ArgumentWrapper<>(t, str);
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException("Wrong syntax in argument type data", e);
                }
            });
    }

    public static <T> SerializableDataType<TagLike<T>> tagLike(Registry<T> registry) {
        return new SerializableDataType<>(ClassUtil.castClass(TagLike.class),
                (packetByteBuf, tagLike) -> tagLike.write(packetByteBuf),
                packetByteBuf -> {
                    TagLike<T> tagLike = new TagLike<>(registry);
                    tagLike.read(packetByteBuf);
                    return tagLike;
                },
                jsonElement -> {
                    TagLike<T> tagLike = new TagLike<>(registry);
                    if (!jsonElement.isJsonArray()) {
                        throw new JsonSyntaxException("Expected a JSON array,");
                    }
                    JsonArray jsonArray = jsonElement.getAsJsonArray();
                    jsonArray.forEach(je -> {
                        String s = je.getAsString();
                        if (s.startsWith("#")) {
                            ResourceLocation id = ResourceLocation.parse(s.substring(1));
                            tagLike.addTag(id);
                        } else {
                            tagLike.add(ResourceLocation.parse(s));
                        }
                    });
                    return tagLike;
                });
    }
}
