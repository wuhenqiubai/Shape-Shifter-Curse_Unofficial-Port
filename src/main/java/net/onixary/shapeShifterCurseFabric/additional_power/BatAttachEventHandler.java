package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public class BatAttachEventHandler {

    public static void register() {
        // 处理右键点击方块
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {

            if (world.isClientSide()) {
                return InteractionResult.PASS;
            }

            if (hand != InteractionHand.MAIN_HAND) {
                return InteractionResult.PASS;
            }

            // 获取玩家的 BatBlockAttachPower
            BatBlockAttachPower attachPower = getBatAttachPower(player);
            if (attachPower == null) {
                return InteractionResult.PASS;
            }

            // 如果已经吸附，取消吸附
            if (attachPower.isAttached()) {
                attachPower.handleRightClick(player);
                return InteractionResult.SUCCESS;
            }

            // 尝试吸附
            if (attachPower.tryAttach(player, hitResult)) {
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        });

        // 处理方块破坏事件
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClientSide()) {
                return;
            }
            BatBlockAttachPower attachPower = getBatAttachPower(player);
            if (attachPower != null && attachPower.isAttached()) {
                BlockPos attachedPos = attachPower.getAttachedBlockPos();
                if (attachedPos != null && attachedPos.equals(pos)) {
                    attachPower.detach(player, false);

                }
            }
        });

        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, from, to) -> {
            BatBlockAttachPower attachPower = getBatAttachPower(player);
            if (attachPower != null && attachPower.isAttached()) {
                attachPower.detach(player, false);
            }
        });
    }

    static BatBlockAttachPower getBatAttachPower(Player player) {
        return PowerHolderComponent.getPowers(player, BatBlockAttachPower.class)
                .stream()
                .findFirst()
                .orElse(null);
    }
}
