package net.onixary.shapeShifterCurseFabric.player_form.forms;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AbstractAnimStateController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateControllerDP.OneAnimController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateControllerDP.RideAnimController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateControllerDP.WithSneakAnimController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateEnum;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimSystem;

import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimUtils;
import net.onixary.shapeShifterCurseFabric.player_form.PlayerFormBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Form_Bat2 extends PlayerFormBase {
    public Form_Bat2(Identifier formID) {
        super(formID);
    }

    public static final AbstractAnimStateController JUMP_CONTROLLER = new WithSneakAnimController(new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("bat_2_jump"), 1.0f, 6), null);
    public static final AbstractAnimStateController IDLE_CONTROLLER = new WithSneakAnimController(null, new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("bat_1_sneak_idle"), 1.0f, 6));
    public static final AbstractAnimStateController RIDE_CONTROLLER = new RideAnimController(new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("bat_2_riding"), 1.0f, 6, "EASE_IN_OUT_QUAD"), new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("bat_1_sneak_idle"), 1.0f, 6));
    public static final AbstractAnimStateController FALL_CONTROLLER = new OneAnimController(new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("bat_2_slow_falling"), 1.0f, 6));
    public static final AbstractAnimStateController FLYING_CONTROLLER = new OneAnimController(new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("bat_2_slow_falling"), 1.0f, 6));
    public static final AbstractAnimStateController MINING_CONTROLLER = new OneAnimController(new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("bat_2_digging"), 1.0f, 6));
    public static final AbstractAnimStateController ATTACK_CONTROLLER = new OneAnimController(new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("bat_2_attack"), 1.0f, 6));

    @Override
    public @Nullable AbstractAnimStateController getAnimStateController(PlayerEntity player, AnimSystem.AnimSystemData animSystemData, @NotNull Identifier animStateID) {
        @Nullable AnimStateEnum animStateEnum = AnimStateEnum.getStateEnum(animStateID);
        if (animStateEnum != null) {
	        return switch (animStateEnum) {
		        case ANIM_STATE_JUMP -> JUMP_CONTROLLER;
		        case ANIM_STATE_IDLE -> IDLE_CONTROLLER;
		        case ANIM_STATE_RIDE -> RIDE_CONTROLLER;
		        case ANIM_STATE_FALL -> FALL_CONTROLLER;
		        case ANIM_STATE_FLYING -> FLYING_CONTROLLER;
		        case ANIM_STATE_MINING -> MINING_CONTROLLER;
		        case ANIM_STATE_ATTACK -> ATTACK_CONTROLLER;
		        default -> null;
	        };
        }
        return super.getAnimStateController(player, animSystemData, animStateID);
    }
}
