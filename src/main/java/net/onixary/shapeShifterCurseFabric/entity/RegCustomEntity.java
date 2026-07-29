package net.onixary.shapeShifterCurseFabric.entity;

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

    public static void init() {
    }
}