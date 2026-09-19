package io.github.apace100.apoli.networking;

import com.google.common.collect.ImmutableList;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.MultiplePowerType;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record PowerListPacket(
    Map<ResourceLocation, PowerType<?>> factories
) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, PowerListPacket> CODEC = StreamCodec.composite(
        StreamCodec.of((buf, packet) -> {
            buf.writeVarInt(packet.size());
            packet.forEach((key, type) -> {
                var factory = type.getFactory();

                if (factory != null) {
                    buf.writeResourceLocation(key);
                    factory.write(buf);

                    if (type instanceof MultiplePowerType<?> multiplePowerType) {
                        buf.writeBoolean(true);
                        ImmutableList<ResourceLocation> subPowers = multiplePowerType.getSubPowers();
                        buf.writeVarInt(subPowers.size());
                        subPowers.forEach(buf::writeResourceLocation);
                    } else {
                        buf.writeBoolean(false);
                    }

                    buf.writeUtf(type.getOrCreateNameTranslationKey());
                    buf.writeUtf(type.getOrCreateDescriptionTranslationKey());
                    buf.writeBoolean(type.isHidden());
                }
            });
        }, (buf) -> {
            var powerCount = buf.readVarInt();
            var factories = new HashMap<ResourceLocation, PowerType<?>>();

            for (int i = 0; i < powerCount; i++) {
                ResourceLocation powerId = buf.readResourceLocation();
                ResourceLocation factoryId = buf.readResourceLocation();
                try {
                    PowerFactory<?> factory = ApoliRegistries.POWER_FACTORY.get(factoryId);
                    PowerFactory<?>.Instance factoryInstance = factory.read(buf);
                    PowerType<?> type;
                    if (buf.readBoolean()) {
                        type = new MultiplePowerType<>(powerId, factoryInstance);
                        int subPowerCount = buf.readVarInt();
                        List<ResourceLocation> subPowers = new ArrayList<>(subPowerCount);
                        for(int j = 0; j < subPowerCount; j++) {
                            subPowers.add(buf.readResourceLocation());
                        }
                        ((MultiplePowerType<?>) type).setSubPowers(subPowers);
                    } else {
                        type = new PowerType<>(powerId, factoryInstance);
                    }
                    type.setTranslationKeys(buf.readUtf(), buf.readUtf());
                    if (buf.readBoolean()) {
                        type.setHidden();
                    }

                    factories.put(powerId, type);
                } catch(Exception e) {
                    Apoli.LOGGER.error("Error while receiving \"" + powerId + "\" (factory: \"" + factoryId + "\"): " + e.getMessage());
                }
            }

            return factories;
        }), PowerListPacket::factories,
        PowerListPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ModPackets.POWER_LIST;
    }
}
