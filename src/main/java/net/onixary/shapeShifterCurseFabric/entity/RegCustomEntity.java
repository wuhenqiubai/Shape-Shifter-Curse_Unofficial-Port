package net.onixary.shapeShifterCurseFabric.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.entity.projectile.WebBullet;

public class RegCustomEntity {
    public static final EntityType<WebBullet> WEB_BULLET = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ShapeShifterCurseFabric.identifier("web_bullet"),
            FabricEntityTypeBuilder.<WebBullet>create(MobCategory.MISC, WebBullet::new).dimensions(EntityDimensions.fixed(0.5f, 0.5f)).trackRangeChunks(10).trackedUpdateRate(1).build()
    );

    public static void init() {
    }
}
