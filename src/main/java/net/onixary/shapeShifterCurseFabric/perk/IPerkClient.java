package net.onixary.shapeShifterCurseFabric.perk;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public interface IPerkClient {
    public ResourceLocation getID();

    public default ResourceLocation getIcon() {
        ResourceLocation id = this.getID();
        String NameSpace = id.getNamespace();
        String Path = id.getPath();
        return ResourceLocation.fromNamespaceAndPath(NameSpace, "textures/perks/" + Path + ".png");
    }

    public default Component getName() {
        return IPerkClient.getDefaultName(this.getID());
    }

    public default Component getDesc() {
        return IPerkClient.getDefaultDesc(this.getID());
    }

    public static @NotNull Component getDefaultName(@NotNull ResourceLocation perkName) {
        String NameSpace = perkName.getNamespace();
        String Path = perkName.getPath();
        return Component.translatable("ssc_perk." + NameSpace + "." + Path + ".name");
    }

    public static @NotNull Component getDefaultDesc(@NotNull ResourceLocation perkName) {
        String NameSpace = perkName.getNamespace();
        String Path = perkName.getPath();
        return Component.translatable("ssc_perk." + NameSpace + "." + Path + ".desc");
    }
}
