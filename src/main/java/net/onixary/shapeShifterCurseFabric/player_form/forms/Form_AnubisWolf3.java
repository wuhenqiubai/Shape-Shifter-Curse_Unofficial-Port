package net.onixary.shapeShifterCurseFabric.player_form.forms;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AbstractAnimStateController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateEnum;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimSystem;
import net.onixary.shapeShifterCurseFabric.player_form.NormalForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Form_AnubisWolf3 extends NormalForm {
    public Form_AnubisWolf3(Identifier formID) {
        super(formID);
    }

    public @Nullable AbstractAnimStateController getAnimStateController(Player player, AnimSystem.AnimSystemData animSystemData, @NotNull Identifier animStateID) {
        @Nullable AnimStateEnum animStateEnum = AnimStateEnum.getStateEnum(animStateID);
        if (animStateEnum != null) {
            return switch (animStateEnum) {
                case ANIM_STATE_SLEEP -> Form_FeralBase.SLEEP_CONTROLLER;
                case ANIM_STATE_CLIMB -> Form_FeralBase.CLIMB_CONTROLLER;
                case ANIM_STATE_FALL -> Form_FeralBase.FALL_CONTROLLER;
                case ANIM_STATE_JUMP -> Form_FeralBase.JUMP_CONTROLLER;
                case ANIM_STATE_RIDE -> Form_SnowFox3.RIDE_CONTROLLER;
                case ANIM_STATE_SWIM -> Form_FeralBase.SWIM_CONTROLLER;
                case ANIM_STATE_USE_ITEM -> Form_FeralBase.USE_ITEM_CONTROLLER;
                case ANIM_STATE_WALK -> Form_FeralBase.WALK_CONTROLLER;
                case ANIM_STATE_SPRINT -> Form_FeralBase.SPRINT_CONTROLLER;
                case ANIM_STATE_IDLE -> Form_FeralBase.IDLE_CONTROLLER;
                case ANIM_STATE_MINING -> Form_FeralBase.MINING_CONTROLLER;
                case ANIM_STATE_ATTACK -> Form_FeralBase.ATTACK_CONTROLLER;
                case ANIM_STATE_FLYING, ANIM_STATE_FALL_FLYING -> Form_FeralBase.FALL_FLYING_CONTROLLER;
                default -> Form_FeralBase.IDLE_CONTROLLER;
            };
        }
        return super.getAnimStateController(player, animSystemData, animStateID);
    }
}