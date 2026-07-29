package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

public class DiggingBareHandCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        if (!(entity instanceof Player playerEntity)) {
            return false;
        }

        if (playerEntity instanceof ServerPlayer serverPlayerEntity) {
            if (!serverPlayerEntity.gameMode.isDestroyingBlock) {
                return false;
            }

        } else if (playerEntity instanceof LocalPlayer) {
            MultiPlayerGameMode interactionManager = Minecraft.getInstance().gameMode;
            if (interactionManager == null || !interactionManager.isDestroying()) {
                return false;
            }
        } else {
            return false;
        }

        // getMiningLevel removed in 1.21; check tool component instead of TieredItem
        if (playerEntity.getInventory().isEmpty()) {
            return true;  // bare hand
        } else {
            ItemStack held = playerEntity.getInventory().getSelectedItem();
            return !held.has(net.minecraft.core.component.DataComponents.TOOL);
        }
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
                ShapeShifterCurseFabric.identifier("barehand_digging"),
                new SerializableData(),
                DiggingBareHandCondition::condition
        );
    }
}
