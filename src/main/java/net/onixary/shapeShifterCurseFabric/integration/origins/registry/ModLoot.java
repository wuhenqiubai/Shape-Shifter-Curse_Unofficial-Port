package net.onixary.shapeShifterCurseFabric.integration.origins.registry;

import net.minecraft.resources.Identifier;

// Enchantment loot tables can be added via JSON data files:
//   data/origins/loot_table/...

public class ModLoot {

    private static final Identifier DUNGEON_LOOT = Identifier.fromNamespaceAndPath("minecraft", "chests/simple_dungeon");
    private static final Identifier STRONGHOLD_LIBRARY = Identifier.fromNamespaceAndPath("minecraft", "chests/stronghold_library");
    private static final Identifier MINESHAFT = Identifier.fromNamespaceAndPath("minecraft", "chests/abandoned_mineshaft");
    private static final Identifier WATER_RUIN = Identifier.fromNamespaceAndPath("minecraft", "chests/underwater_ruin_small");

// TODO:上游没有用到，但我后面还是重新写一下吧
//    public static final LootConditionType ORIGIN_LOOT_CONDITION = registerLootCondition("origin", new OriginLootCondition.Serializer());
//    private static LootConditionType registerLootCondition(String path, JsonSerializer<? extends LootCondition> serializer) {
//        return Registry.register(Registries.LOOT_CONDITION_TYPE, Origins.identifier(path), new LootConditionType(serializer));
//    }

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
//    }
}