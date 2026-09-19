package io.github.apace100.apoli.power.factory;

import com.google.gson.JsonObject;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.BiFunction;
import java.util.function.Function;

public class PowerFactory<P extends Power> implements Factory {

    private final ResourceLocation id;
    private boolean hasConditions = false;
    protected SerializableData data;
    protected Function<SerializableData.Instance, BiFunction<PowerType<P>, LivingEntity, P>> factoryConstructor;

    public PowerFactory(ResourceLocation id, SerializableData data, Function<SerializableData.Instance, BiFunction<PowerType<P>, LivingEntity, P>> factoryConstructor) {
        this.id = id;
        this.data = data;
        this.factoryConstructor = factoryConstructor;
    }

    public PowerFactory<P> allowCondition() {
        if(!hasConditions) {
            hasConditions = true;
            data.add("condition", ApoliDataTypes.ENTITY_CONDITION, null);
        }
        return this;
    }

    @Override
    public ResourceLocation getSerializerId() {
        return id;
    }

    @Override
    public SerializableData getSerializableData() {
        return data;
    }

    public class Instance implements BiFunction<PowerType<P>, LivingEntity, P> {

        private final SerializableData.Instance dataInstance;

        private Instance(SerializableData.Instance data) {
            this.dataInstance = data;
        }

        public void write(RegistryFriendlyByteBuf buf) {
            buf.writeResourceLocation(id);
            data.write(buf, dataInstance);
        }

        @Override
        public P apply(PowerType<P> pPowerType, LivingEntity livingEntity) {
            BiFunction<PowerType<P>, LivingEntity, P> powerFactory = factoryConstructor.apply(dataInstance);
            P p = powerFactory.apply(pPowerType, livingEntity);
            if(hasConditions && dataInstance.isPresent("condition")) {
                p.addCondition((ConditionFactory<Entity>.Instance) dataInstance.get("condition"));
            }
            return p;
        }

        public SerializableData.Instance getDataInstance() {
            return dataInstance;
        }

        public PowerFactory<?> getFactory() {
            return PowerFactory.this;
        }
    }

    public Instance read(JsonObject json, HolderLookup.Provider provider) {
        return new Instance(data.read(json, provider));
    }

    public Instance read(RegistryFriendlyByteBuf buffer) {
        return new Instance(data.read(buffer));
    }
}
