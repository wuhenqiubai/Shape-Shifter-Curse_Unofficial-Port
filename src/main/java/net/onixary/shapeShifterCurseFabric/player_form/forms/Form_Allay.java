package net.onixary.shapeShifterCurseFabric.player_form.forms;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AbstractAnimStateController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateControllerDP.OneAnimController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateControllerDP.WithSneakAnimController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateEnum;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimSystem;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimUtils;
import net.onixary.shapeShifterCurseFabric.player_form.NormalForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Form_Allay extends NormalForm {
    public Form_Allay(Identifier formID) {
        super(formID);
    }

    public static final AbstractAnimStateController WALK_CONTROLLER = new WithSneakAnimController(new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("allay_sp_moving")), new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("allay_sp_sneaking_walk")));
    public static final AbstractAnimStateController SPRINT_CONTROLLER = new WithSneakAnimController(new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("allay_sp_run")), new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("allay_sp_sneaking_walk")));
    public static final AbstractAnimStateController IDLE_CONTROLLER = new WithSneakAnimController(new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("allay_sp_idle")), new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("allay_sp_sneaking")));
    public static final AbstractAnimStateController MINING_CONTROLLER = new OneAnimController(new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("allay_sp_digging")));
    public static final AbstractAnimStateController ATTACK_CONTROLLER = new OneAnimController(new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("allay_sp_attack")));
    public static final AbstractAnimStateController FLYING_CONTROLLER = new OneAnimController(new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("allay_sp_fly")));

    public @Nullable AbstractAnimStateController getAnimStateController(Player player, AnimSystem.AnimSystemData animSystemData, @NotNull Identifier animStateID) {
        @Nullable AnimStateEnum animStateEnum = AnimStateEnum.getStateEnum(animStateID);
        if (animStateEnum != null) {
            return switch (animStateEnum) {
                case ANIM_STATE_WALK -> WALK_CONTROLLER;
                case ANIM_STATE_SPRINT -> SPRINT_CONTROLLER;
                case ANIM_STATE_IDLE -> IDLE_CONTROLLER;
                case ANIM_STATE_MINING -> MINING_CONTROLLER;
                case ANIM_STATE_ATTACK -> ATTACK_CONTROLLER;
                case ANIM_STATE_JUMP, ANIM_STATE_FALL, ANIM_STATE_FALL_FLYING, ANIM_STATE_FLYING -> FLYING_CONTROLLER;
                default -> WALK_CONTROLLER;
            };
        }
        return super.getAnimStateController(player, animSystemData, animStateID);
    }
}