package io.github.apace100.apoli.power;

import io.github.apace100.apoli.integration.PowerClearCallback;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class PowerTypeRegistry {

    private static final HashMap<ResourceLocation, PowerType<?>> idToPower = new HashMap<>();
    private static final Set<ResourceLocation> disabledPowers = new HashSet<>();

    public static Map<ResourceLocation, PowerType<?>> get() {
        return idToPower;
    }

    public static PowerType register(ResourceLocation id, PowerType powerType) {
        if(idToPower.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate power type id tried to register: '" + id.toString() + "'");
        }
        disabledPowers.remove(id);
        idToPower.put(id, powerType);
        return powerType;
    }

    protected static PowerType update(ResourceLocation id, PowerType powerType) {
        if(idToPower.containsKey(id)) {
            PowerType old = idToPower.get(id);
            idToPower.remove(id);
        }
        return register(id, powerType);
    }

    protected static void disable(ResourceLocation id) {
        remove(id);
        disabledPowers.add(id);
    }

    protected static void remove(ResourceLocation id) {
        idToPower.remove(id);
    }

    public static boolean isDisabled(ResourceLocation id) {
        return disabledPowers.contains(id);
    }

    public static int size() {
        return idToPower.size();
    }

    public static Stream<ResourceLocation> identifiers() {
        return idToPower.keySet().stream();
    }

    public static Iterable<Map.Entry<ResourceLocation, PowerType<?>>> entries() {
        return idToPower.entrySet();
    }

    public static Iterable<PowerType<?>> values() {
        return idToPower.values();
    }

    public static PowerType get(ResourceLocation id) {
        if(!idToPower.containsKey(id)) {
            throw new IllegalArgumentException("Could not get power type from id '" + id.toString() + "', as it was not registered!");
        }
        PowerType powerType = idToPower.get(id);
        return powerType;
    }

    public static ResourceLocation getId(PowerType<?> powerType) {
        return powerType.getIdentifier();
    }

    public static boolean contains(ResourceLocation id) {
        return idToPower.containsKey(id);
    }

    public static void clear() {
        PowerClearCallback.EVENT.invoker().onPowerClear();
        idToPower.clear();
    }
    
    public static void clearDisabledPowers() {
        disabledPowers.clear();
    }

    public static void reset() {
        clear();
        clearDisabledPowers();
    }
}
