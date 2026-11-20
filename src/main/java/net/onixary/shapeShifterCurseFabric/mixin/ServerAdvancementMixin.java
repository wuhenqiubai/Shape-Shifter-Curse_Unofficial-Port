package net.onixary.shapeShifterCurseFabric.mixin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerAdvancementManager;
import net.onixary.shapeShifterCurseFabric.util.AdvancementUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ServerAdvancementManager.class)
public class ServerAdvancementMixin {
	@Mutable
	@Shadow
	private Map<Identifier, AdvancementHolder> advancements;

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/AdvancementTree;<init>()V", shift = At.Shift.BEFORE))
	private void ssc$patchAdvancements(HolderLookup.Provider registries, CallbackInfo ci) {
		ImmutableMap.Builder<Identifier, AdvancementHolder> builder = ImmutableMap.builder();
		this.advancements.forEach((id, entry) -> builder.put(id, AdvancementUtils.onAdvancementAdded(entry)));
		this.advancements = builder.buildOrThrow();
	}
}
