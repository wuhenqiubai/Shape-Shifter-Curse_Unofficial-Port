package net.onixary.shapeShifterCurseFabric.recipes.altar;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.recipes.RecipeSerializerRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class AltarShapedRecipe extends AltarRecipe {
    public final ShapedRecipePattern pattern;
    public final ItemStackTemplate output;
    public final @Nullable Ingredient catalyst;
    public final int recipeTime;
    public final int fuelCostPerTick;
    public final @Nullable Identifier requireAdvancement;
    private @Nullable PlacementInfo placementInfo;

    // 26.1 的 ItemStackTemplate 序列化类型 + 1.21.11 的 totalFuelCost（Cost 系统精确燃料预算）并存
    public AltarShapedRecipe(ShapedRecipePattern pattern, ItemStackTemplate output, @Nullable Ingredient catalyst, int recipeTime, int fuelCostPerTick, @Nullable Identifier requireAdvancement, int totalFuelCost) {
        this.pattern = pattern;
        this.output = output;
        this.catalyst = catalyst;
        this.recipeTime = recipeTime;
        this.fuelCostPerTick = fuelCostPerTick;
        this.requireAdvancement = requireAdvancement;
        // 精确燃料预算（单位 fuel unit，1 个月尘 = 800）；-1 表示未指定，走 legacy 的逐 tick fuel_cost。
        this.totalFuelCost = totalFuelCost;
    }

    // 1.21.1 侧曾在此覆写 getIngredients()（那边 Recipe 接口有该方法，且 StackedContents.canCraft 会读它）。
    // 1.21.11 的 Recipe 接口已删除 getIngredients()（改由 placementInfo() 供 StackedItemContents.canCraft 读取，
    // 本类的 placementInfo() 已正确实现），且 ShapedRecipePattern.ingredients() 返回 List<Optional<Ingredient>>，
    // 与该覆写的 NonNullList<Ingredient> 返回类型不符 —— 故不再保留。

    @Override
    public int recipeTime() {
        return recipeTime;
    }

    // 进度锁：require_advancement 未完成则不可合成
    @Override
    public boolean canCraft(@Nullable Player player) {
        if (requireAdvancement == null) {
            return true;
        }
        if (player instanceof ServerPlayer playerEntity) {
            MinecraftServer server = playerEntity.level().getServer();
            AdvancementHolder advancement = server.getAdvancements().get(requireAdvancement);
            if (advancement == null) {
                return false;
            }
            AdvancementProgress advancementProgress = playerEntity.getAdvancements().getOrStartProgress(advancement);
            return advancementProgress.isDone();
        }
        return false;
    }

    @Override
    public @NonNull PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.createFromOptionals(this.pattern.ingredients());
        }
        return this.placementInfo;
    }

    @Override
    public @NonNull RecipeBookCategory recipeBookCategory() {
        // 该类无 category 字段（1.21.1 版）；alter 配方无分类概念，固定 MISC
        return RecipeBookCategories.CRAFTING_MISC;
    }

    private boolean matchesPattern(RecipeInput inv, int offsetX, int offsetY, boolean flipped) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                int k = i - offsetX;
                int l = j - offsetY;
                Optional<Ingredient> ingredient = Optional.empty();
                if (k >= 0 && l >= 0 && k < this.pattern.width() && l < this.pattern.height()) {
                    if (flipped) {
                        ingredient = this.pattern.ingredients().get(this.pattern.width() - k - 1 + l * this.pattern.width());
                    } else {
                        ingredient = this.pattern.ingredients().get(k + l * this.pattern.width());
                    }
                }
                if (!Ingredient.testOptionalIngredient(ingredient, inv.getItem(i + j * 3))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean matches(@NonNull RecipeInput recipeInput, @NonNull Level world) {
        if (this.catalyst != null) {
            ItemStack itemStack = recipeInput.getItem(9);
            if (!this.catalyst.test(itemStack)) {
                return false;
            }
        }

        for (int i = 0; i <= 3 - this.pattern.width(); ++i) {
            for (int j = 0; j <= 3 - this.pattern.height(); ++j) {
                if (this.matchesPattern(recipeInput, i, j, true)) {
                    return true;
                }
                if (this.matchesPattern(recipeInput, i, j, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int fuelUsage() {
        return fuelCostPerTick;
    }

    @Override
    public @NotNull ItemStack assemble(RecipeInput input) {
        return this.output.create();
    }

    @Override
    public @NonNull RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return RecipeSerializerRegister.ALTAR_SHAPED_RECIPE;
    }

    public static class Serializer {
        public static final MapCodec<AltarShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                ShapedRecipePattern.MAP_CODEC.forGetter(r -> r.pattern),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.output),
                Ingredient.CODEC.optionalFieldOf("catalyst").forGetter(r -> Optional.ofNullable(r.catalyst)),
                Codec.INT.optionalFieldOf("time", 200).forGetter(r -> r.recipeTime),
                Codec.INT.optionalFieldOf("fuel_cost", 1).forGetter(r -> r.fuelCostPerTick),
                Identifier.CODEC.optionalFieldOf("require_advancement").forGetter(r -> Optional.ofNullable(r.requireAdvancement)),
                // 数据包用「月尘个数」表达燃料预算，内部换算成 fuel unit（1 个尘 = 800）。
                // ⚠ 字段缺省值与「显式写了 0」必须区分：0 表示「本配方不耗燃料」（totalFuelCost=0），
                //   而字段整个缺失才表示「未指定」（totalFuelCost=-1 → 退回逐 tick fuel_cost）。
                //   所以这里用无默认值的 optionalFieldOf（拿到 Optional），不要写死默认 0。
                Codec.INT.optionalFieldOf("moondust_cost")
                        .forGetter(r -> r.totalFuelCost >= 0 ? Optional.of(r.totalFuelCost / 800) : Optional.empty())
            ).apply(instance, (pattern, output, catalyst, time, fuelCost, requireAdvancement, moondustCost) ->
                new AltarShapedRecipe(pattern, output, catalyst.orElse(null), time, fuelCost, requireAdvancement.orElse(null),
                        moondustCost.map(integer -> integer * 800).orElse(-1)))
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, AltarShapedRecipe> STREAM_CODEC = StreamCodec.of(
            Serializer::toNetwork, Serializer::fromNetwork
        );

        // 26.1 侧此处为空：pattern 改由官方的 ShapedRecipePattern.MAP_CODEC 解析（见上面的 CODEC），
        // 1.21.11 那套手工解析辅助方法（getPattern / findFirstSymbol / findLastSymbol / removePadding）
        // 因此整体作废；Serializer 在 26.1 也不再 implements RecipeSerializer（RecipeSerializer 成了 record），
        // 故 codec() / streamCodec() 两个 @Override 一并消失。

        private static AltarShapedRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            Ingredient catalyst = null;
            if (buf.readBoolean()) {
                catalyst = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            }
            Identifier requireAdvancement = null;
            if (buf.readBoolean()) {
                requireAdvancement = Identifier.STREAM_CODEC.decode(buf);
            }
            ShapedRecipePattern pattern = ShapedRecipePattern.STREAM_CODEC.decode(buf);
            ItemStackTemplate output = ItemStackTemplate.STREAM_CODEC.decode(buf);
            int time = buf.readVarInt();
            int fuelCost = buf.readVarInt();
            // 必须与 toNetwork 的写入顺序严格对应 —— 那边最后还写了 totalFuelCost，
            // 这里此前漏读，会让后续读到的字节整体错位。
            int totalFuelCost = buf.readVarInt();
            return new AltarShapedRecipe(pattern, output, catalyst, time, fuelCost, requireAdvancement, totalFuelCost);
        }

        private static void toNetwork(RegistryFriendlyByteBuf packetByteBuf, AltarShapedRecipe altarRecipe) {
            if (altarRecipe.catalyst != null) {
                packetByteBuf.writeBoolean(true);
                Ingredient.CONTENTS_STREAM_CODEC.encode(packetByteBuf, altarRecipe.catalyst);
            } else {
                packetByteBuf.writeBoolean(false);
            }
            if (altarRecipe.requireAdvancement != null) {
                packetByteBuf.writeBoolean(true);
                Identifier.STREAM_CODEC.encode(packetByteBuf, altarRecipe.requireAdvancement);
            } else {
                packetByteBuf.writeBoolean(false);
            }
            ShapedRecipePattern.STREAM_CODEC.encode(packetByteBuf, altarRecipe.pattern);
            ItemStackTemplate.STREAM_CODEC.encode(packetByteBuf, altarRecipe.output);
            packetByteBuf.writeVarInt(altarRecipe.recipeTime);
            packetByteBuf.writeVarInt(altarRecipe.fuelCostPerTick);
            packetByteBuf.writeVarInt(altarRecipe.totalFuelCost);
        }
    }
}
