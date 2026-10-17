package net.onixary.shapeShifterCurseFabric.perk;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.onixary.shapeShifterCurseFabric.util.util.ISprite;
import org.jetbrains.annotations.NotNull;

public interface IPerkClient {
    public Identifier getID();

    public default ISprite getIcon() {
        return RegPerks.FALLBACK_PERK_ICON;
    }

    public default Component getName() {
        return IPerkClient.getDefaultName(this.getID());
    }

    public default Component getDesc() {
        return IPerkClient.getDefaultDesc(this.getID());
    }

    public static @NotNull Component getDefaultName(@NotNull Identifier perkName) {
        String NameSpace = perkName.getNamespace();
        String Path = perkName.getPath();
        return Component.translatable("ssc_perk." + NameSpace + "." + Path + ".name");
    }

    public static @NotNull Component getDefaultDesc(@NotNull Identifier perkName) {
        String NameSpace = perkName.getNamespace();
        String Path = perkName.getPath();
        return Component.translatable("ssc_perk." + NameSpace + "." + Path + ".desc");
    }
}
