package io.github.apace100.apoli.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.util.StackPowerUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record StackPowerComponent(List<StackPowerUtil.StackPower> powers) {
    public static final Codec<StackPowerComponent> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            StackPowerUtil.StackPower.CODEC.listOf()
                .fieldOf("powers")
                .forGetter(StackPowerComponent::powers)
        )
            .apply(instance, StackPowerComponent::new)
    );

    public static final StreamCodec<FriendlyByteBuf, StackPowerComponent> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.<FriendlyByteBuf, StackPowerUtil.StackPower>list().apply(StackPowerUtil.StackPower.STREAM_CODEC), StackPowerComponent::powers,
        StackPowerComponent::new
    );

    public static final DataComponentType<StackPowerComponent> TYPE = DataComponentType.<StackPowerComponent>builder()
        .persistent(CODEC)
        .networkSynchronized(STREAM_CODEC)
        .build();
}
