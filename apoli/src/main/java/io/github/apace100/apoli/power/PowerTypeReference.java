package io.github.apace100.apoli.power;

import com.mojang.serialization.Codec;
import io.github.apace100.apoli.power.factory.PowerFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class PowerTypeReference<T extends Power> extends PowerType<T> {
    public static final Codec<PowerType<?>> CODEC = ResourceLocation.CODEC.xmap(PowerTypeReference::new, PowerType::getIdentifier);

    private PowerType<T> referencedPowerType;

    public PowerTypeReference(ResourceLocation id) {
        super(id, null);
    }

    @Override
    public PowerFactory<T>.Instance getFactory() {
        getReferencedPowerType();
        if(referencedPowerType == null) {
            return null;
        }
        return referencedPowerType.getFactory();
    }

    @Override
    public boolean isActive(Entity entity) {
        getReferencedPowerType();
        if(referencedPowerType == null) {
            return false;
        }
        return referencedPowerType.isActive(entity);
    }

    @Override
    public T get(Entity entity) {
        getReferencedPowerType();
        if(referencedPowerType == null) {
            return null;
        }
        return referencedPowerType.get(entity);
    }

    public PowerType<T> getReferencedPowerType() {
        if(isReferenceInvalid()) {
            try {
                referencedPowerType = null;
                referencedPowerType = PowerTypeRegistry.get(getIdentifier());
            } catch(IllegalArgumentException e) {
                //cooldown = 600;
                //Apoli.LOGGER.warn("Invalid PowerTypeReference: no power type exists with id \"" + getIdentifier() + "\"");
            }
        }
        return referencedPowerType;
    }

    private boolean isReferenceInvalid() {
        if(referencedPowerType != null) {
            if(PowerTypeRegistry.contains(referencedPowerType.getIdentifier())) {
                PowerType<T> type = PowerTypeRegistry.get(referencedPowerType.getIdentifier());
                return type != referencedPowerType;
            }
        }
        return true;
    }
}
