package net.onixary.shapeShifterCurseFabric.perk;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.onixary.shapeShifterCurseFabric.util.util.ISprite;
import org.jetbrains.annotations.Nullable;

public class VirtualPerk implements IPerkClient {
    public final Identifier perkID;
    public final ISprite icon;

    public VirtualPerk(Identifier perkID, ISprite icon) {
        this.perkID = perkID;
        this.icon = icon;
    }

    @Override
    public Identifier getID() {
        return perkID;
    }

    @Override
    public ISprite getIcon() {
        return icon;
    }

    @Override
    public @Nullable Component getName() {
        return Component.empty();
    }

    @Override
    public @Nullable Component getDesc() {
        return Component.empty();
    }
}
