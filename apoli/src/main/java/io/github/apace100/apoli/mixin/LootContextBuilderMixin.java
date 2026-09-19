package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.ReplacingLootContext;
import io.github.apace100.apoli.access.ReplacingLootContextParameterSet;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootContext.Builder.class)
public class LootContextBuilderMixin {
    @Shadow
    @Final
    private LootParams params;

    @Inject(method = "create", at = @At("RETURN"))
    private void setLootContextType(CallbackInfoReturnable<LootContext> cir) {
        ReplacingLootContextParameterSet rlcps = (ReplacingLootContextParameterSet) params;

        ReplacingLootContext rlc = (ReplacingLootContext) cir.getReturnValue();
        rlc.setType(rlcps.getType());
    }
}
