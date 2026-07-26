package net.onixary.shapeShifterCurseFabric.player_animation.v3;

import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.player_animation.AnimationHolder;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractAnimStateController {
    public abstract @Nullable AnimationHolder getAnimation(Player player, AnimSystem.AnimSystemData data);

    private boolean isRegistered = false;

    public boolean isRegistered(Player player, AnimSystem.AnimSystemData data) {
        return isRegistered;
    }

    public void registerAnim(Player player, AnimSystem.AnimSystemData data) {
        isRegistered = true;
    }

    // 仅用于特殊系统 比如TransformController 其他系统请勿使用(没对应逻辑)
    public boolean isEnabled(Player player, AnimSystem.AnimSystemData data) {
        return true;
    }
}
