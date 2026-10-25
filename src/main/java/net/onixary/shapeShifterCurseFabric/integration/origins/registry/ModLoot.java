package net.onixary.shapeShifterCurseFabric.integration.origins.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;
import net.onixary.shapeShifterCurseFabric.integration.origins.util.OriginLootCondition;

public class ModLoot {

    private static final ResourceLocation DUNGEON_LOOT = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/simple_dungeon");
    private static final ResourceLocation STRONGHOLD_LIBRARY = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/stronghold_library");
    private static final ResourceLocation MINESHAFT = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/abandoned_mineshaft");
    private static final ResourceLocation WATER_RUIN = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/underwater_ruin_small");

    public static final LootItemConditionType ORIGIN_LOOT_CONDITION = Registry.register(
            BuiltInRegistries.LOOT_CONDITION_TYPE, Origins.identifier("origin"), OriginLootCondition.TYPE);

    public static void registerLootTables() {
        /*NbtCompound waterProtectionLevel1 = createEnchantmentTag(ModEnchantments.WATER_PROTECTION, 1);
        NbtCompound waterProtectionLevel2 = createEnchantmentTag(ModEnchantments.WATER_PROTECTION, 2);
        NbtCompound waterProtectionLevel3 = createEnchantmentTag(ModEnchantments.WATER_PROTECTION, 3);
        LootTableEvents.MODIFY.register(((resourceManager, lootManager, identifier, tableBuilder, source) -> {
            if (!source.isBuiltin()) {
                return;
            }
            if (DUNGEON_LOOT.equals(identifier)) {
                LootPool.Builder lootPool = new LootPool.Builder();
                lootPool.rolls(ConstantLootNumberProvider.create(1))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .weight(20)
                        .apply(SetNbtLootFunction.builder(waterProtectionLevel1)))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .weight(10)
                        .apply(SetNbtLootFunction.builder(waterProtectionLevel2)))
                    .with(EmptyEntry.builder().weight(80));
                tableBuilder.pool(lootPool);
            } else if (STRONGHOLD_LIBRARY.equals(identifier)) {
                LootPool.Builder lootPool = new LootPool.Builder();
                lootPool.rolls(ConstantLootNumberProvider.create(1))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .weight(20)
                        .apply(SetNbtLootFunction.builder(waterProtectionLevel2)))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .weight(10)
                        .apply(SetNbtLootFunction.builder(waterProtectionLevel3)))
                    .with(EmptyEntry.builder().weight(80));
                tableBuilder.pool(lootPool);
            } else if (MINESHAFT.equals(identifier)) {
                LootPool.Builder lootPool = new LootPool.Builder();
                lootPool.rolls(ConstantLootNumberProvider.create(1))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .weight(20)
                        .apply(SetNbtLootFunction.builder(waterProtectionLevel1)))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .weight(5)
                        .apply(SetNbtLootFunction.builder(waterProtectionLevel2)))
                    .with(EmptyEntry.builder().weight(90));
                tableBuilder.pool(lootPool);
            } else if (WATER_RUIN.equals(identifier)) {
                LootPool.Builder lootPool = new LootPool.Builder();
                lootPool.rolls(ConstantLootNumberProvider.create(1))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .weight(10)
                        .apply(SetNbtLootFunction.builder(waterProtectionLevel1)))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .weight(20)
                        .apply(SetNbtLootFunction.builder(waterProtectionLevel2)))
                    .with(EmptyEntry.builder().weight(110));
                tableBuilder.pool(lootPool);
            }
        }));*/
    }

//    private static NbtCompound createEnchantmentTag(Enchantment enchantment, int level) {
//        EnchantmentLevelEntry entry = new EnchantmentLevelEntry(enchantment, level);
//        return EnchantedBookItem.forEnchantment(entry).getNbt();
}