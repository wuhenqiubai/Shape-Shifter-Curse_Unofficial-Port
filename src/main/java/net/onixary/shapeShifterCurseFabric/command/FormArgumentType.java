package net.onixary.shapeShifterCurseFabric.command;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.ISubForm;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;
import net.onixary.shapeShifterCurseFabric.util.ClientUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiPredicate;
import java.util.function.Function;


public class FormArgumentType implements ArgumentType<Identifier> {
    public static final HashMap<Identifier, Function<@Nullable Player, @NotNull List<Identifier>>> SUGGESTIONS_PROVIDERS_REGISTRY = new HashMap<>();

    public static @NotNull Identifier registerSuggestionsProvider(@NotNull Identifier id, @NotNull Function<@Nullable Player, @NotNull List<Identifier>> suggestionsProvider) {
        SUGGESTIONS_PROVIDERS_REGISTRY.put(id, suggestionsProvider);
        return id;
    }

    public static @Nullable Function<@Nullable Player, @NotNull List<Identifier>> getRegisteredSuggestionsProvider(@Nullable Identifier id) {
        return SUGGESTIONS_PROVIDERS_REGISTRY.get(id);
    }

    public static Function<@Nullable Player, @NotNull List<Identifier>> buildSuggestionsProvider(BiPredicate<@Nullable Player, @NotNull IForm> filter) {
        return player -> {
            List<Identifier> availableForms = new ArrayList<>();
            RegPlayerForms.playerForms.forEach((formID, form) -> {
                if (filter.test(player, form)) {
                    availableForms.add(form.getFormID());
                }
            });
            return availableForms;
        };
    }

    public static final BiPredicate<@Nullable Player, @NotNull IForm> NonDynamicForms = (player, form) -> !form.isDynamicForm();
    public static final BiPredicate<@Nullable Player, @NotNull IForm> DynamicForms = (player, form) -> form.isDynamicForm();
    public static final BiPredicate<@Nullable Player, @NotNull IForm> NonSubForms = (player, form) -> !(form instanceof ISubForm subForm && subForm.isSubForm());
    public static final BiPredicate<@Nullable Player, @NotNull IForm> SubForms = (player, form) -> (form instanceof ISubForm subForm && subForm.isSubForm());
    public static final BiPredicate<@Nullable Player, @NotNull IForm> UsableForms = FormUtils::isFormCanUse;
    public static final Identifier ALL_FORM_ARG = registerSuggestionsProvider(ShapeShifterCurseFabric.identifier("all_form_arg"), buildSuggestionsProvider(((player, form) -> true)));
    public static final Identifier SET_FORM_ARG = registerSuggestionsProvider(ShapeShifterCurseFabric.identifier("set_form_arg"), buildSuggestionsProvider(NonDynamicForms.and(NonSubForms)));
    public static final Identifier SET_DYNAMIC_FORM_ARG = registerSuggestionsProvider(ShapeShifterCurseFabric.identifier("set_dynamic_form_arg"), buildSuggestionsProvider(DynamicForms.and(NonSubForms)));
    public static final Identifier SET_SUB_FORM_ARG = registerSuggestionsProvider(ShapeShifterCurseFabric.identifier("set_sub_form_arg"), buildSuggestionsProvider(SubForms.and(UsableForms)));

    private final Identifier suggestionsProvider;

    public FormArgumentType(Identifier suggestionsProvider) {
        this.suggestionsProvider = suggestionsProvider;
    }

    public static final DynamicCommandExceptionType FORM_NOT_FOUND = new DynamicCommandExceptionType(
        o -> Component.translatable("commands.shape-shifter-curse.form_not_found", o)
    );

    public Identifier parse(StringReader stringReader) throws CommandSyntaxException {
       return Identifier.read(stringReader);
    }

    public static IForm getForm(CommandContext<CommandSourceStack> context, String argumentName) throws CommandSyntaxException {

       Identifier id = context.getArgument(argumentName, Identifier.class);

       try {
             return RegPlayerForms.playerForms.get(id);
       }

       catch(IllegalArgumentException e) {
          throw FORM_NOT_FOUND.create(id);
       }

    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {

       List<Identifier> availableForms = new ArrayList<>();

       try {
           if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
               Player player = ClientUtils.getPlayer();
               Function<@Nullable Player, @NotNull List<Identifier>> suggestionsProviderFunc = getRegisteredSuggestionsProvider(suggestionsProvider);
               if (suggestionsProviderFunc != null) {
                   availableForms = suggestionsProviderFunc.apply(player);
               }
           }
       }
       catch(IllegalArgumentException ignored) {}
       return SharedSuggestionProvider.suggestResource(availableForms.stream(), builder);
    }

    public static class Form_ArgumentType_Serializer implements ArgumentTypeInfo<FormArgumentType, FormArgumentType.Form_ArgumentType_Serializer.Form_ArgumentType_Properties> {

        @Override
        public void serializeToNetwork(Form_ArgumentType_Properties properties, FriendlyByteBuf buf) {
            buf.writeIdentifier(properties.data);
        }

        @Override
        public Form_ArgumentType_Properties deserializeFromNetwork(FriendlyByteBuf buf) {
            return new Form_ArgumentType_Properties(this, buf.readIdentifier());
        }

        @Override
        public void serializeToJson(Form_ArgumentType_Properties properties, JsonObject json) {
            json.addProperty("data", properties.data.toString());
        }

        @Override
        public Form_ArgumentType_Properties unpack(FormArgumentType argumentType) {
            return new Form_ArgumentType_Properties(this, argumentType.suggestionsProvider);
        }

        public class Form_ArgumentType_Properties implements ArgumentTypeInfo.Template<FormArgumentType> {
            final Identifier data;

            public Form_ArgumentType_Properties(Form_ArgumentType_Serializer ArgumentSerializer, Identifier data) {
                this.data = data;
            }

            public FormArgumentType instantiate(CommandBuildContext commandRegistryAccess) {
                return new FormArgumentType(data);
            }

            public ArgumentTypeInfo<FormArgumentType, ?> type() {
                return Form_ArgumentType_Serializer.this;
            }
        }
    }
}