package net.onixary.shapeShifterCurseFabric.integration.origins.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;
import net.onixary.shapeShifterCurseFabric.integration.origins.entity.EnderianPearlEntity;

public class ModEntities {

    private static final ResourceKey<EntityType<?>> ENDERIAN_PEARL_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Origins.MODID, "enderian_pearl"));

    public static final EntityType<EnderianPearlEntity> ENDERIAN_PEARL;

    static {
        ENDERIAN_PEARL = EntityType.Builder.<EnderianPearlEntity>of(EnderianPearlEntity::new, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .clientTrackingRange(64)
                .updateInterval(10)
                .build(ENDERIAN_PEARL_KEY);
    }

    public static void register() {
        Registry.register(BuiltInRegistries.ENTITY_TYPE, ENDERIAN_PEARL_KEY, ENDERIAN_PEARL);
    }
}
