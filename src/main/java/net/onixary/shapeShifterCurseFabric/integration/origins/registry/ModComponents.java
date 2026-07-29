package net.onixary.shapeShifterCurseFabric.integration.origins.registry;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;
import net.onixary.shapeShifterCurseFabric.integration.origins.component.OriginComponent;
import net.onixary.shapeShifterCurseFabric.integration.origins.component.PlayerOriginComponent;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

public class ModComponents implements EntityComponentInitializer {

    public static final ComponentKey<OriginComponent> ORIGIN;

    static {
        ORIGIN = ComponentRegistry.getOrCreate(Identifier.fromNamespaceAndPath(Origins.MODID, "origin"), OriginComponent.class);
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(Player.class, ORIGIN).after(PowerHolderComponent.KEY).respawnStrategy(RespawnCopyStrategy.CHARACTER).end(PlayerOriginComponent::new);
    }
}