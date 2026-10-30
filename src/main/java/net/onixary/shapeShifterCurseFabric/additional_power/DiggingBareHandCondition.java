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

        // 修了2个小时(反正大于1个小时) 这Bug真难修(也不算是Bug 毕竟是那个Mod的问题 也就得亏那个Field用处不大 要是需要在挖掘时给Buff 就只能标记为不兼容了)
        // https://github.com/hammertater/treechop 会导致挖掘后不会触发attackBlock 导致虽然在破坏方块中但是isBreakingBlock却为False
        // 不过modify_break_speed本身只会在破坏方块时生效 而barehand_digging的Condition仅蝙蝠空手加速Power里使用 所以删除这个检查应该没问题

        // if (playerEntity instanceof ServerPlayerEntity serverPlayerEntity) {
        //     if (!serverPlayerEntity.interactionManager.mining) {
        //         return false;
        //     }
        // } else if (playerEntity instanceof ClientPlayerEntity) {
        //     ClientPlayerInteractionManager interactionManager = MinecraftClient.getInstance().interactionManager;
        //     if (interactionManager == null || !interactionManager.isBreakingBlock()) {
        //         return false;
        //     }
        // } else {
        //     return false;
        // }

	    ItemStack held = playerEntity.getInventory().getSelectedItem();
            return !held.has(net.minecraft.core.component.DataComponents.TOOL);

    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
                ShapeShifterCurseFabric.identifier("can_barehand_digging"),
                new SerializableData(),
                DiggingBareHandCondition::condition
        );
    }

}
