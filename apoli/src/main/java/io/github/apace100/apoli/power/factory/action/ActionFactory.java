package io.github.apace100.apoli.power.factory.action;

import com.google.gson.JsonObject;
import io.github.apace100.apoli.power.factory.Factory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ActionFactory<T> implements Factory {

    private final ResourceLocation identifier;
    protected SerializableData data;
    private final BiConsumer<SerializableData.Instance, T> effect;

    public ActionFactory(ResourceLocation identifier, SerializableData data, BiConsumer<SerializableData.Instance, T> effect) {
        this.identifier = identifier;
        this.effect = effect;
        this.data = data;
        this.data.add("inverted", SerializableDataTypes.BOOLEAN, false);
    }

    public class Instance implements Consumer<T> {

        private final SerializableData.Instance dataInstance;

        private Instance(SerializableData.Instance data) {
            this.dataInstance = data;
        }

        public void write(RegistryFriendlyByteBuf buf) {
            buf.writeResourceLocation(identifier);
            data.write(buf, dataInstance);
        }

        @Override
        public void accept(T t) {
            effect.accept(dataInstance, t);
        }
    }

    @Override
    public ResourceLocation getSerializerId() {
        return identifier;
    }


    @Override
    public SerializableData getSerializableData() {
        return data;
    }

    public Instance read(JsonObject json, HolderLookup.Provider provider) {
        return new Instance(data.read(json, provider));
    }

    public Instance read(RegistryFriendlyByteBuf buffer) {
        return new Instance(data.read(buffer));
    }
}
