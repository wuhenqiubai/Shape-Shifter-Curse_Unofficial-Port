package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.ReplacingLootContextParameterSet;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootParams.Builder.class)
public class LootContextParameterSetBuilderMixin {
    @Inject(method = "create", at = @At("RETURN"))
    private void setLootContextType(ContextKeySet type, CallbackInfoReturnable<LootContext> cir) {
        ReplacingLootContextParameterSet rlc = (ReplacingLootContextParameterSet) cir.getReturnValue();
        rlc.setType(type);
    }
}
