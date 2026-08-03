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

/**
 * 兼容 Calio-Legacy 1.11.2（1.20 移植版）的粒子 JSON 解析与 1.21 遗留数据格式。
 *
 * <p>SSCU 的粒子数据已回退到上游 1.20 格式（字符串 params，如 {"type": "minecraft:dust", "params": "0.07 0.07 0.07 2"}
 * 或字符串 id，如 "minecraft:nautilus"），Calio-Legacy 原生解析器（LegacyParticleOptionFactory + StringReader）
 * 能正确处理这类格式——本 mixin 对它们<b>放行走原方法</b>。</p>
 *
 * <p>仅对 1.21 对象格式（params 为对象，或无 params 的对象 {"type": "minecraft:enchant"}）用 1.21 标准
 * {@link ParticleTypes#CODEC} 解析，并展开 params 嵌套为平铺字段——作为兼容遗留数据的兜底。</p>
 */
@Mixin(SerializableDataType.class)
public abstract class CalioParticleEffectMixin {

    @Inject(method = "read(Lcom/google/gson/JsonElement;Lnet/minecraft/core/HolderLookup$Provider;)Ljava/lang/Object;", at = @At("HEAD"), cancellable = true)
    private void ssc$fixParticleEffectRead(JsonElement json, HolderLookup.Provider provider, CallbackInfoReturnable<Object> cir) {
        if ((Object) this != SerializableDataTypes.PARTICLE_EFFECT) {
            return;
        }
        // 1.20 字符串格式（字符串 id / 字符串 params）走 Calio 原生解析，不拦截
        if (!json.isJsonObject()) {
            return;
        }
        JsonElement params = json.getAsJsonObject().get("params");
        if (params != null && params.isJsonPrimitive()) {
            return;
        }
        // 1.21 对象格式（对象 params 或 无 params 对象）→ 1.21 codec + 展开
        cir.setReturnValue(ssc$readParticle21(json, provider));
    }

    @Unique
    private static ParticleOptions ssc$readParticle21(JsonElement json, HolderLookup.Provider provider) {
        JsonElement element = json;
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            if (obj.has("params") && obj.get("params").isJsonObject()) {
                // 1.21 旧格式 {"type": ..., "params": {...}} → 平铺成 1.21 格式
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
