package net.onixary.shapeShifterCurseFabric.player_form.ability;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface IFormLayerGroup {
    boolean isExists(@NotNull Player player);

    @NotNull Identifier getGroupID();

    void __setGroupID(@NotNull Identifier groupID);

    @NotNull List<Identifier> getLayers();

    void __setLayers(@NotNull List<Identifier> layers);

    default @NotNull Identifier transformLayerID(@NotNull Player player, @Nullable Identifier layerID) {
        if (layerID == null) {
            // 需要重载 可以实现一些特殊操作
            throw new NullPointerException("layerID is null");
        }
        return layerID;
    }

    // 给后续留的拓展接口 说不定未来客户端需要知道部分数据 反正目前不需要
    default void write(@NotNull FriendlyByteBuf packetByteBuf) {
        packetByteBuf.writeIdentifier(getGroupID());
        packetByteBuf.writeCollection(getLayers(), FriendlyByteBuf::writeIdentifier);
    }

    default void read(@NotNull FriendlyByteBuf packetByteBuf) {
        __setGroupID(packetByteBuf.readIdentifier());
        __setLayers(packetByteBuf.readCollection(ArrayList::new, FriendlyByteBuf::readIdentifier));
    }

    default void onAddGroup(@NotNull Player player, @NotNull Identifier newLayer) {
    }

    default void onRemoveGroup(@NotNull Player player, @NotNull Identifier oldLayer) {
    }
}