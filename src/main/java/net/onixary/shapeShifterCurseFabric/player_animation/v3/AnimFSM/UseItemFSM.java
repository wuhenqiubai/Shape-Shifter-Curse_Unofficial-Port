package net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimFSM;

import net.onixary.shapeShifterCurseFabric.player_animation.v3.AbstractAnimFSM;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimRegistries.*;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class UseItemFSM extends AbstractAnimFSM {
    @Override
    public @Nullable ResourceLocation getNextFSM(Player player, AnimSystem.AnimSystemData animSystemData) {
        if (!(player.isUsingItem() || player.swinging)) {
            return FSM_ON_GROUND;
        }
        return null;
    }

    @Override
    public @NotNull ResourceLocation getStateID(Player player, AnimSystem.AnimSystemData animSystemData) {
        @Nullable ResourceLocation UniversalStateResult = FSMUtils.ProcessUniversalAnim(player, animSystemData);
        if (UniversalStateResult != null) {
            return UniversalStateResult;
        }
        if (player.isUsingItem()) {
            // 举盾防御优先于普通使用物品判定 1.20.1的isBlocking仅在举盾时为true
            if (player.isBlocking()) {
                return ANIM_STATE_BLOCK_SHIELD;
            }
            return ANIM_STATE_USE_ITEM;
        }
        // 到这里player.handSwinging一定是true
        if (animSystemData.ContinueSwingAnimCounter >= 10) {
            return ANIM_STATE_MINING;
        }
        return ANIM_STATE_ATTACK;
    }
}
