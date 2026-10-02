package io.github.apace100.calio.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public class LazyItemStack implements ItemInstance, Supplier<ItemStack> {
    public static final MapCodec<LazyItemStack> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("id")
                .forGetter(LazyItemStack::typeHolder),
            Codec.INT.optionalFieldOf("count", 1)
                .forGetter(LazyItemStack::count),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                .forGetter(LazyItemStack::components)
        )
            .apply(instance, LazyItemStack::new)
    );

    public static final Codec<LazyItemStack> CODEC = MAP_CODEC.codec();

    public static final StreamCodec<RegistryFriendlyByteBuf, LazyItemStack> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.holderRegistry(Registries.ITEM), LazyItemStack::typeHolder,
        ByteBufCodecs.VAR_INT, LazyItemStack::count,
        DataComponentPatch.STREAM_CODEC, LazyItemStack::components,
        LazyItemStack::new
    );

    private final Holder<Item> typeHolder;
    private final int count;
    private final DataComponentPatch components;
    private final Supplier<ItemStack> lazy = new Lazy<>(this::createStack);

    public LazyItemStack(Item item) {
        this(item.builtInRegistryHolder(), 1);
    }

    public LazyItemStack(Holder<Item> typeHolder, int count, DataComponentPatch components) {
        this.typeHolder = typeHolder;
        this.count = count;
        this.components = components;
    }

    public LazyItemStack(Holder<Item> typeHolder, int count) {
        this(typeHolder, count, DataComponentPatch.EMPTY);
    }

    @Override
    public Holder<Item> typeHolder() {
        return this.typeHolder;
    }

    @Override
    public int count() {
        return this.count;
    }

    public DataComponentPatch components() {
        return this.components;
    }

    public static LazyItemStack fromItem(ItemStack instance) {
        return new LazyItemStack(instance.typeHolder(), instance.count(), instance.getComponentsPatch());
    }

    public static LazyItemStack fromTemplate(ItemStackTemplate template) {
        return new LazyItemStack(template.typeHolder(), template.count(), template.components());
    }

    public static ItemStack createStackFromData(SerializableData.Instance data, String name) {
        var value = data.get(name);

        return switch (value) {
            case ItemStack stack -> stack;
            case LazyItemStack lazyStack -> lazyStack.getStack();
            case ItemStackTemplate template -> template.create();
            case null, default -> throw new IllegalStateException("Invalid item instance type provided!");
        };
    }

    @Override
    public @Nullable <T> T get(DataComponentType<? extends T> type) {
        if (!this.typeHolder().areComponentsBound()) {
            return null;
        }

        return this.components().get(this.typeHolder().components(), type);
    }

    public ItemStack getStack() {
        if (!this.typeHolder().areComponentsBound())
            return ItemStack.EMPTY;

        return this.lazy.get();
    }

    @Override
    public ItemStack get() {
        return this.getStack();
    }

    public @Nullable ItemStack createStack() {
        if (!this.typeHolder().areComponentsBound())
            return null;

        return new ItemStack(this.typeHolder(), this.count(), this.components());
    }

    public ItemStackTemplate buildTemplate() {
        return new ItemStackTemplate(this.typeHolder(), this.count(), this.components());
    }
}
