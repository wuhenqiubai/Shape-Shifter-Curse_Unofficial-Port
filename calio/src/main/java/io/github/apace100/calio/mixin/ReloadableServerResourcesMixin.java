package io.github.apace100.calio.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.calio.resource.OrderedResourceListenerManager;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesMixin {
    @Unique private final List<IdentifiableResourceReloadListener> calio$registryListeners = new ArrayList<>();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void calio$initRegistryBasedListeners(LayeredRegistryAccess fullLayers, HolderLookup.Provider loadingContext, FeatureFlagSet enabledFeatures, Commands.CommandSelection commandSelection, List postponedTags, PermissionSet functionCompilationPermissions, List newComponents, CallbackInfo ci) {
        for (Function<HolderLookup.Provider, IdentifiableResourceReloadListener> provider : OrderedResourceListenerManager.getRegistryBasedReloadListenerProviders()) {
            calio$registryListeners.add(provider.apply(loadingContext));
        }
    }

    @ModifyReturnValue(method = "listeners", at = @At("RETURN"))
    private List<PreparableReloadListener> calio$appendRegistryListeners(List<PreparableReloadListener> original) {
        var modifiable = new ArrayList<>(original);
        modifiable.addAll(this.calio$registryListeners);

        return modifiable;
    }
}
