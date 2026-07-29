package net.onixary.shapeShifterCurseFabric.minion;

import net.minecraft.resources.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

// 或许应该把这几个Component合并到同一个RegClass
public class RegPlayerMinionComponent implements EntityComponentInitializer {
    public static final ComponentKey<PlayerMinionComponent> PLAYER_MINION_DATA = ComponentRegistry.getOrCreate(Identifier.fromNamespaceAndPath(MOD_ID, "player_minion_data"), PlayerMinionComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry entityComponentFactoryRegistry) {
        // 为 PlayerEntity 注册组件，并启用持久化
        entityComponentFactoryRegistry.registerForPlayers(
                PLAYER_MINION_DATA,
                player -> new PlayerMinionComponent(),
                RespawnCopyStrategy.ALWAYS_COPY
        );
    }
}