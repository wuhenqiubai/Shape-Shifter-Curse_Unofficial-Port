package net.onixary.shapeShifterCurseFabric.player_animation.v3;

import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractAnimFSM {
    public Tuple<@Nullable Identifier, @NotNull Identifier> update(Player player, AnimSystem.AnimSystemData animSystemData) {
        Identifier NextFSMID = getNextFSM(player, animSystemData);
        if (NextFSMID == null) {
            return new Tuple<>(null, getStateID(player, animSystemData));
        } else {
            AbstractAnimFSM NextFSM = AnimRegistry.getAnimFSM(NextFSMID);
            if (NextFSM == null) {
                ShapeShifterCurseFabric.LOGGER.error("找不到动画控制状态机: {}", NextFSMID);
                return new Tuple<>(null, getStateID(player, animSystemData));
            }
            Tuple<@Nullable Identifier, @NotNull Identifier> NextFSMResult = NextFSM.update(player, animSystemData);
            if (NextFSMResult.getA() != null) {
                return NextFSMResult;
            }
            else {  // 单次跳转
                return new Tuple<>(NextFSMID, NextFSMResult.getB());
            }
        }
    }

	public abstract @Nullable Identifier getNextFSM(Player player, AnimSystem.AnimSystemData animSystemData);

    public abstract @NotNull Identifier getStateID(Player player, AnimSystem.AnimSystemData animSystemData);
}