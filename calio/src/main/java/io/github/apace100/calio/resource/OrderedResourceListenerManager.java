package io.github.apace100.calio.resource;

import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @deprecated  Deprecated in favour of using Fabric's IdentifiableResourceReloadListener.
 *              To establish an `after` dependency, simply add the other reload listener's identifier
 *              (IdentifiableResourceReloadListener#getFabricId) to the set your reload listener returns
 *              in IdentifiableResourceReloadListener#getFabricDependencies.
 *              To establish a `before` dependency, the other resource loader needs to expose the dependency
 *              set it returns in that method, e.g. by using a publicly accessible set, or exposing a public
 *              method to add to it.
 */
@Deprecated
public class OrderedResourceListenerManager {
    private static final List<Function<HolderLookup.Provider, IdentifiableResourceReloadListener>> registryBasedReloadListenerProviders = new ArrayList<>();

    public static List<Function<HolderLookup.Provider, IdentifiableResourceReloadListener>> getRegistryBasedReloadListenerProviders() {
        return registryBasedReloadListenerProviders;
    }

    private final Instance registryBasedInstance = new Instance(ResourceManagerHelper.get(PackType.SERVER_DATA)::registerReloadListener, registryBasedReloadListenerProviders::add);
    private final HashMap<PackType, Instance> instances = new HashMap<>();

    OrderedResourceListenerManager() {}

    public OrderedResourceListener.Registration register(PackType resourceType, IdentifiableResourceReloadListener resourceReloadListener) {
        Instance inst = instances.computeIfAbsent(resourceType, rt -> new Instance(ResourceManagerHelper.get(rt)::registerReloadListener));
        return new OrderedResourceListener.Registration(inst, resourceReloadListener);
    }

    public OrderedResourceListener.Registration registerWithRegistries(ResourceLocation id, Function<HolderLookup.Provider, IdentifiableResourceReloadListener> reloadListener) {
        return new OrderedResourceListener.Registration(id, registryBasedInstance, reloadListener);
    }

    void finishRegistration() {
        for(Instance inst : instances.values()) {
            inst.finish();
        }

        registryBasedInstance.finish();
    }

    static class Instance {
        private final HashMap<ResourceLocation, OrderedResourceListener.Registration> registrations = new HashMap<>();
        private final HashMap<Integer, List<ResourceLocation>> sortedMap = new HashMap<>();
        private int maxIndex = 0;

        private final Consumer<IdentifiableResourceReloadListener> registrationMethod;
        private final Consumer<Function<HolderLookup.Provider, IdentifiableResourceReloadListener>> registrationProviderMethod;

        private Instance(Consumer<IdentifiableResourceReloadListener> registrationMethod) {
            this.registrationMethod = registrationMethod;
            this.registrationProviderMethod = null;
        }

        private Instance(Consumer<IdentifiableResourceReloadListener> registrationMethod, Consumer<Function<HolderLookup.Provider, IdentifiableResourceReloadListener>> registrationProviderMethod) {
            this.registrationMethod = registrationMethod;
            this.registrationProviderMethod = registrationProviderMethod;
        }

        void add(OrderedResourceListener.Registration registration) {
            registrations.put(registration.id, registration);
        }

        void finish() {
            prepareSetsAndSort();
            List<ResourceLocation> sortedList = new LinkedList<>();
            List<ResourceLocation> nextListeners;
            while(!(nextListeners = copy(getRegistrations(0))).isEmpty()) {
                sortedList.addAll(nextListeners);
                sortedMap.remove(0);
                for(int i = 1; i <= maxIndex; i++) {
                    for(ResourceLocation regId : copy(getRegistrations(i))) {
                        OrderedResourceListener.Registration registration = registrations.get(regId);
                        int before = registration.dependencies.size();
                        nextListeners.forEach(registration.dependencies::remove);
                        update(registration, before);
                    }
                }
            }
            if(!sortedMap.isEmpty()) {
                StringBuilder errorBuilder = new StringBuilder("Couldn't resolve ordered resource listener dependencies. Unsolved:");
                for(int i = 0; i <= maxIndex; i++) {
                    if(!getRegistrations(i).isEmpty()) {
                        errorBuilder.append("\t").append(i).append(" dependencies:");
                        for(ResourceLocation id : getRegistrations(i)) {
                            OrderedResourceListener.Registration registration = registrations.get(id);
                            errorBuilder.append("\t\t").append(registration.toString());
                            if (registration.resourceReloadListener != null)
                                registrationMethod.accept(registration.resourceReloadListener);
                            else
                                registrationProviderMethod.accept(registration.reloadListenerProvider);
                        }
                    }
                }
                throw new RuntimeException(errorBuilder.toString());
            } else {
                for(ResourceLocation id : sortedList) {
                    OrderedResourceListener.Registration registration = registrations.get(id);
                    if (registration.resourceReloadListener != null)
                        registrationMethod.accept(registration.resourceReloadListener);
                    else
                        registrationProviderMethod.accept(registration.reloadListenerProvider);
                }
            }
        }

        private void prepareSetsAndSort() {
            for (OrderedResourceListener.Registration reg : registrations.values()) {
                reg.dependencies.removeIf(id -> !registrations.containsKey(id));
                reg.dependants.forEach(id -> {
                    if(registrations.containsKey(id)) {
                        registrations.get(id).dependencies.add(reg.id);
                    }
                });
            }
            registrations.values().forEach(this::sortIntoMap);
        }

        private void sortIntoMap(OrderedResourceListener.Registration registration) {
            int index = registration.dependencies.size();
            List<ResourceLocation> list = sortedMap.computeIfAbsent(index, i -> new LinkedList<>());
            list.add(registration.id);
            if(index > maxIndex) {
                maxIndex = index;
            }
        }

        private void update(OrderedResourceListener.Registration registration, int indexBefore) {
            int index = registration.dependencies.size();
            if(index == indexBefore) {
                return;
            }
            List<ResourceLocation> regs = getRegistrations(indexBefore);
            regs.remove(registration.id);
            if(regs.isEmpty()) {
                sortedMap.remove(indexBefore);
            }
            List<ResourceLocation> list = sortedMap.computeIfAbsent(index, i -> new LinkedList<>());
            list.add(registration.id);
        }

        private List<ResourceLocation> getRegistrations(int index) {
            return sortedMap.getOrDefault(index, new LinkedList<>());
        }
    }

    private static <T> List<T> copy(List<T> list) {
        return Lists.newLinkedList(list);
    }
}
