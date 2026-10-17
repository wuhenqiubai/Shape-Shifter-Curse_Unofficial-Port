package net.onixary.shapeShifterCurseFabric.entity;

import net.minecraft.world.entity.EntityDimensions;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.entity.projectile.WebBullet;

public class RegCustomEntity {
    public static final EntityType<WebBullet> WEB_BULLET = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ShapeShifterCurseFabric.identifier("web_bullet"),
            EntityType.Builder.<WebBullet>of((entityType, level) -> new WebBullet(entityType, level), MobCategory.MISC).sized(0.5F, 0.5F).build(
                ResourceKey.create(Registries.ENTITY_TYPE, ShapeShifterCurseFabric.identifier("web_bullet"))
            )
    );
    public static final EntityType<PotionCharmArrowEntity> POTION_CHARM_ARROW = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ShapeShifterCurseFabric.identifier("potion_charm_arrow"),
            FabricEntityTypeBuilder.create(MobCategory.MISC, PotionCharmArrowEntity::new).dimensions(EntityDimensions.fixed(0.5f, 0.5f)).trackable(64, 1).build(ResourceKey.create(Registries.ENTITY_TYPE, ShapeShifterCurseFabric.identifier("potion_charm_arrow"))));

    public static void init() {
    }
}