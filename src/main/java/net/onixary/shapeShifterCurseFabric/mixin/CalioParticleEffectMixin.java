package net.onixary.shapeShifterCurseFabric.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(SerializableDataType.class)
public abstract class CalioParticleEffectMixin {

    @Inject(method = "read(Lcom/google/gson/JsonElement;Lnet/minecraft/core/HolderLookup$Provider;)Ljava/lang/Object;", at = @At("HEAD"), cancellable = true)
    private void ssc$fixParticleEffectRead(JsonElement json, HolderLookup.Provider provider, CallbackInfoReturnable<Object> cir) {
        if ((Object) this != SerializableDataTypes.PARTICLE_EFFECT) {
            return;
        }
        if (!json.isJsonObject()) {
            return;
        }
        JsonElement params = json.getAsJsonObject().get("params");
        if (params != null && params.isJsonPrimitive()) {
            return;
        }
        cir.setReturnValue(ssc$readParticle21(json, provider));
    }

    @Unique
    private static ParticleOptions ssc$readParticle21(JsonElement json, HolderLookup.Provider provider) {
        JsonElement element = json;
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            if (obj.has("params") && obj.get("params").isJsonObject()) {
                JsonObject merged = obj.deepCopy();
                JsonObject params = merged.remove("params").getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : params.entrySet()) {
                    merged.add(entry.getKey(), entry.getValue());
                }
                element = merged;
            }
        }
        return ParticleTypes.CODEC.decode(provider.createSerializationContext(JsonOps.INSTANCE), element).getOrThrow().getFirst();
    }
}
