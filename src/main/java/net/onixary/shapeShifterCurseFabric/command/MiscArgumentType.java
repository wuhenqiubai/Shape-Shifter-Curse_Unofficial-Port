package net.onixary.shapeShifterCurseFabric.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class MiscArgumentType {
    public static class Enum_ArgumentType implements ArgumentType<String> {
        public final List<String> Suggestions = new ArrayList<>();

        public Enum_ArgumentType(String... suggestions) {
            Suggestions.addAll(Arrays.asList(suggestions));
        }

        @Override
        public String parse(StringReader reader) throws CommandSyntaxException {
            return reader.readString();
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            String string = builder.getRemaining();
            for (String s : Suggestions) {
                if (s.startsWith(string)) {
                    builder.suggest(s);
                }
            }
            return builder.buildFuture();
        }
    }

    public static class Enum_ArgumentType_Serializer implements ArgumentTypeInfo<Enum_ArgumentType, Enum_ArgumentType_Serializer.Enum_ArgumentType_Properties> {

        @Override
        public void serializeToNetwork(Enum_ArgumentType_Properties properties, FriendlyByteBuf buf) {
            buf.writeInt(properties.data.size());
            for (String data : properties.data) {
                buf.writeUtf(data);
            }
        }

        @Override
        public Enum_ArgumentType_Properties deserializeFromNetwork(FriendlyByteBuf buf) {
            int size = buf.readInt();
            List<String> datas = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                datas.add(buf.readUtf());
            }
            return new Enum_ArgumentType_Properties(this, datas);
        }

        @Override
        public void serializeToJson(Enum_ArgumentType_Properties properties, JsonObject json) {
            JsonArray array = new JsonArray();
            for (String data : properties.data) {
                array.add(data);
            }
            json.add("data", array);
        }

        @Override
        public Enum_ArgumentType_Properties unpack(Enum_ArgumentType argumentType) {
            return new Enum_ArgumentType_Properties(this, argumentType.Suggestions);
        }

        public class Enum_ArgumentType_Properties implements ArgumentTypeInfo.Template<Enum_ArgumentType> {
            final List<String> data;

            public Enum_ArgumentType_Properties(Enum_ArgumentType_Serializer ArgumentSerializer, List<String> data) {
                this.data = data;
            }

            public Enum_ArgumentType instantiate(CommandBuildContext commandRegistryAccess) {
                return new Enum_ArgumentType(data.toArray(new String[0]));
            }

            public ArgumentTypeInfo<Enum_ArgumentType, ?> type() {
                return Enum_ArgumentType_Serializer.this;
            }
        }
    }
}