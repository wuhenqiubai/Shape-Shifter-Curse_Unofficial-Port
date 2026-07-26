package net.onixary.shapeShifterCurseFabric.mixin.accessor;

import io.github.apace100.apoli.ApoliClient;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.HashMap;

@Mixin(ApoliClient.class)
public interface ApoliClientAccessor {
    @Accessor(value = "idToKeyBindingMap", remap = false)
    static HashMap<String, KeyMapping> get_idToKeyBindingMap() {
        throw new AssertionError();
    }
}