package net.onixary.shapeShifterCurseFabric.player_form.skin;

import net.minecraft.resources.Identifier;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

public class RegPlayerSkinComponent  implements EntityComponentInitializer {
    public static final ComponentKey<PlayerSkinComponent> SKIN_SETTINGS =
            ComponentRegistry.getOrCreate(Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "skin_settings"), PlayerSkinComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(
                SKIN_SETTINGS,
                player -> new PlayerSkinComponent(),
                RespawnCopyStrategy.ALWAYS_COPY
        );
    }
}