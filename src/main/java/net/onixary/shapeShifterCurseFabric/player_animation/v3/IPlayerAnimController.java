package net.onixary.shapeShifterCurseFabric.player_animation.v3;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IPlayerAnimController {
    @Nullable ResourceLocation shape_shifter_curse$getPowerAnimationID();

    int shape_shifter_curse$getPowerAnimationCount();

    int shape_shifter_curse$getPowerAnimationTime();

    void shape_shifter_curse$playAnimationWithCount(@NotNull ResourceLocation id, int PlayCount);

    void shape_shifter_curse$playAnimationWithTime(@NotNull ResourceLocation id, int Time);

    void shape_shifter_curse$playAnimationLoop(@NotNull ResourceLocation id);

    void shape_shifter_curse$stopAnimation();

    void shape_shifter_curse$animationDoneCallBack(@NotNull ResourceLocation id);

    void shape_shifter_curse$setAnimationData(@Nullable ResourceLocation id, int count, int time);

}
