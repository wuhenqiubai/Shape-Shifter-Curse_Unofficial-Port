package net.onixary.shapeShifterCurseFabric.recipes.altar;

import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.blocks.block_entity.AltarBlockEntity;
import net.onixary.shapeShifterCurseFabric.recipes.RecipeSerializerRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.function.*;

public class BuiltinAltarRecipe extends AltarRecipe {
    public static final HashMap<Identifier, BARecipeConfig> BARecipeConfigMap = new HashMap<>();

    public record BARecipeConfig (
            BiPredicate<AltarBlockEntity, Level> match,
            BiFunction<AltarBlockEntity, HolderLookup.Provider, ItemStack> craft,
            Function<HolderLookup.Provider, ItemStack> virtualOutput,
            Predicate<Player> canCraft,
            int recipeTime,
            int fuelUsage,
            Predicate<AltarBlockEntity> isInputsCountEnough,
            Consumer<AltarBlockEntity> consumeInputs,
            Function<AltarBlockEntity, List<ItemStack>> extraOutput
    ) {
        public void register(Identifier id) {
            BARecipeConfigMap.put(id, this);
        }

        public static @Nullable BARecipeConfig get(Identifier id) {
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
        public BARecipeConfig build() { return new BARecipeConfig(match, craft, virtualOutput, canCraft, recipeTime, fuelUsage, isInputsCountEnough, consumeInputs, extraOutput); }

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

                // 1.21.11: StackedContents 变成了泛型的 StackedItemContents（accountStack 仍在），与 AltarShapelessRecipe 对齐
                StackedItemContents recipeMatcher = new StackedItemContents();
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

    /** 配方配置 id（指向 {@link BARecipeConfigMap}）；1.21.1 Recipe 不再自带配方 id（由 RecipeHolder 管理）。 */
    public final Identifier configId;
    public final BARecipeConfig recipeConfig;

    public BuiltinAltarRecipe(Identifier configId, BARecipeConfig recipeConfig) {
        this.configId = configId;
        this.recipeConfig = recipeConfig;
    }

    @Override
    public int recipeTime() {
        return this.recipeConfig.recipeTime;
    }

    @Override
    public boolean matches(RecipeInput recipeInput, Level world) {
        if (recipeInput instanceof AltarBlockEntity altarBlockEntity) {
            return this.recipeConfig.match.test(altarBlockEntity, world);
        }
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(RecipeInput input) {
        // 26.1: Recipe#assemble 收敛为单参（旧的 (T, HolderLookup.Provider) 已移除），
        // 原先由形参传入的 provider 改从方块实体所在世界的 registryAccess 取
        // （与 AltarBlockEntity 内既有的 world.registryAccess() 用法一致，同样带 null 检查）。
        if (input instanceof AltarBlockEntity altarBlockEntity) {
            Level world = altarBlockEntity.getLevel();
            if (world != null) {
                return this.recipeConfig.craft.apply(altarBlockEntity, world.registryAccess());
            }
        }
        return ItemStack.EMPTY;
    }

    // 1.21.11 的 Recipe 接口删掉了 canCraftInDimensions / getResultItem（结果展示改走 display()/RecipeDisplay），
    // 并新增了 placementInfo() / recipeBookCategory()。本配方由 Java 代码驱动、没有原料表，故不可放置。
    @Override
    public @NonNull PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public @NonNull RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    /** 原 getResultItem 的语义（虚拟产物），Recipe 接口已不再要求，保留为普通方法供内部/调试使用。 */
    public @NotNull ItemStack getVirtualResultItem(HolderLookup.Provider provider) {
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
    public @NonNull RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return RecipeSerializerRegister.BUILTIN_Altar_RECIPE;
    }

    public static class Serializer {
        /** JSON：只存 recipe_config_id，decode 时从 BARecipeConfigMap 查运行时配置。 */
        public static final MapCodec<BuiltinAltarRecipe> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                Identifier.CODEC.fieldOf("recipe_config_id").forGetter(r -> r.configId)
            ).apply(instance, BuiltinAltarRecipe::fromConfigId)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, BuiltinAltarRecipe> STREAM_CODEC =
            StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);


        private static BuiltinAltarRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            Identifier configId = buf.readIdentifier();
            return fromConfigId(configId);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, BuiltinAltarRecipe alterRecipe) {
            buf.writeIdentifier(alterRecipe.configId);
        }
    }

    /** 从配置 id 查运行时配置构造配方（配置未注册则抛错）。 */
    private static BuiltinAltarRecipe fromConfigId(Identifier configId) {
        BARecipeConfig recipeConfig = BARecipeConfig.get(configId);
        if (recipeConfig == null) {
            throw new JsonSyntaxException("Unknown recipe_config_id: " + configId);
        }
        return new BuiltinAltarRecipe(configId, recipeConfig);
    }
}
