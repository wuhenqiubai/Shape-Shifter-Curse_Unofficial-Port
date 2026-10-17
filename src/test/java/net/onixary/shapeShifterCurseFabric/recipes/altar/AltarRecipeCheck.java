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

public final class AltarRecipeCheck {
    private static final Identifier ID = Identifier.fromNamespaceAndPath("test", "altar");

    private static final class Inventory extends SimpleContainer implements WorldlyContainer {
        Inventory() { super(12); }

        @Override
        public int[] getSlotsForFace(Direction side) { return new int[]{0, 1, 9}; }

        @Override
        public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) { return true; }

        @Override
        public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) { return true; }
    }

    private static AltarRecipeInput input(Inventory inventory) {
        return new AltarRecipeInput(inventory.getItems());
    }

    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

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

    @GameTest
    public void recipes(GameTestHelper context) {
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
        oldHook.set(DataComponents.CUSTOM_NAME, Component.literal("Must not be inherited"));
        altar.setItem(0, oldHook);
        altar.setItem(9, new ItemStack(RegCustomItem.NIGHT_CATALYST_CORE));
        altar.tick(context.getLevel(), BlockPos.ZERO, altar.getBlockState(), altar);
        check(altar.progress == 0, "No progress without fuel");

        altar.setItem(10, new ItemStack(RegCustomItem.UNTREATED_MOONDUST, 16));
        for (int tick = 0; tick < 200; tick++) altar.tick(context.getLevel(), BlockPos.ZERO, altar.getBlockState(), altar);

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
