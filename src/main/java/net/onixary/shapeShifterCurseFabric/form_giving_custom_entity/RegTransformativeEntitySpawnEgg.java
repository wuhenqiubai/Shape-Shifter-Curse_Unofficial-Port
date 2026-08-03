package net.onixary.shapeShifterCurseFabric.form_giving_custom_entity;

// FabricItemSettings removed in 1.21, use Item.Settings directly

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TypedEntityData;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import java.util.function.Function;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.T_AXOLOTL;
import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.T_BAT;
import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.T_OCELOT;
import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.T_SPIDER;
import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.T_WOLF;


public class RegTransformativeEntitySpawnEgg {
    private RegTransformativeEntitySpawnEgg(){}
    // 注册刷怪蛋
    //public static final Item T_BAT_SPAWN_EGG = new SpawnEggItem(
    //        T_BAT, 0x1F1F1F, 0x8B8B8B, new Item.Settings()
    //);

    // 1.21.11: SpawnEggItem(Properties) 从 DataComponents.ENTITY_DATA (TypedEntityData<EntityType>) 读实体类型，
    // 不设置则 getType 返回 null → 刷怪蛋放不出实体。必须给 properties 加 ENTITY_DATA component。
    public static final Item T_BAT_SPAWN_EGG = register("custom_bat_spawn_egg", props -> new SpawnEggItem(props.component(DataComponents.ENTITY_DATA, TypedEntityData.<EntityType<?>>of(T_BAT, new CompoundTag()))));

    public static final Item T_AXOLOTL_SPAWN_EGG = register("custom_axolotl_spawn_egg", props -> new SpawnEggItem(props.component(DataComponents.ENTITY_DATA, TypedEntityData.<EntityType<?>>of(T_AXOLOTL, new CompoundTag()))));

    public static final Item T_OCELOT_SPAWN_EGG = register("custom_ocelot_spawn_egg", props -> new SpawnEggItem(props.component(DataComponents.ENTITY_DATA, TypedEntityData.<EntityType<?>>of(T_OCELOT, new CompoundTag()))));

    public static final Item T_WOLF_SPAWN_EGG = register("custom_wolf_spawn_egg", props -> new SpawnEggItem(props.component(DataComponents.ENTITY_DATA, TypedEntityData.<EntityType<?>>of(T_WOLF, new CompoundTag()))));

    public static final Item T_SPIDER_SPAWN_EGG = register("custom_spider_spawn_egg", props -> new SpawnEggItem(props.component(DataComponents.ENTITY_DATA, TypedEntityData.<EntityType<?>>of(T_SPIDER, new CompoundTag()))));

    public static <T extends Item> T register(String path, Function<Item.Properties, T> factory) {
        ResourceKey<Item> key = ResourceKey.create(BuiltInRegistries.ITEM.key(), Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, path));
        T item = factory.apply(new Item.Properties().setId(key));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
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