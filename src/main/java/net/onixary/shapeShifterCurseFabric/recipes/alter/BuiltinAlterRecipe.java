package net.onixary.shapeShifterCurseFabric.recipes.alter;

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
import net.onixary.shapeShifterCurseFabric.blocks.block_entity.AlterBlockEntity;
import net.onixary.shapeShifterCurseFabric.recipes.RecipeSerializerRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.function.*;

public class BuiltinAlterRecipe extends AlterRecipe {
    public static final HashMap<ResourceLocation, BARecipeConfig> BARecipeConfigMap = new HashMap<>();

    public record BARecipeConfig (
            BiPredicate<AlterBlockEntity, Level> match,
            BiFunction<AlterBlockEntity, HolderLookup.Provider, ItemStack> craft,
            Function<HolderLookup.Provider, ItemStack> virtualOutput,
            Predicate<Player> canCraft,
            int recipeTime,
            int fuelUsage,
            Predicate<AlterBlockEntity> isInputsCountEnough,
            Consumer<AlterBlockEntity> consumeInputs,
            Function<AlterBlockEntity, List<ItemStack>> extraOutput
    ) {
        public void register(ResourceLocation id) {
            BARecipeConfigMap.put(id, this);
        }

        public static @Nullable BARecipeConfig get(ResourceLocation id) {
            return BARecipeConfigMap.get(id);
        }
    }

    public static class BARecipeConfigBuilder {
        BiPredicate<AlterBlockEntity, Level> match = (alterBlockEntity, world) -> false;
        BiFunction<AlterBlockEntity, HolderLookup.Provider, ItemStack> craft = (alterBlockEntity, provider) -> ItemStack.EMPTY;
        Function<HolderLookup.Provider, ItemStack> virtualOutput = provider -> ItemStack.EMPTY;
        Predicate<Player> canCraft = player -> true;
        int recipeTime = 200;
        int fuelUsage = 1;
        Predicate<AlterBlockEntity> isInputsCountEnough = alterBlockEntity -> true;
        Consumer<AlterBlockEntity> consumeInputs = alterBlockEntity -> {};
        Function<AlterBlockEntity, List<ItemStack>> extraOutput = alterBlockEntity -> List.of();

        public BARecipeConfigBuilder() { }

        public BARecipeConfigBuilder match(BiPredicate<AlterBlockEntity, Level> match) { this.match = match; return this; }
        public BARecipeConfigBuilder craft(BiFunction<AlterBlockEntity, HolderLookup.Provider, ItemStack> craft) { this.craft = craft; return this; }
        public BARecipeConfigBuilder virtualOutput(Function<HolderLookup.Provider, ItemStack> virtualOutput) { this.virtualOutput = virtualOutput; return this; }
        public BARecipeConfigBuilder canCraft(Predicate<Player> canCraft) { this.canCraft = canCraft; return this; }
        public BARecipeConfigBuilder recipeTime(int recipeTime) { this.recipeTime = recipeTime; return this; }
        public BARecipeConfigBuilder fuelUsage(int fuelUsage) { this.fuelUsage = fuelUsage; return this; }
        public BARecipeConfigBuilder isInputsCountEnough(Predicate<AlterBlockEntity> isInputsCountEnough) { this.isInputsCountEnough = isInputsCountEnough; return this; }
        public BARecipeConfigBuilder consumeInputs(Consumer<AlterBlockEntity> consumeInputs) { this.consumeInputs = consumeInputs; return this; }
        public BARecipeConfigBuilder extraOutput(Function<AlterBlockEntity, List<ItemStack>> extraOutput) { this.extraOutput = extraOutput; return this; }
        public BARecipeConfig build() { return new BARecipeConfig(match, craft, virtualOutput, canCraft, recipeTime, fuelUsage, isInputsCountEnough, consumeInputs, extraOutput); }

        // TODO 还差几个预设生成器 比如match函数 让它支持Shape和Shapeless

        // 坏了 还得整TriPredicate TriFunction 顺带在整个TriConsumer吧 函数还得传配方自身
        public static BiPredicate<AlterBlockEntity, Level> createMatch_Shapeless(NonNullList<Ingredient> input, Ingredient catalyst) {
            return (alterBlockEntity, world) -> {
                if (catalyst != null) {
                    ItemStack itemStack = alterBlockEntity.getItem(9);
                    if (!catalyst.test(itemStack)) {
                        return false;
                    }
                }

                StackedContents recipeMatcher = new StackedContents();
                int i = 0;
                for(int j = 0; j < 9; ++j) {
                    ItemStack itemStack = alterBlockEntity.getItem(j);
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

    /** 配方配置 id（指向 {@link BARecipeConfigMap}）；1.21.1 Recipe 不再自带配方 id（由 RecipeHolder 管理）。 */
    public final ResourceLocation configId;
    public final BARecipeConfig recipeConfig;

    public BuiltinAlterRecipe(ResourceLocation configId, BARecipeConfig recipeConfig) {
        this.configId = configId;
        this.recipeConfig = recipeConfig;
    }

    @Override
    public int recipeTime() {
        return this.recipeConfig.recipeTime;
    }

    @Override
    public boolean matches(RecipeInput recipeInput, Level world) {
        if (recipeInput instanceof AlterBlockEntity alterBlockEntity) {
            return this.recipeConfig.match.test(alterBlockEntity, world);
        }
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(RecipeInput recipeInput, HolderLookup.Provider provider) {
        if (recipeInput instanceof AlterBlockEntity alterBlockEntity) {
            return this.recipeConfig.craft.apply(alterBlockEntity, provider);
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
        if (inventory instanceof AlterBlockEntity alterBlockEntity) {
            return this.recipeConfig.isInputsCountEnough.test(alterBlockEntity);
        }
        return false;
    }

    @Override
    public void consumeInputs(WorldlyContainer inventory) {
        if (inventory instanceof AlterBlockEntity alterBlockEntity) {
            this.recipeConfig.consumeInputs.accept(alterBlockEntity);
        }
    }

    @Override
    public List<ItemStack> getExtraOutput(WorldlyContainer inventory) {
        if (inventory instanceof AlterBlockEntity alterBlockEntity) {
            return this.recipeConfig.extraOutput.apply(alterBlockEntity);
        }
        return List.of();
    }

    @Override
    public int fuelUsage() {
        return this.recipeConfig.fuelUsage;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return RecipeSerializerRegister.BUILTIN_ALTER_RECIPE;
    }

    public static class Serializer implements RecipeSerializer<BuiltinAlterRecipe> {
        /** JSON：只存 recipe_config_id，decode 时从 BARecipeConfigMap 查运行时配置。 */
        private static final MapCodec<BuiltinAlterRecipe> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("recipe_config_id").forGetter(r -> r.configId)
            ).apply(instance, BuiltinAlterRecipe::fromConfigId)
        );

        private static final StreamCodec<RegistryFriendlyByteBuf, BuiltinAlterRecipe> STREAM_CODEC =
            StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        @Override
        public @NotNull MapCodec<BuiltinAlterRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, BuiltinAlterRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static BuiltinAlterRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            ResourceLocation configId = buf.readResourceLocation();
            return fromConfigId(configId);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, BuiltinAlterRecipe alterRecipe) {
            buf.writeResourceLocation(alterRecipe.configId);
        }
    }

    /** 从配置 id 查运行时配置构造配方（配置未注册则抛错）。 */
    private static BuiltinAlterRecipe fromConfigId(ResourceLocation configId) {
        BARecipeConfig recipeConfig = BARecipeConfig.get(configId);
        if (recipeConfig == null) {
            throw new JsonSyntaxException("Unknown recipe_config_id: " + configId);
        }
        return new BuiltinAlterRecipe(configId, recipeConfig);
    }
}
