package net.onixary.shapeShifterCurseFabric.player_form.forms;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.*;

import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateControllerDP.RideAnimController;
import net.onixary.shapeShifterCurseFabric.player_form.NormalForm;
import net.onixary.shapeShifterCurseFabric.player_form.PlayerFormBodyType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Form_FamiliarFox3 extends NormalForm {
    public Form_FamiliarFox3(Identifier formID) {
        super(formID);
    }

    public static final AbstractAnimStateController RIDE_CONTROLLER = new RideAnimController(new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("familiar_fox_3_riding"), 1.0f, 6, "EASE_IN_OUT_QUAD"), new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("form_feral_common_sneak_idle"), 1.0f, 6));

    public @Nullable AbstractAnimStateController getAnimStateController(PlayerEntity player, AnimSystem.AnimSystemData animSystemData, @NotNull Identifier animStateID) {
        @Nullable AnimStateEnum animStateEnum = AnimStateEnum.getStateEnum(animStateID);
        if (animStateEnum != null) {
	        return switch (animStateEnum) {
		        case ANIM_STATE_SLEEP -> Form_FeralBase.SLEEP_CONTROLLER;
		        case ANIM_STATE_CLIMB -> Form_FeralBase.CLIMB_CONTROLLER;
		        case ANIM_STATE_FALL -> Form_FeralBase.FALL_CONTROLLER;
		        case ANIM_STATE_JUMP -> Form_FeralBase.JUMP_CONTROLLER;
		        case ANIM_STATE_RIDE -> RIDE_CONTROLLER;
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
