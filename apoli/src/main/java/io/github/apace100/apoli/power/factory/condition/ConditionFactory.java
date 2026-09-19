package io.github.apace100.apoli.power.factory.condition;

import com.google.gson.JsonObject;
import io.github.apace100.apoli.power.factory.Factory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public class ConditionFactory<T> implements Factory {

    private final ResourceLocation identifier;
    protected SerializableData data;
    private final BiFunction<SerializableData.Instance, T, Boolean> condition;

    public ConditionFactory(ResourceLocation identifier, SerializableData data, BiFunction<SerializableData.Instance, T, Boolean> condition) {
        this.identifier = identifier;
        this.condition = condition;
        this.data = data;
        this.data.add("inverted", SerializableDataTypes.BOOLEAN, false);
    }

    public class Instance implements Predicate<T> {

        private final SerializableData.Instance dataInstance;

        private Instance(SerializableData.Instance data) {
            this.dataInstance = data;
        }

        public final boolean test(T t) {
            boolean fulfilled = isFulfilled(t);
            if(dataInstance.getBoolean("inverted")) {
                return !fulfilled;
            } else {
                return fulfilled;
            }
        }

        public boolean isFulfilled(T t) {
            try {
                return condition.apply(dataInstance, t);
            } catch (Throwable e) {
                throw new RuntimeException("Failed to run condition on " + identifier + "!", e);
            }
        }

        public void write(RegistryFriendlyByteBuf buf) {
            buf.writeResourceLocation(identifier);
            data.write(buf, dataInstance);
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
