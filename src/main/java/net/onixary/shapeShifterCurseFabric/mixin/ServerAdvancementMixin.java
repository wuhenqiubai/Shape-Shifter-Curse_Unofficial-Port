package net.onixary.shapeShifterCurseFabric.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.onixary.shapeShifterCurseFabric.util.AdvancementUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ServerAdvancementManager.class)
public class ServerAdvancementMixin {
	@Shadow
	private Map<Identifier, AdvancementHolder> advancements;

	@Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/AdvancementTree;<init>()V",shift = At.Shift.BEFORE))
	private void patchAdvancements(Map<Identifier, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
		ImmutableMap.Builder<Identifier,AdvancementHolder> builder = ImmutableMap.builder();
		this.advancements.forEach((id, entry) ->
				builder.put(id,AdvancementUtils.onAdvancementAdded(entry)));
		this.advancements = builder.buildOrThrow();

	}
}