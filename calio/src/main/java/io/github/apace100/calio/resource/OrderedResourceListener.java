package io.github.apace100.calio.resource;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;
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
public class OrderedResourceListener implements ModInitializer {

    public static final String ENTRYPOINT_KEY = "calio:ordered-resource-listener";

    @Override
    public void onInitialize() {
        OrderedResourceListenerManager manager = new OrderedResourceListenerManager();
        FabricLoader.getInstance().getEntrypoints(ENTRYPOINT_KEY, OrderedResourceListenerInitializer.class).forEach(
            entrypoint -> {
                entrypoint.registerResourceListeners(manager);
            }
        );
        manager.finishRegistration();
    }

    public static class Registration {

        private final OrderedResourceListenerManager.Instance manager;
        final ResourceLocation id;
        final IdentifiableResourceReloadListener resourceReloadListener;
        final Function<HolderLookup.Provider, IdentifiableResourceReloadListener> reloadListenerProvider;
        final Set<ResourceLocation> dependencies = new HashSet<>();
        final Set<ResourceLocation> dependants = new HashSet<>();
        private boolean isCompleted;

        Registration(OrderedResourceListenerManager.Instance manager, IdentifiableResourceReloadListener listener) {
            this.id = listener.getFabricId();
            this.manager = manager;
            this.resourceReloadListener = listener;
            this.reloadListenerProvider = null;
        }

        Registration(ResourceLocation id, OrderedResourceListenerManager.Instance manager, Function<HolderLookup.Provider, IdentifiableResourceReloadListener> listenerProvider) {
            this.id = id;
            this.manager = manager;
            this.resourceReloadListener = null;
            this.reloadListenerProvider = listenerProvider;
        }

        public Registration after(String identifier) {
            return after(ResourceLocation.parse(identifier));
        }

        public Registration after(ResourceLocation identifier) {
            if(isCompleted) {
                throw new IllegalStateException(
                    "Can't add a resource reload listener registration dependency after it was completed.");
            }
            dependencies.add(identifier);
            return this;
        }

        public Registration before(String identifier) {
            return before(ResourceLocation.parse(identifier));
        }

        public Registration before(ResourceLocation identifier) {
            if(isCompleted) {
                throw new IllegalStateException(
                    "Can't add a resource reload listener registration dependant after it was completed.");
            }
            dependants.add(identifier);
            return this;
        }

        public void complete() {
            isCompleted = true;
            manager.add(this);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(id.toString());
            builder.append("{depends_on=[");
            boolean first = true;
            for (ResourceLocation afterId : dependencies) {
                builder.append(afterId);
                if(!first) {
                    builder.append(',');
                }
                first = false;
            }
            builder.append("]}");
            return builder.toString();
        }
    }
}
