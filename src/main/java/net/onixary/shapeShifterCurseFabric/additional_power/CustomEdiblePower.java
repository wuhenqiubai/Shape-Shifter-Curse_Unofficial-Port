package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.util.CustomEdibleUtils;

import java.util.List;

public class CustomEdiblePower extends Power {

    private final FoodProperties foodComponent;
    private final List<Identifier> ItemIdList;


    public CustomEdiblePower(PowerType<?> type, LivingEntity entity, SerializableData.Instance data) {
        super(type, entity);
        this.ItemIdList = data.get("item_id_list");
        FoodProperties.Builder foodComponentBuilder = new FoodProperties.Builder()
                .nutrition(data.getInt("hunger"))
                .saturationModifier(data.getFloat("saturation_modifier"));
        if (data.getBoolean("meat")) {
            // .meat() removed in 1.21 — 1.21 的 FoodComponent 已无 meat 属性，改由 ItemTags.WOLF_FOOD 处理
        }
        if (data.getBoolean("always_edible")) {
            foodComponentBuilder.alwaysEdible();
        }
        // snack and effect methods removed from FoodProperties.Builder in 1.21.11
        this.foodComponent = foodComponentBuilder.build();
    }

    public List<Identifier> getItemIdList() {
        return this.ItemIdList;
    }

    public FoodProperties getFoodComponent() {
        return this.foodComponent;
    }

    // 每Tick仅会运行一次 无论多少个Power
    public static void OnClientTick(Player player) {
        // 5s 更新1次 会导致悦灵变形到其他形态时还可以吃一个紫水晶碎片(在特定时机)
        if (player.tickCount % 100 == 0) {
            CustomEdibleUtils.ReloadPlayerCustomEdible(player);
            // 如果启用这个和服务器端的Logger后发现Server和Client的Logger同时输出 不用想肯定是Bug
            // ShapeShifterCurseFabric.LOGGER.info("Reload Player Custom Edible For {} In Client", player.getName().getString());
        }
    }

    // 每Tick仅会运行玩家数量次 无论多少个Power
    public static void OnServerTick(ServerPlayer player) {
        // 防止在单人游戏里运行两次
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            return;
        }
        // 5s 更新1次 会导致悦灵变形到其他形态时还可以吃一个紫水晶碎片(在特定时机)
        if (player.tickCount % 100 == 0) {
            CustomEdibleUtils.ReloadPlayerCustomEdible(player);
            // 如果启用这个和客户端的Logger后发现Server和Client的Logger同时输出 不用想肯定是Bug
            // ShapeShifterCurseFabric.LOGGER.info("Reload Player Custom Edible For {} In Server", player.getName().getString());
        }
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
                ShapeShifterCurseFabric.identifier("custom_edible"),
                new SerializableData()
                        .add("item_id_list", SerializableDataTypes.IDENTIFIERS, null)
                        .add("hunger", SerializableDataTypes.INT, 0)
                        .add("saturation_modifier", SerializableDataTypes.FLOAT, 0.0f)
                        .add("meat", SerializableDataTypes.BOOLEAN, false)
                        .add("always_edible", SerializableDataTypes.BOOLEAN, false)
                        .add("snack", SerializableDataTypes.BOOLEAN, false)
                        .add("status_effects", SerializableDataTypes.STATUS_EFFECT_INSTANCES, null),
                data -> (type, entity) -> new CustomEdiblePower(type, entity, data)
        );
    }
}