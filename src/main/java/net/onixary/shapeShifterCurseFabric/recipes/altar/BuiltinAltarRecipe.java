package net.onixary.shapeShifterCurseFabric.recipes.altar;

import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.blocks.block_entity.AltarBlockEntity;
import net.onixary.shapeShifterCurseFabric.recipes.RecipeSerializerRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.function.*;

public class BuiltinAltarRecipe extends AltarRecipe {
    public static final HashMap<ResourceLocation, BARecipeConfig> BARecipeConfigMap = new HashMap<>();

    public record BARecipeConfig (
            BiPredicate<AltarBlockEntity, Level> match,
            BiFunction<AltarBlockEntity, HolderLookup.Provider, ItemStack> craft,
            Function<HolderLookup.Provider, ItemStack> virtualOutput,
            Predicate<Player> canCraft,
            int recipeTime,
            int fuelUsage,
            Predicate<AltarBlockEntity> isInputsCountEnough,
            Consumer<AltarBlockEntity> consumeInputs,
            Function<AltarBlockEntity, List<ItemStack>> extraOutput,
            @Nullable Item catalyst
    ) {
        public void register(ResourceLocation id) {
            BARecipeConfigMap.put(id, this);
        }

        public static @Nullable BARecipeConfig get(ResourceLocation id) {
            return BARecipeConfigMap.get(id);
        }
    }

    public static class BARecipeConfigBuilder {
        BiPredicate<AltarBlockEntity, Level> match = (altarBlockEntity, world) -> false;
        BiFunction<AltarBlockEntity, HolderLookup.Provider, ItemStack> craft = (altarBlockEntity, provider) -> ItemStack.EMPTY;
        Function<HolderLookup.Provider, ItemStack> virtualOutput = provider -> ItemStack.EMPTY;
        Predicate<Player> canCraft = player -> true;
        int recipeTime = 200;
        int fuelUsage = 1;
        Predicate<AltarBlockEntity> isInputsCountEnough = altarBlockEntity -> true;
        Consumer<AltarBlockEntity> consumeInputs = altarBlockEntity -> {};
        Function<AltarBlockEntity, List<ItemStack>> extraOutput = altarBlockEntity -> List.of();
        @Nullable Item catalyst = null;

        public BARecipeConfigBuilder() { }

        public BARecipeConfigBuilder match(BiPredicate<AltarBlockEntity, Level> match) { this.match = match; return this; }
        public BARecipeConfigBuilder craft(BiFunction<AltarBlockEntity, HolderLookup.Provider, ItemStack> craft) { this.craft = craft; return this; }
        public BARecipeConfigBuilder virtualOutput(Function<HolderLookup.Provider, ItemStack> virtualOutput) { this.virtualOutput = virtualOutput; return this; }
        public BARecipeConfigBuilder canCraft(Predicate<Player> canCraft) { this.canCraft = canCraft; return this; }
        public BARecipeConfigBuilder recipeTime(int recipeTime) { this.recipeTime = recipeTime; return this; }
        public BARecipeConfigBuilder fuelUsage(int fuelUsage) { this.fuelUsage = fuelUsage; return this; }
        public BARecipeConfigBuilder isInputsCountEnough(Predicate<AltarBlockEntity> isInputsCountEnough) { this.isInputsCountEnough = isInputsCountEnough; return this; }
        public BARecipeConfigBuilder consumeInputs(Consumer<AltarBlockEntity> consumeInputs) { this.consumeInputs = consumeInputs; return this; }
        public BARecipeConfigBuilder extraOutput(Function<AltarBlockEntity, List<ItemStack>> extraOutput) { this.extraOutput = extraOutput; return this; }
        public BARecipeConfigBuilder catalyst(@Nullable Item catalyst) { this.catalyst = catalyst; return this; }
        public BARecipeConfig build() { return new BARecipeConfig(match, craft, virtualOutput, canCraft, recipeTime, fuelUsage, isInputsCountEnough, consumeInputs, extraOutput, catalyst); }

        // TODO 还差几个预设生成器 比如match函数 让它支持Shape和Shapeless

        // 坏了 还得整TriPredicate TriFunction 顺带在整个TriConsumer吧 函数还得传配方自身
        public static BiPredicate<AltarBlockEntity, Level> createMatch_Shapeless(NonNullList<Ingredient> input, Ingredient catalyst) {
            return (altarBlockEntity, world) -> {
                if (catalyst != null) {
                    ItemStack itemStack = altarBlockEntity.getItem(9);
                    if (!catalyst.test(itemStack)) {
                        return false;
                    }
                }

                StackedContents recipeMatcher = new StackedContents();
                int i = 0;
                for(int j = 0; j < 9; ++j) {
                    ItemStack itemStack = altarBlockEntity.getItem(j);
                    if (!itemStack.isEmpty()) {
                        ++i;
                        recipeMatcher.accountStack(itemStack, 1);
                    }
                }

                // return i == input.size() && recipeMatcher.match(this, (IntList)null);
                return false;
            };
        }
    }

    public final ResourceLocation configId;
    public final BARecipeConfig recipeConfig;

    public BuiltinAltarRecipe(ResourceLocation configId, BARecipeConfig recipeConfig) {
        this.configId = configId;
        this.recipeConfig = recipeConfig;
    }

    @Override
    public int recipeTime() {
        return this.recipeConfig.recipeTime;
    }

    @Override
    public boolean matches(RecipeInput recipeInput, Level world) {
        if (recipeInput instanceof AltarRecipeInput input && input.owner() != null) {
            return this.recipeConfig.match.test(input.owner(), world);
        }
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(RecipeInput recipeInput, HolderLookup.Provider provider) {
        if (recipeInput instanceof AltarRecipeInput input && input.owner() != null) {
            return this.recipeConfig.craft.apply(input.owner(), provider);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.Provider provider) {
        return this.recipeConfig.virtualOutput.apply(provider);
    }

    @Override
    public boolean canCraft(@Nullable Player player) {
        return this.recipeConfig.canCraft.test(player);
    }

    @Override
    public boolean InputsCountEnough(WorldlyContainer inventory) {
        if (inventory instanceof AltarBlockEntity altarBlockEntity) {
            return this.recipeConfig.isInputsCountEnough.test(altarBlockEntity);
        }
        return false;
    }

    @Override
    public void consumeInputs(WorldlyContainer inventory) {
        if (inventory instanceof AltarBlockEntity altarBlockEntity) {
            this.recipeConfig.consumeInputs.accept(altarBlockEntity);
        }
    }

    @Override
    public List<ItemStack> getExtraOutput(WorldlyContainer inventory) {
        if (inventory instanceof AltarBlockEntity altarBlockEntity) {
            return this.recipeConfig.extraOutput.apply(altarBlockEntity);
        }
        return List.of();
    }

    @Override
    public int fuelUsage() {
        return this.recipeConfig.fuelUsage;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return RecipeSerializerRegister.BUILTIN_ALTAR_RECIPE;
    }

    public static class Serializer implements RecipeSerializer<BuiltinAltarRecipe> {
        private static final MapCodec<BuiltinAltarRecipe> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("recipe_config_id").forGetter(r -> r.configId)
            ).apply(instance, BuiltinAltarRecipe::fromConfigId)
        );

        private static final StreamCodec<RegistryFriendlyByteBuf, BuiltinAltarRecipe> STREAM_CODEC =
            StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        @Override
        public @NotNull MapCodec<BuiltinAltarRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, BuiltinAltarRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static BuiltinAltarRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            ResourceLocation configId = buf.readResourceLocation();
            return fromConfigId(configId);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, BuiltinAltarRecipe AltarRecipe) {
            buf.writeResourceLocation(AltarRecipe.configId);
        }
    }

    private static BuiltinAltarRecipe fromConfigId(ResourceLocation configId) {
        BARecipeConfig recipeConfig = BARecipeConfig.get(configId);
        if (recipeConfig == null) {
            throw new JsonSyntaxException("Unknown recipe_config_id: " + configId);
        }
        return new BuiltinAltarRecipe(configId, recipeConfig);
    }
}
