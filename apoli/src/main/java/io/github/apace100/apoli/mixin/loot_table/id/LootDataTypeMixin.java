package io.github.apace100.apoli.mixin.loot_table.id;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.DynamicOps;
import io.github.apace100.apoli.access.IdentifiedLootTable;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootDataType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(LootDataType.class)
public abstract class LootDataTypeMixin<T> {
    @ModifyReturnValue(method = "deserialize", at = @At("RETURN"))
    private <V> Optional<T> apoli_legacy$associateIdToData(Optional<T> original, @Local(argsOnly = true) DynamicOps<V> ops, @Local(argsOnly = true) ResourceLocation id) {
        return original.map(e -> {
            if (e instanceof IdentifiedLootTable identifiedLootTable) {
                HolderGetter.Provider lookup = null;
                if (ops instanceof RegistryOps<V> registryOps && ((RegistryOpsAccessor) registryOps).apoli_legacy$getLookupProvider() instanceof HolderLookupAdapterAccessor acc) {
                    lookup = acc.apoli_legacy$getLookupProvider().asGetterLookup();
                }

                identifiedLootTable.apoli$setId(id, lookup);
            }

            return e;
        });
    }
}
