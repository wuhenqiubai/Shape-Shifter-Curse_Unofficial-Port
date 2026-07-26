package net.onixary.shapeShifterCurseFabric.player_form.ability;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface IFormLayerGroup {
    boolean isExists(@NotNull Player player);

    @NotNull ResourceLocation getGroupID();

    void __setGroupID(@NotNull ResourceLocation groupID);

    @NotNull List<ResourceLocation> getLayers();

    void __setLayers(@NotNull List<ResourceLocation> layers);

    default @NotNull ResourceLocation transformLayerID(@NotNull Player player, @Nullable ResourceLocation layerID) {
        if (layerID == null) {
            // 需要重载 可以实现一些特殊操作
            throw new NullPointerException("layerID is null");
        }
        return layerID;
    }

    // 给后续留的拓展接口 说不定未来客户端需要知道部分数据 反正目前不需要
    default void write(@NotNull FriendlyByteBuf packetByteBuf) {
        packetByteBuf.writeResourceLocation(getGroupID());
        packetByteBuf.writeCollection(getLayers(), FriendlyByteBuf::writeResourceLocation);
    }

    default void read(@NotNull FriendlyByteBuf packetByteBuf) {
        __setGroupID(packetByteBuf.readResourceLocation());
        __setLayers(packetByteBuf.readCollection(ArrayList::new, FriendlyByteBuf::readResourceLocation));
    }

    default void onAddGroup(@NotNull Player player, @NotNull ResourceLocation newLayer) {
    }

    default void onRemoveGroup(@NotNull Player player, @NotNull ResourceLocation oldLayer) {
    }
}