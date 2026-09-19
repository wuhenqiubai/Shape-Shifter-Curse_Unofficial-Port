package io.github.apace100.apoli.component;

import com.google.common.collect.ImmutableList;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.util.GainedPowerCriterion;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PowerHolderComponentImpl implements PowerHolderComponent {

    private final LivingEntity owner;
    private final ConcurrentHashMap<PowerType<?>, Power> powers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<PowerType<?>, List<ResourceLocation>> powerSources = new ConcurrentHashMap<>();

    public PowerHolderComponentImpl(LivingEntity owner) {
        this.owner = owner;
    }

    @Override
    public boolean hasPower(PowerType<?> powerType) {
        return powers.containsKey(powerType);
    }

    @Override
    public boolean hasPower(PowerType<?> powerType, ResourceLocation source) {
        return powerSources.containsKey(powerType) && powerSources.get(powerType).contains(source);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Power> T getPower(PowerType<T> powerType) {
        if(powers.containsKey(powerType)) {
            return (T)powers.get(powerType);
        }
        return null;
    }

    @Override
    public List<Power> getPowers() {
        return new LinkedList<>(powers.values());
    }

    public Set<PowerType<?>> getPowerTypes(boolean getSubPowerTypes) {
        HashSet<PowerType<?>> powerTypes = new HashSet<>(powers.keySet());
        for (PowerType<?> type : powers.keySet()) {
            if(!getSubPowerTypes && type instanceof MultiplePowerType<?>) {
                ((MultiplePowerType<?>)type).getSubPowers().stream().map(PowerTypeRegistry::get).forEach(powerTypes::remove);
            }
        }
        return powerTypes;
    }

    @Override
    public <T extends Power> List<T> getPowers(Class<T> powerClass) {
        return getPowers(powerClass, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Power> List<T> getPowers(Class<T> powerClass, boolean includeInactive) {
        List<T> list = new LinkedList<>();
        for(Power power : powers.values()) {
            if(powerClass.isAssignableFrom(power.getClass()) && (includeInactive || power.isActive())) {
                list.add((T)power);
            }
        }
        return list;
    }

    @Override
    public List<ResourceLocation> getSources(PowerType<?> powerType) {
        if(powerSources.containsKey(powerType)) {
            return List.copyOf(powerSources.get(powerType));
        } else {
            return List.of();
        }
    }

    public void removePower(PowerType<?> powerType, ResourceLocation source) {
        if(powerType instanceof PowerTypeReference<?>) {
            powerType = ((PowerTypeReference<?>)powerType).getReferencedPowerType();
        }
        if(powerSources.containsKey(powerType)) {
            List<ResourceLocation> sources = powerSources.get(powerType);
            sources.remove(source);
            if(sources.isEmpty()) {
                powerSources.remove(powerType);
                Power power = powers.remove(powerType);
                if(power != null) {
                    power.onRemoved();
                    power.onLost();
                }
            }
            if(powerType instanceof MultiplePowerType) {
                ImmutableList<ResourceLocation> subPowers = ((MultiplePowerType<?>)powerType).getSubPowers();
                for(ResourceLocation subPowerId : subPowers) {
                    removePower(PowerTypeRegistry.get(subPowerId), source);
                }
            }
        }
    }

    @Override
    public int removeAllPowersFromSource(ResourceLocation source) {
        List<PowerType<?>> powersToRemove = getPowersFromSource(source);
        powersToRemove.forEach(p -> removePower(p, source));
        return powersToRemove.size();
    }

    @Override
    public List<PowerType<?>> getPowersFromSource(ResourceLocation source) {
        List<PowerType<?>> powers = new LinkedList<>();
        for(Map.Entry<PowerType<?>, List<ResourceLocation>> sourceEntry : powerSources.entrySet()) {
            if(sourceEntry.getValue().contains(source)) {
                powers.add(sourceEntry.getKey());
            }
        }
        return powers;
    }

    public boolean addPower(PowerType<?> powerType, ResourceLocation source) {
        if(powerType instanceof PowerTypeReference<?>) {
            powerType = ((PowerTypeReference<?>)powerType).getReferencedPowerType();
        }
        if(powerSources.containsKey(powerType)) {
            List<ResourceLocation> sources = powerSources.get(powerType);
            if(sources.contains(source)) {
                return false;
            } else {
                sources.add(source);
                if(powerType instanceof MultiplePowerType) {
                    ImmutableList<ResourceLocation> subPowers = ((MultiplePowerType<?>)powerType).getSubPowers();
                    for(ResourceLocation subPowerId : subPowers) {
                        addPower(PowerTypeRegistry.get(subPowerId), source);
                    }
                }
                return true;
            }
        } else {
            List<ResourceLocation> sources = new LinkedList<>();
            sources.add(source);
            if(powerType instanceof MultiplePowerType) {
                ImmutableList<ResourceLocation> subPowers = ((MultiplePowerType<?>)powerType).getSubPowers();
                for(ResourceLocation subPowerId : subPowers) {
                    addPower(PowerTypeRegistry.get(subPowerId), source);
                }
            }
            powerSources.put(powerType, sources);
            Power power = powerType.create(owner);
            this.powers.put(powerType, power);
            power.onGained();
            power.onAdded();
            if(owner instanceof ServerPlayer spe) {
                GainedPowerCriterion.INSTANCE.trigger(spe, powerType);
            }
            return true;
        }
    }

    @Override
    public void serverTick() {
        this.getPowers(Power.class, true).stream().filter(p -> p.shouldTick() && (p.shouldTickWhenInactive() || p.isActive())).forEach(Power::tick);
    }

    @Override
    public void readFromNbt(CompoundTag compoundTag, HolderLookup.Provider provider) {
        this.fromTag(compoundTag, true, provider);
    }

    private void fromTag(CompoundTag compoundTag, boolean callPowerOnAdd, HolderLookup.Provider provider) {
        try {
            if (owner == null) {
                Apoli.LOGGER.error("Owner was null in PowerHolderComponent#fromTag!");
            }
            if (callPowerOnAdd) {
                for (Power power : powers.values()) {
                    power.onRemoved();
                    power.onLost();
                }
            }
            powers.clear();
            ListTag powerList = (ListTag) compoundTag.get("Powers");
            if(powerList != null) {
                for (int i = 0; i < powerList.size(); i++) {
                    CompoundTag powerTag = powerList.getCompound(i);
                    ResourceLocation powerTypeId = ResourceLocation.tryParse(powerTag.getString("Type"));
                    if(callPowerOnAdd && PowerTypeRegistry.isDisabled(powerTypeId)) {
                        continue;
                    }
                    ListTag sources = (ListTag) powerTag.get("Sources");
                    List<ResourceLocation> list = new LinkedList<>();
                    if(sources != null) {
                        sources.forEach(nbtElement -> list.add(ResourceLocation.tryParse(nbtElement.getAsString())));
                    }
                    PowerType<?> type = PowerTypeRegistry.get(powerTypeId);
                    powerSources.put(type, list);
                    try {
                        Tag data = powerTag.get("Data");
                        Power power = type.create(owner);
                        try {
                            power.fromTag(data, provider);
                        } catch (ClassCastException e) {
                            // Occurs when power was overriden by data pack since last world load
                            // to be a power type which uses different data class.
                            Apoli.LOGGER.warn("Data type of \"" + powerTypeId + "\" changed, skipping data for that power on entity " + owner.getName().getString());
                        }
                        this.powers.put(type, power);
                        if (callPowerOnAdd) {
                            power.onAdded();
                        }
                    } catch (IllegalArgumentException e) {
                        Apoli.LOGGER.warn("Power data of unregistered power \"" + powerTypeId + "\" found on entity, skipping...");
                    }
                }

                for(Map.Entry<PowerType<?>, List<ResourceLocation>> entry : powerSources.entrySet()) {
                    PowerType<?> powerType = entry.getKey();
                    if(powerType instanceof MultiplePowerType) {
                        ImmutableList<ResourceLocation> subPowers = ((MultiplePowerType<?>)powerType).getSubPowers();
                        for(ResourceLocation subPowerId : subPowers) {
                            try {
                                PowerType<?> subType = PowerTypeRegistry.get(subPowerId);
                                for(ResourceLocation source : entry.getValue()) {
                                    if(!hasPower(subType, source)) {
                                        addPower(subType, source);
                                    }
                                }
                            } catch (IllegalArgumentException e) {
                                if(callPowerOnAdd && PowerTypeRegistry.isDisabled(subPowerId)) {
                                    continue;
                                }
                                Apoli.LOGGER.warn("Multiple power type read from data contained unregistered sub-type: \"" + subPowerId + "\".");
                            }
                        }
                    }
                }
            }
        } catch(Exception e) {
            Apoli.LOGGER.error("Error while reading power holder data: " + e.getMessage());
        }
    }

    @Override
    public void writeToNbt(CompoundTag compoundTag, HolderLookup.Provider provider) {
        ListTag powerList = new ListTag();
        for(Map.Entry<PowerType<?>, Power> powerEntry : powers.entrySet()) {
            CompoundTag powerTag = new CompoundTag();
            powerTag.putString("Type", PowerTypeRegistry.getId(powerEntry.getKey()).toString());
            powerTag.put("Data", powerEntry.getValue().toTag(provider));
            ListTag sources = new ListTag();
            powerSources.get(powerEntry.getKey()).forEach(id -> sources.add(StringTag.valueOf(id.toString())));
            powerTag.put("Sources", sources);
            powerList.add(powerTag);
        }
        compoundTag.put("Powers", powerList);
    }

    @Override
    public void applySyncPacket(RegistryFriendlyByteBuf buf) {
        CompoundTag compoundTag = buf.readNbt();
        if(compoundTag != null) {
            this.fromTag(compoundTag, false, buf.registryAccess());
        }
    }

    @Override
    public void sync() {
        PowerHolderComponent.sync(this.owner);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("PowerHolderComponent[\n");
        for (Map.Entry<PowerType<?>, Power> powerEntry : powers.entrySet()) {
            str.append("\t").append(PowerTypeRegistry.getId(powerEntry.getKey())).append(": ").append(powerEntry.getValue().toTag(this.owner.level().registryAccess()).toString()).append("\n");
        }
        str.append("]");
        return str.toString();
    }
}
