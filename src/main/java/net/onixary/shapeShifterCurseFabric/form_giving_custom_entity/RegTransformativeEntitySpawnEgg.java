package net.onixary.shapeShifterCurseFabric.form_giving_custom_entity;

// FabricItemSettings removed in 1.21, use Item.Settings directly

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.T_AXOLOTL;
import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.T_BAT;

public class RegTransformativeEntitySpawnEgg {
    private RegTransformativeEntitySpawnEgg(){}
    // 注册刷怪蛋
    //public static final Item T_BAT_SPAWN_EGG = new SpawnEggItem(
    //        T_BAT, 0x1F1F1F, 0x8B8B8B, new Item.Settings()
    //);

    public static final Item T_BAT_SPAWN_EGG = register("custom_bat_spawn_egg", new SpawnEggItem(
            T_BAT,  0x8B8B8B, 0x1F1F1F,new Item.Properties()
    ));

    public static final Item T_AXOLOTL_SPAWN_EGG = register("custom_axolotl_spawn_egg", new SpawnEggItem(
            T_AXOLOTL,  0x8B8B8B,0xe4a7ae, new Item.Properties()
    ));

    public static final Item T_OCELOT_SPAWN_EGG = register("custom_ocelot_spawn_egg", new SpawnEggItem(
            ShapeShifterCurseFabric.T_OCELOT,  0x8B8B8B,0xfc801d, new Item.Properties()
    ));

    public static final Item T_WOLF_SPAWN_EGG = register("custom_wolf_spawn_egg", new SpawnEggItem(
            ShapeShifterCurseFabric.T_WOLF,  0x8B8B8B,0xffd355, new Item.Properties()
    ));

    public static final Item T_SPIDER_SPAWN_EGG = register("custom_spider_spawn_egg", new SpawnEggItem(
            ShapeShifterCurseFabric.T_SPIDER,  0x8B8B8B,0xff9060, new Item.Properties()
    ));

    public static <T extends Item> T register(String path, T item) {
        return Registry.register(BuiltInRegistries.ITEM, ResourceKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, path)), item);
    }

    public static void initialize() {
        //Registry.register(Registries.ITEM, RegistryKey.of(Registries.ITEM.getKey(), Identifier.of(ShapeShifterCurseFabric.MOD_ID, "custom_bat_spawn_egg")), T_BAT_SPAWN_EGG);
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(content -> {
            content.accept(T_BAT_SPAWN_EGG);
            content.accept(T_AXOLOTL_SPAWN_EGG);
            content.accept(T_OCELOT_SPAWN_EGG);
            content.accept(T_WOLF_SPAWN_EGG);
            content.accept(T_SPIDER_SPAWN_EGG);
        });
    }
}
