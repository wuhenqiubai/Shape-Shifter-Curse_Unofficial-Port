package net.onixary.shapeShifterCurseFabric.items;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potions;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.minion.mobs.ManaReservoirEntity;

public final class FamiliarFoxContent {
    public static final Item HEALING_CHARM = Registry.register(BuiltInRegistries.ITEM, ShapeShifterCurseFabric.identifier("healing_charm_paper"), new PotionCharmItem(Potions.STRONG_HEALING));
    public static final Item HARMING_CHARM = Registry.register(BuiltInRegistries.ITEM, ShapeShifterCurseFabric.identifier("harming_charm_paper"), new PotionCharmItem(Potions.STRONG_HARMING));
    public static final Item POISON_CHARM = Registry.register(BuiltInRegistries.ITEM, ShapeShifterCurseFabric.identifier("poison_charm_paper"), new PotionCharmItem(Potions.STRONG_POISON));
    public static final EntityType<ManaReservoirEntity> MANA_RESERVOIR = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            ShapeShifterCurseFabric.identifier("mana_reservoir"), FabricEntityTypeBuilder.create(MobCategory.MISC, ManaReservoirEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 0.6f)).build());
    public static final EntityType<PotionCharmArrowEntity> POTION_CHARM_ARROW = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            ShapeShifterCurseFabric.identifier("potion_charm_arrow"), FabricEntityTypeBuilder.create(MobCategory.MISC, PotionCharmArrowEntity::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f)).trackRangeBlocks(64).trackedUpdateRate(1).build());
    public static void register() {
        FabricDefaultAttributeRegistry.register(MANA_RESERVOIR, ManaReservoirEntity.attributes());
    }
}
