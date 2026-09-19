package io.github.apace100.calio;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.HashMap;
import java.util.Locale;
import java.util.function.Function;

public class SerializationHelper {

    // Use SerializableDataTypes.ATTRIBUTE_MODIFIER instead
    @Deprecated
    public static AttributeModifier readAttributeModifier(JsonElement jsonElement) {
        if(jsonElement.isJsonObject()) {
            JsonObject json = jsonElement.getAsJsonObject();
            String name = GsonHelper.getAsString(json, "name", "calio:unnamed");
            String operation = GsonHelper.getAsString(json, "operation").toUpperCase(Locale.ROOT);
            double value = GsonHelper.getAsFloat(json, "value");
            return new AttributeModifier(SerializableDataTypes.convertNameToLocation(name), value, AttributeModifier.Operation.valueOf(operation));
        }
        throw new JsonSyntaxException("Attribute modifier needs to be a JSON object.");
    }

    // Use SerializableDataTypes.ATTRIBUTE_MODIFIER instead
    @Deprecated
    public static AttributeModifier readAttributeModifier(FriendlyByteBuf buf) {
        return AttributeModifier.STREAM_CODEC.decode(buf);
    }

    // Use SerializableDataTypes.ATTRIBUTE_MODIFIER instead
    @Deprecated
    public static void writeAttributeModifier(FriendlyByteBuf buf, AttributeModifier modifier) {
        AttributeModifier.STREAM_CODEC.encode(buf, modifier);
    }

    public static MobEffectInstance readStatusEffect(JsonElement jsonElement) {
        if(jsonElement.isJsonObject()) {
            JsonObject json = jsonElement.getAsJsonObject();
            String effect = GsonHelper.getAsString(json, "effect");
            var effectOptional = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.tryParse(effect));
            if(!effectOptional.isPresent()) {
                throw new JsonSyntaxException("Error reading status effect: could not find status effect with id: " + effect);
            }
            int duration = GsonHelper.getAsInt(json, "duration", 100);
            int amplifier = GsonHelper.getAsInt(json, "amplifier", 0);
            boolean ambient = GsonHelper.getAsBoolean(json, "is_ambient", false);
            boolean showParticles = GsonHelper.getAsBoolean(json, "show_particles", true);
            boolean showIcon = GsonHelper.getAsBoolean(json, "show_icon", true);
            return new MobEffectInstance(effectOptional.get(), duration, amplifier, ambient, showParticles, showIcon);
        } else {
            throw new JsonSyntaxException("Expected status effect to be a json object.");
        }
    }

    public static MobEffectInstance readStatusEffect(FriendlyByteBuf buf) {
        ResourceLocation effect = buf.readResourceLocation();
        int duration = buf.readInt();
        int amplifier = buf.readInt();
        boolean ambient = buf.readBoolean();
        boolean showParticles = buf.readBoolean();
        boolean showIcon = buf.readBoolean();
        return new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.getHolder(effect).orElseThrow(), duration, amplifier, ambient, showParticles, showIcon);
    }

    public static void writeStatusEffect(FriendlyByteBuf buf, MobEffectInstance statusEffectInstance) {
        buf.writeResourceLocation(BuiltInRegistries.MOB_EFFECT.getKey(statusEffectInstance.getEffect().value()));
        buf.writeInt(statusEffectInstance.getDuration());
        buf.writeInt(statusEffectInstance.getAmplifier());
        buf.writeBoolean(statusEffectInstance.isAmbient());
        buf.writeBoolean(statusEffectInstance.isVisible());
        buf.writeBoolean(statusEffectInstance.showIcon());
    }

    public static <T extends Enum<T>> HashMap<String, T> buildEnumMap(Class<T> enumClass, Function<T, String> enumToString) {
        HashMap<String, T> map = new HashMap<>();
        for (T enumConstant : enumClass.getEnumConstants()) {
            map.put(enumToString.apply(enumConstant), enumConstant);
        }
        return map;
    }
}
