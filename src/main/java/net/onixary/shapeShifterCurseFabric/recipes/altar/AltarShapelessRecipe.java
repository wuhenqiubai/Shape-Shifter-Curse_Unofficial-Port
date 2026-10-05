package net.onixary.shapeShifterCurseFabric.recipes.altar;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.recipes.RecipeSerializerRegister;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

// 注：merge 时该文件被误解决（丢了 catalyst/fuelCostPerTick/output，拼成 1.21.11 骨架+1.21.1 网络残骸）。
// 这里按 1.21.1 语义整体复原，并用 1.21.11 接口（PlacementInfo/recipeBookCategory/StackedItemContents/Ingredient.CODEC）。
public class AltarShapelessRecipe extends AltarRecipe {
    public final ItemStackTemplate output;
    public final List<Ingredient> input;
    public final @Nullable Ingredient catalyst;
    public final int recipeTime;
    public final int fuelCostPerTick;
    private @Nullable PlacementInfo placementInfo;

    public final @Nullable Identifier requireAdvancement;

    // 1.21.11 侧 input 为 List<Ingredient>（1.21.1 侧是 NonNullList，那边 Recipe 接口还有 getIngredients()）；
    // 这里保留 1.21.11 的 List，便于沿用 Ingredient.CODEC.listOf(1, 9)。
    // 26.1 的 ItemStackTemplate 序列化类型 + 1.21.11 的 totalFuelCost（Cost 系统精确燃料预算）并存。
    public AltarShapelessRecipe(ItemStackTemplate output, List<Ingredient> input, @Nullable Ingredient catalyst, int recipeTime, int fuelCostPerTick, @Nullable Identifier requireAdvancement, int totalFuelCost) {
        this.output = output;
        this.input = input;
        this.recipeTime = recipeTime;
        this.catalyst = catalyst;
        this.fuelCostPerTick = fuelCostPerTick;
        this.requireAdvancement = requireAdvancement;
        // 精确燃料预算（单位 fuel unit，1 个月尘 = 800）；-1 表示未指定，走 legacy 的逐 tick fuel_cost。
        this.totalFuelCost = totalFuelCost;
    }

    // [1.21.1 修复] 那边覆写了 getIngredients()（Recipe 接口有该方法，StackedContents.canCraft 会读它）。
    // 1.21.11 的 Recipe 接口已删除 getIngredients()：StackedItemContents.canCraft 改读 placementInfo()，
    // 本类的 placementInfo() 直接用 this.input 构造，语义等价，故无需该覆写。

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
            this.placementInfo = PlacementInfo.create(this.input);
        }
        return this.placementInfo;
    }

    // 1.21.11 Recipe 接口新增；alter 配方无分类概念（json 无 category/group），固定 MISC
    @Override
    public @NonNull RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public boolean matches(@NonNull RecipeInput recipeInput, @NonNull Level level) {
        if (this.catalyst != null) {
            ItemStack itemStack = recipeInput.getItem(9);
            if (!this.catalyst.test(itemStack)) {
                return false;
            }
        }

        StackedItemContents contents = new StackedItemContents();
        int i = 0;
        for (int j = 0; j < 9; ++j) {
            ItemStack itemStack = recipeInput.getItem(j);
            if (!itemStack.isEmpty()) {
                ++i;
                contents.accountStack(itemStack, 1);
            }
        }
        return i == this.input.size() && contents.canCraft(this, null);
    }

    // 1.21.1 修复点：shapeless 必须返回 fuelCostPerTick，否则 fuel_cost 配置不生效
    @Override
    public int fuelUsage() {
        return fuelCostPerTick;
    }

    @Override
    public @NonNull ItemStack assemble(RecipeInput input) {
        return this.output.create();
    }

    @Override
    public @NonNull RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return RecipeSerializerRegister.ALTAR_SHAPELESS_RECIPE;
    }

    public static class Serializer {
        public static final MapCodec<AltarShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.output),
                Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(r -> r.input),
                Ingredient.CODEC.optionalFieldOf("catalyst").forGetter(r -> Optional.ofNullable(r.catalyst)),
                Codec.INT.optionalFieldOf("time", 200).forGetter(r -> r.recipeTime),
                Codec.INT.optionalFieldOf("fuel_cost", 1).forGetter(r -> r.fuelCostPerTick),
                Identifier.CODEC.optionalFieldOf("require_advancement").forGetter(r -> Optional.ofNullable(r.requireAdvancement)),
                // 数据包用「月尘个数」表达燃料预算，内部换算成 fuel unit（1 个尘 = 800）。
                // ⚠ 字段缺省值与「显式写了 0」必须区分：0 表示「本配方不耗燃料」（totalFuelCost=0），
                //   而字段整个缺失才表示「未指定」（totalFuelCost=-1 → 退回逐 tick fuel_cost）。
                //   所以这里用无默认值的 optionalFieldOf（拿到 Optional），不要写死默认 0。
                // 1.21.11: Ingredient.CODEC 本身已是 non-empty（1.21.1 的 CODEC_NONEMPTY 在本版本不存在），
                // 故沿用上面的 Ingredient.CODEC.listOf(1, 9)（1.21.1 侧的 CODEC_NONEMPTY 写法不适用）。
                Codec.INT.optionalFieldOf("moondust_cost")
                        .forGetter(r -> r.totalFuelCost >= 0 ? Optional.of(r.totalFuelCost / 800) : Optional.empty())
            ).apply(instance, (output, input, catalyst, time, fuelCost, requireAdvancement, moondustCost) ->
                new AltarShapelessRecipe(output, input, catalyst.orElse(null), time, fuelCost, requireAdvancement.orElse(null),
                        moondustCost.map(integer -> integer * 800).orElse(-1)))
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, AltarShapelessRecipe> STREAM_CODEC = StreamCodec.of(
            Serializer::toNetwork, Serializer::fromNetwork
        );

        // 26.1 侧此处为空：Serializer 不再 implements RecipeSerializer（它成了 record，
        // 由 RecipeSerializerRegister 用 new RecipeSerializer<>(CODEC, STREAM_CODEC) 直接构造），
        // 故 codec() / streamCodec() 两个 @Override 消失 —— 1.21.11 在此修的「streamCodec 自引用无限递归」
        // 在 26.1 的结构下已不可能发生。

        private static AltarShapelessRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            Ingredient catalyst = null;
            if (buf.readBoolean()) {
                catalyst = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            }
            Identifier requireAdvancement = null;
            if (buf.readBoolean()) {
                requireAdvancement = Identifier.STREAM_CODEC.decode(buf);
            }
            List<Ingredient> list = Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
            ItemStackTemplate output = ItemStackTemplate.STREAM_CODEC.decode(buf);
            int time = buf.readVarInt();
            int fuelCost = buf.readVarInt();
            // 必须与 toNetwork 的写入顺序严格对应 —— 那边最后还写了 totalFuelCost，
            // 这里此前漏读，会让后续读到的字节整体错位。
            int totalFuelCost = buf.readVarInt();
            return new AltarShapelessRecipe(output, list, catalyst, time, fuelCost, requireAdvancement, totalFuelCost);
        }

        private static void toNetwork(RegistryFriendlyByteBuf packetByteBuf, AltarShapelessRecipe shapelessRecipe) {
            if (shapelessRecipe.catalyst != null) {
                packetByteBuf.writeBoolean(true);
                Ingredient.CONTENTS_STREAM_CODEC.encode(packetByteBuf, shapelessRecipe.catalyst);
            } else {
                packetByteBuf.writeBoolean(false);
            }
            if (shapelessRecipe.requireAdvancement != null) {
                packetByteBuf.writeBoolean(true);
                Identifier.STREAM_CODEC.encode(packetByteBuf, shapelessRecipe.requireAdvancement);
            } else {
                packetByteBuf.writeBoolean(false);
            }
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).encode(packetByteBuf, shapelessRecipe.input);
            ItemStackTemplate.STREAM_CODEC.encode(packetByteBuf, shapelessRecipe.output);
            packetByteBuf.writeVarInt(shapelessRecipe.recipeTime);
            packetByteBuf.writeVarInt(shapelessRecipe.fuelCostPerTick);
            packetByteBuf.writeVarInt(shapelessRecipe.totalFuelCost);
        }
    }
}
