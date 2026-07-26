package net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateController;

import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.player_animation.AnimationHolder;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AbstractAnimStateController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimSystem;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

public class ConditionAnimController extends AbstractAnimStateController {
    private List<Tuple<BiFunction<Player, AnimSystem.AnimSystemData, Boolean>, AnimationHolder>> animConditionList;
    private final List<Tuple<BiFunction<Player, AnimSystem.AnimSystemData, Boolean>, AnimUtils.AnimationHolderData>> animConditionBuilder;
    private @Nullable AnimationHolder defaultAnimation;
    private final @NotNull AnimUtils.AnimationHolderData defaultAnimationData;

    public ConditionAnimController(List<Tuple<BiFunction<Player, AnimSystem.AnimSystemData, Boolean>, AnimUtils.AnimationHolderData>> animConditionBuilder, @NotNull AnimUtils.AnimationHolderData defaultAnimationData) {
        this.animConditionBuilder = animConditionBuilder;
        this.defaultAnimationData = AnimUtils.ensureAnimHolderDataNotNull(defaultAnimationData);
    }

    @Override
    public void registerAnim(Player player, AnimSystem.AnimSystemData data) {
        List<Tuple<BiFunction<Player, AnimSystem.AnimSystemData, Boolean>, AnimationHolder>> animConditionList = new LinkedList<>();
        for (Tuple<BiFunction<Player, AnimSystem.AnimSystemData, Boolean>, AnimUtils.AnimationHolderData> pair : this.animConditionBuilder) {
            animConditionList.add(new Tuple<>(pair.getA(), AnimUtils.ensureAnimHolderDataNotNull(pair.getB()).build()));
        }
        this.animConditionList = animConditionList;
        this.defaultAnimation = this.defaultAnimationData.build();
        super.registerAnim(player, data);
    }

    @Override
    public @Nullable AnimationHolder getAnimation(Player player, AnimSystem.AnimSystemData data) {
        for (Tuple<BiFunction<Player, AnimSystem.AnimSystemData, Boolean>, AnimationHolder> pair : this.animConditionList) {
            if (pair.getA().apply(player, data)) {
                return pair.getB();
            }
        }
        return this.defaultAnimation;
    }
}
