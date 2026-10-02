package io.github.apace100.apoli.legacy;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class OverlayableDataComponentMap implements DataComponentMap {
    private DataComponentMap original;
    private final Set<DataComponentPatch> overlays = new HashSet<>();
    private final Set<DataComponentPatch> replacements = new HashSet<>();

    public OverlayableDataComponentMap(DataComponentMap original) {
        this.original = original;
    }

    public boolean matchesOriginal(DataComponentMap map) {
        return this.original == map;
    }

    public void setOriginal(DataComponentMap map) {
        this.original = map;
    }

    public void addOverlay(DataComponentPatch patch, boolean replaceExisting) {
        if (replaceExisting)
            this.replacements.add(patch);
        else
            this.overlays.add(patch);
    }

    public boolean hasOverlay(DataComponentPatch patch) {
        return this.overlays.contains(patch) || this.replacements.contains(patch);
    }

    public boolean hasOverlays() {
        return !this.overlays.isEmpty() || !this.replacements.isEmpty();
    }

    public void clearOverlays() {
        this.overlays.clear();
    }

    @Override
    public Set<DataComponentType<?>> keySet() {
        var set = this.original.keySet();

        if (!this.overlays.isEmpty()) {
            set = new HashSet<>(set);
            for (DataComponentPatch overlay : this.overlays) {
                for (Map.Entry<DataComponentType<?>, Optional<?>> entry : overlay.entrySet()) {
                    set.add(entry.getKey());
                }
            }
        }

        return set;
    }

    @Override
    public @Nullable <T> T get(DataComponentType<? extends T> type) {
        for (DataComponentPatch overlay : this.replacements) {
            var component = overlay.get(this.original, type);
            if (component != null)
                return component;
        }

        if (!this.original.has(type)) {
            for (DataComponentPatch overlay : this.overlays) {
                var component = overlay.get(this.original, type);
                if (component != null)
                    return component;
            }
        }

        return this.original.get(type);
    }
}
