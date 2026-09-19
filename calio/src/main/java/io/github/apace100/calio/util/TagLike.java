package io.github.apace100.calio.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.*;

public class TagLike<T> {
    public static <T> Codec<TagLike<T>> codec(Registry<T> registry) {
        Codec<Entry<T>> either = Codec.either(TagKey.hashedCodec(registry.key()), ResourceLocation.CODEC)
            .xmap(
                e ->
                    e.map(TagEntry::new, IdEntry::new),
                entry ->
                    entry instanceof TagEntry<T> tag ?
                        Either.left(tag.tag()) :
                    entry instanceof IdEntry<T> id ?
                        Either.right(id.id()) :
                    null
            );

        return either.listOf()
            .xmap(e -> new TagLike<>(registry, e), s -> {
                var entries = new ArrayList<Entry<T>>();
                for (TagKey<T> tag : s.tags) {
                    entries.add(new TagEntry<>(tag));
                }

                for (T item : s.items) {
                    entries.add(new IdEntry<>(s.registry.getKey(item)));
                }

                return entries;
            });
    }

    private interface Entry<T> {}
    private record TagEntry<T>(TagKey<T> tag) implements Entry<T> {}
    private record IdEntry<T>(ResourceLocation id) implements Entry<T> {}

    private final Registry<T> registry;
    private final List<TagKey<T>> tags = new LinkedList<>();
    private final Set<T> items = new HashSet<>();

    private TagLike(Registry<T> registry, List<Entry<T>> entries) {
        this(registry);

        for (Entry<T> entry : entries) {
            if (entry instanceof TagLike.TagEntry<T> tagEntry) {
                this.addTag(tagEntry.tag());
            } else if (entry instanceof TagLike.IdEntry<T> idEntry) {
                this.add(idEntry.id());
            }
        }
    }

    public TagLike(Registry<T> registry) {
        this.registry = registry;
    }

    public void addTag(ResourceLocation id) {
        addTag(TagKey.create(registry.key(), id));
    }

    public void add(ResourceLocation id) {
        add(registry.get(id));
    }

    public void addTag(TagKey<T> tagKey) {
        tags.add(tagKey);
    }

    public void add(T t) {
        items.add(t);
    }

    public boolean contains(T t) {
        if(items.contains(t)) {
            return true;
        }
        Holder<T> entry = registry.wrapAsHolder(t);
        for(TagKey<T> tagKey : tags) {
            if(entry.is(tagKey)) {
                return true;
            }
        }
        return false;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(tags.size());
        for(TagKey<T> tagKey : tags) {
            buf.writeUtf(tagKey.location().toString());
        }
        buf.writeVarInt(items.size());
        for(T t : items) {
            buf.writeUtf(registry.getKey(t).toString());
        }
    }

    public void read(FriendlyByteBuf buf) {
        tags.clear();
        int count = buf.readVarInt();
        for(int i = 0; i < count; i++) {
            tags.add(TagKey.create(registry.key(), ResourceLocation.parse(buf.readUtf())));
        }
        items.clear();
        count = buf.readVarInt();
        for(int i = 0; i < count; i++) {
            T t = registry.get(ResourceLocation.parse(buf.readUtf()));
            items.add(t);
        }
    }
}
