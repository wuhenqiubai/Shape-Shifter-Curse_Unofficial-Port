package net.onixary.shapeShifterCurseFabric.integration.origins.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;
import net.onixary.shapeShifterCurseFabric.integration.origins.util.OriginLootCondition;

// Enchantment loot tables can be added via JSON data files:
//   data/origins/loot_table/...

public class ModLoot {

    private static final Identifier DUNGEON_LOOT = Identifier.fromNamespaceAndPath("minecraft", "chests/simple_dungeon");
    private static final Identifier STRONGHOLD_LIBRARY = Identifier.fromNamespaceAndPath("minecraft", "chests/stronghold_library");
    private static final Identifier MINESHAFT = Identifier.fromNamespaceAndPath("minecraft", "chests/abandoned_mineshaft");
    private static final Identifier WATER_RUIN = Identifier.fromNamespaceAndPath("minecraft", "chests/underwater_ruin_small");

    // 上游把 OriginLootCondition 注册成战利品条件类型。1.20 的旧写法（LootConditionType + JsonSerializer）在 1.21
    // 已不可用 —— 条件改由 MapCodec 描述。OriginLootCondition 本身已按 1.21 风格移植好（自带 CODEC + TYPE），
    // 但一直没被注册，导致它是个死类、数据包也用不了 "type": "origins:origin"（战利品条件）。
    // 这里补上注册，注意用的是它自己的 TYPE（LootItemConditionType 内部包着 CODEC），不要再 new 一个。
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
//    }
}