package net.onixary.shapeShifterCurseFabric.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.onixary.shapeShifterCurseFabric.util.AdvancementUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ServerAdvancementLoader.class)
public class ServerAdvancementMixin {
	@Shadow
	private Map<Identifier, AdvancementEntry> advancements;

	@Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/AdvancementManager;<init>()V",shift = At.Shift.BEFORE))
	private void patchAdvancements(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
		ImmutableMap.Builder<Identifier,AdvancementEntry> builder = ImmutableMap.builder();
		this.advancements.forEach((id, entry) ->
				builder.put(id,AdvancementUtils.onAdvancementAdded(entry)));
		this.advancements = builder.buildOrThrow();

	}
}
