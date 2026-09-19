package io.github.apace100.apoli.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class PowerTypeArgumentType implements ArgumentType<ResourceLocation> {

    public static PowerTypeArgumentType power() {
        return new PowerTypeArgumentType();
    }
    
    public ResourceLocation parse(StringReader reader) throws CommandSyntaxException {
        return ResourceLocation.read(reader);
    }

    public static PowerType<?> getPower(CommandContext<CommandSourceStack> context, String argumentName) {
        return PowerTypeRegistry.get(context.getArgument(argumentName, ResourceLocation.class));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(PowerTypeRegistry.identifiers(), builder);
    }
}
