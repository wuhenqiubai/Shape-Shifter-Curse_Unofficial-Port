package net.onixary.shapeShifterCurseFabric.recipes.altar;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.TagValueInput;
import net.onixary.shapeShifterCurseFabric.blocks.RegCustomBlock;
import net.onixary.shapeShifterCurseFabric.blocks.block_entity.AltarBlockEntity;
import net.onixary.shapeShifterCurseFabric.items.RegCustomItem;
import net.onixary.shapeShifterCurseFabric.recipes.RecipeSerializerRegister;
import net.onixary.shapeShifterCurseFabric.util.TrinketUtils;
import org.jetbrains.annotations.Nullable;

/**
 * Runs inside Fabric's existing GameTest server: {@code ./gradlew checkAltarRecipes}
 *
 * <p><b>本文件是行为契约，不是实现细节的快照。</b>它断言的燃料精确预算、网络往返、匹配/消耗语义、
 * 催化槽永不消耗、容器剩余物返还、NBT 断点续造、600 tick 才产出等，都是「祭坛应该有的行为」。</p>
 *
 * <p>1.21.1 迁移时改过调用方式（旧写法全部失效、测试一度编译不过）：</p>
 * <ul>
 *   <li>{@code RecipeSerializer} 不再有 {@code read/write}，改用 {@code codec()} + {@code streamCodec()}</li>
 *   <li>{@code matches} 的参数由 {@code SidedInventory/WorldlyContainer} 换成 {@code RecipeInput}，
 *       故一律经 {@link AltarRecipeInput} 包装（BE 本身刻意不是 RecipeInput）</li>
 *   <li>BlockEntity 的 {@code createNbt/readNbt/getCachedState} → {@code saveWithFullMetadata/loadWithComponents/getBlockState}</li>
 *   <li>{@code WorldlyContainer} 的抽象方法在 Mojmap 下叫
 *       {@code getSlotsForFace/canPlaceItemThroughFace/canTakeItemThroughFace}</li>
 * </ul>
 */
public final class AltarRecipeCheck {
    private static final Identifier ID = Identifier.fromNamespaceAndPath("test", "altar");

    /** 12 槽的最小 WorldlyContainer（Mojmap 方法名，1.21.1 的 WorldlyContainer 有三个抽象方法）。 */
    private static final class Inventory extends SimpleContainer implements WorldlyContainer {
        Inventory() { super(12); }

        @Override
        public int[] getSlotsForFace(Direction side) { return new int[]{0, 1, 9}; }

        @Override
        public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) { return true; }

        @Override
        public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) { return true; }
    }

    /** 把容器包成配方匹配用的 RecipeInput（线性映射 0-9）。 */
    private static AltarRecipeInput input(Inventory inventory) {
        return new AltarRecipeInput(inventory.getItems());
    }

    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    /**
     * 从 JSON 反序列化。MapCodec 本身没有 parse(DynamicOps,...)，先 .codec() 转成 Codec 再解析。
     *
     * <p>⚠ 必须用 {@link RegistryOps}（而不是裸 JsonOps）：1.21.11 起 {@code Ingredient.CODEC} 是
     * {@code HolderSetCodec}，解析物品引用需要 registry 上下文，否则会退化到 decodeWithoutRegistry
     * 并报 "Can't decode element ... without registry"。</p>
     */
    private static AltarShapelessRecipe shapelessFromJson(String json, RegistryAccess registries) {
        return RecipeSerializerRegister.ALTAR_SHAPELESS_RECIPE.codec().codec()
                .parse(RegistryOps.create(JsonOps.INSTANCE, registries), JsonParser.parseString(json))
                .getOrThrow();
    }

    private static AltarShapedRecipe shapedFromJson(String json, RegistryAccess registries) {
        return RecipeSerializerRegister.ALTAR_SHAPED_RECIPE.codec().codec()
                .parse(RegistryOps.create(JsonOps.INSTANCE, registries), JsonParser.parseString(json))
                .getOrThrow();
    }

    private static AltarShapelessRecipe recipe(int seconds, int moons, RegistryAccess registries) {
        return shapelessFromJson("""
                {"ingredients":["minecraft:stick"],
                 "catalyst":"minecraft:diamond",
                 "result":{"id":"minecraft:emerald"},"time":%d,"moondust_cost":%d}
                """.formatted(seconds * 20, moons), registries);
    }

    // 1.21.11：@GameTest 注解在 1.21.5 被 Mojang 移除，改用 Fabric 的同名注解
    // （net.fabricmc.fabric.api.gametest.v1.GameTest）。其 structure() 默认即空结构，
    // 等价于旧版的 FabricGameTest.EMPTY_STRUCTURE，故不再传参。
    @GameTest
    public void recipes(GameTestHelper context) {
        // 用 RegistryAccess（HolderLookup.Provider 的子接口）——RegistryFriendlyByteBuf 要前者、
        // saveWithFullMetadata/loadWithComponents 要后者，声明成子接口两边都能传。
        RegistryAccess registries = context.getLevel().registryAccess();

        for (int[] example : new int[][]{{5, 5}, {10, 10}, {30, 16}, {60, 9}, {5, 0}}) {
            AltarShapelessRecipe recipe = recipe(example[0], example[1], registries);
            int total = 0;
            for (int tick = 0; tick < recipe.recipeTime(); tick++) {
                int cost = recipe.fuelUsage(tick);
                check(cost >= 0, "Fuel must never be created");
                total += cost;
            }
            check(total == example[1] * 800, "Exact moondust cost over full duration");

            // 网络往返：走 StreamCodec（1.21.1 无 write/read）
            RegistryFriendlyByteBuf packet = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
            RecipeSerializerRegister.ALTAR_SHAPELESS_RECIPE.streamCodec().encode(packet, recipe);
            AltarShapelessRecipe copy = RecipeSerializerRegister.ALTAR_SHAPELESS_RECIPE.streamCodec().decode(packet);
            check(copy.totalFuelUsage() == total && copy.recipeTime() == recipe.recipeTime(), "Shapeless network roundtrip");
            packet.release();
        }

        AltarShapelessRecipe recipe = recipe(30, 16, registries);
        Inventory inventory = new Inventory();
        inventory.setItem(0, new ItemStack(Items.STICK));
        check(!recipe.matches(input(inventory), null), "A catalyst is required");
        inventory.setItem(9, new ItemStack(Items.DIAMOND));
        check(recipe.matches(input(inventory), null), "Correct ingredients and catalyst match");
        inventory.setItem(0, new ItemStack(Items.DIRT));
        check(!recipe.matches(input(inventory), null), "Same occupied slot count must not match wrong ingredients");
        inventory.setItem(0, new ItemStack(Items.STICK));
        recipe.consumeInputs(inventory);
        check(inventory.getItem(0).isEmpty(), "Input is consumed");
        check(inventory.getItem(9).is(Items.DIAMOND), "Catalyst is never consumed");
        inventory.setItem(0, new ItemStack(Items.POWDER_SNOW_BUCKET));
        recipe.consumeInputs(inventory);
        check(inventory.getItem(0).is(Items.BUCKET), "Frost core returns its bucket");
        inventory.setItem(0, new ItemStack(Items.HONEY_BOTTLE, 2));
        check(recipe.getExtraOutput(inventory).get(0).is(Items.GLASS_BOTTLE), "Stacked container inputs return extra containers");

        AltarShapedRecipe shaped = shapedFromJson("""
                {"pattern":["SS"],"key":{"S":"minecraft:stick"},
                 "result":{"id":"minecraft:emerald"},"time":600,"moondust_cost":16}
                """, registries);
        RegistryFriendlyByteBuf shapedPacket = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
        RecipeSerializerRegister.ALTAR_SHAPED_RECIPE.streamCodec().encode(shapedPacket, shaped);
        AltarShapedRecipe shapedCopy = RecipeSerializerRegister.ALTAR_SHAPED_RECIPE.streamCodec().decode(shapedPacket);
        check(shapedCopy.totalFuelUsage() == 12800 && shapedCopy.recipeTime() == 600, "Shaped network roundtrip");
        shapedPacket.release();

        Inventory shapedInventory = new Inventory();
        shapedInventory.setItem(0, new ItemStack(Items.STICK));
        check(!shapedCopy.matches(input(shapedInventory), null), "Repeated ingredients require both slots");
        shapedInventory.setItem(1, new ItemStack(Items.STICK));
        check(shapedCopy.matches(input(shapedInventory), null), "Shaped ingredients match");

        // legacy：只用逐 tick fuel_cost、没有 moondust_cost 的旧数据包
        AltarShapelessRecipe legacy = shapelessFromJson("""
                {"ingredients":["minecraft:stick"],"result":{"id":"minecraft:emerald"},
                 "time":100,"fuel_cost":3}
                """, registries);
        check(legacy.fuelUsage(0) == 3 && legacy.totalFuelUsage() == 300, "Legacy per-tick fuel remains compatible");

        // Check the actual loaded data, not only the vanilla-item fixtures above.
        int upgradeMappings = 0;
        for (var entry : TrinketUtils.accessoryPowerRegistry.entrySet()) {
            if (!entry.getKey().getPath().endsWith("_plus")) continue;
            upgradeMappings++;
            for (var group : entry.getValue().layerPowerAddMap.values()) {
                for (var powers : group.values()) {
                    for (Identifier id : powers) {
                        check(PowerTypeRegistry.get(id) != null, "Missing upgraded power: " + id);
                    }
                }
            }
        }
        check(upgradeMappings == 10, "All data-driven accessory mappings loaded (spindle uses its projectile hook)");
        check(PowerTypeRegistry.get(Identifier.fromNamespaceAndPath("shape-shifter-curse", "form_familiar_fox_explosive_charm_paper")) != null,
                "Explosive charm action loaded");
        check(PowerTypeRegistry.get(Identifier.fromNamespaceAndPath("shape-shifter-curse", "form_snow_fox_bottled_snowfall_plus_tool")) != null,
                "Upgraded snowfall action loaded");

        AltarBlockEntity altar = new AltarBlockEntity(BlockPos.ZERO, RegCustomBlock.ALTAR_BLOCK.defaultBlockState());
        altar.setLevel(context.getLevel());
        ItemStack oldHook = new ItemStack(RegCustomItem.ATTACH_HOOK);
        // 1.21.1：ItemStack 的 setCustomName/hasCustomName 已移除，改用 CUSTOM_NAME 数据组件。
        oldHook.set(DataComponents.CUSTOM_NAME, Component.literal("Must not be inherited"));
        altar.setItem(0, oldHook);
        altar.setItem(9, new ItemStack(RegCustomItem.NIGHT_CATALYST_CORE));
        altar.tick(context.getLevel(), BlockPos.ZERO, altar.getBlockState(), altar);
        check(altar.progress == 0, "No progress without fuel");

        altar.setItem(10, new ItemStack(RegCustomItem.UNTREATED_MOONDUST, 16));
        for (int tick = 0; tick < 200; tick++) altar.tick(context.getLevel(), BlockPos.ZERO, altar.getBlockState(), altar);

        // 存档 / 读档：1.21.1 用 saveWithFullMetadata / loadWithComponents（旧的 createNbt/readNbt 已移除）
        // 1.21.11：loadWithComponents 改收 ValueInput（不再是 (CompoundTag, Provider)），
        // 用 TagValueInput 包一层 —— 与本仓库既有写法一致（如 apoli 的 PowerHolderComponentImpl）。
        var saved = altar.saveWithFullMetadata(registries);
        altar = new AltarBlockEntity(BlockPos.ZERO, RegCustomBlock.ALTAR_BLOCK.defaultBlockState());
        altar.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, registries, saved));
        altar.setLevel(context.getLevel());

        for (int tick = 200; tick < 599; tick++) altar.tick(context.getLevel(), BlockPos.ZERO, altar.getBlockState(), altar);
        check(altar.getItem(11).isEmpty(), "Upgrade takes the full 30 seconds");
        altar.tick(context.getLevel(), BlockPos.ZERO, altar.getBlockState(), altar);
        check(altar.getItem(11).is(RegCustomItem.ATTACH_HOOK_PLUS), "Correct standalone upgrade output");
        check(altar.getItem(11).get(DataComponents.CUSTOM_NAME) == null, "No input NBT inheritance");
        check(altar.fuelTime == 0 && altar.getItem(10).isEmpty(), "Exactly 16 moondust consumed");
        check(altar.getItem(9).is(RegCustomItem.NIGHT_CATALYST_CORE), "Actual catalyst retained");

        altar.setItem(0, new ItemStack(RegCustomItem.ATTACH_HOOK));
        check(altar.nowRecipe == null, "Do not overstack an upgraded accessory in the output slot");

        System.out.println("Altar recipe checks passed.");
        context.succeed();
    }
}
