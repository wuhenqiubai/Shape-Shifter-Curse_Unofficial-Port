package io.github.apace100.calio.util;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Allows registering data resource listeners in a specified order, to prevent problems
 * due to mod loading order and inter-mod data dependencies.
 */
@Deprecated
public final class OrderedResourceListeners {

    private static final Set<ResourceLocation> finalizedRegistrations = new HashSet<>();
    private static final HashMap<ResourceLocation, Registration> registrations = new HashMap<>();

    public static Registration register(IdentifiableResourceReloadListener resourceReloadListener) {
        Registration registration = new Registration(resourceReloadListener);
        return registration;
    }

    private static void completeRegistration(Registration registration) {
        registration.afterSet.removeAll(finalizedRegistrations);
        if(registration.afterSet.size() == 0) {
            finalizeRegistration(registration);
        } else {
            registrations.put(registration.resourceReloadListener.getFabricId(), registration);
        }
    }

    private static void finalizeRegistration(Registration registration) {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(registration.resourceReloadListener);
        ResourceLocation id = registration.resourceReloadListener.getFabricId();
        finalizedRegistrations.add(id);
        registrations.remove(id);
        Set<ResourceLocation> finishedOnes = new HashSet<>();
        for(Map.Entry<ResourceLocation, Registration> registrationEntry : registrations.entrySet()) {
            registrationEntry.getValue().afterSet.remove(id);
            if(registrationEntry.getValue().afterSet.size() == 0) {
                finishedOnes.add(registrationEntry.getKey());
            }
        }
        for(ResourceLocation finished : finishedOnes) {
            finalizeRegistration(registrations.get(finished));
        }
    }

    public static class Registration {

        private final IdentifiableResourceReloadListener resourceReloadListener;
        private final Set<ResourceLocation> afterSet = new HashSet<>();
        private final Set<ResourceLocation> beforeSet = new HashSet<>();
        private boolean isCompleted;

        private Registration(IdentifiableResourceReloadListener resourceReloadListener) {
            this.resourceReloadListener = resourceReloadListener;
        }

        public Registration after(ResourceLocation identifier) {
            if(isCompleted) {
                throw new IllegalStateException(
                    "Can't add a resource reload listener registration dependency after it was completed.");
            }
            afterSet.add(identifier);
            return this;
        }

        public Registration before(ResourceLocation identifier) {
            if(isCompleted) {
                throw new IllegalStateException(
                    "Can't add a resource reload listener registration dependency after it was completed.");
            }
            beforeSet.add(identifier);
            return this;
        }

        public void complete() {
            completeRegistration(this);
            isCompleted = true;
        }
    }
}
