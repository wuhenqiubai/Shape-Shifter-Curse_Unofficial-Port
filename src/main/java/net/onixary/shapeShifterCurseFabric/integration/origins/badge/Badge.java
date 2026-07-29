package net.onixary.shapeShifterCurseFabric.integration.origins.badge;

import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.registry.DataObject;
import io.github.apace100.calio.registry.DataObjectFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import java.util.List;

public interface Badge extends DataObject<Badge> {

    Identifier spriteId();

    boolean hasTooltip();

    @Environment(EnvType.CLIENT)
    List<ClientTooltipComponent> getTooltipComponents(PowerType<?> powerType, int widthLimit, float time, Font textRenderer);

    SerializableData.Instance toData(SerializableData.Instance instance);

    BadgeFactory getBadgeFactory();

    @Override
    default DataObjectFactory<Badge> getFactory() {
        return this.getBadgeFactory();
    }

    default void writeBuf(FriendlyByteBuf buf) {
        DataObjectFactory<Badge> factory = this.getFactory();
        buf.writeIdentifier(this.getBadgeFactory().id());
        factory.getData().write((RegistryFriendlyByteBuf) buf, factory.toData(this));
    }

}