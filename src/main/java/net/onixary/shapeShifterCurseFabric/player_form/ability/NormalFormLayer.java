package net.onixary.shapeShifterCurseFabric.player_form.ability;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NormalFormLayer implements IFormLayer {
    private ResourceLocation layerID = null;
    private List<ResourceLocation> powerID = new ArrayList<>();

    @Override
    public @NotNull ResourceLocation getID() {
        return layerID;
    }

    public NormalFormLayer setID(@NotNull ResourceLocation new_id) {
        this.layerID = new_id;
        return this;
    }

    @Override
    public void __setID(@NotNull ResourceLocation id) {
        this.setID(id);
    }

    @Override
    public @NotNull List<ResourceLocation> getPowerID(@Nullable Player player) {
        return powerID;
    }

    public NormalFormLayer setPower(@NotNull List<ResourceLocation> powerIDList) {
        this.powerID = powerIDList;
        return this;
    }

    public NormalFormLayer setPower(@NotNull ResourceLocation... powerID) {
        this.powerID.clear();
        Collections.addAll(this.powerID, powerID);
        return this;
    }

    @Override
    public void __setPowerID(@NotNull List<ResourceLocation> powerIDList) {
        this.setPower(powerIDList);
    }

    /*
    {
        "load_priority": 0,      // 加载数据包时读取 最后仅会执行一次fromJson
        "id": "ssc:layer_id",    // 用于覆盖由文件名自动获取的ID 没什么用
        "power": [
            "ssc:power_1",
            "ssc:power_2",
            ...
        ]
    }
    */

    // 数据包用 后续可能会把Layers给迁移到硬编码(差不多和形态系统一样 主硬编码 副数据包)
    public static NormalFormLayer fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json) {
        NormalFormLayer layer = new NormalFormLayer();
        if (json.has("id")) {
            layer.setID(ResourceLocation.parse(json.get("id").getAsString()));
        } else {
            layer.setID(id);
        }
        JsonObject powerJson = json.getAsJsonObject("power");
        List<ResourceLocation> powerID = new ArrayList<>();
        for (String key : powerJson.keySet()) {
            powerID.add(ResourceLocation.parse(key));
        }
        layer.setPower(powerID);
        return layer;
    }
}