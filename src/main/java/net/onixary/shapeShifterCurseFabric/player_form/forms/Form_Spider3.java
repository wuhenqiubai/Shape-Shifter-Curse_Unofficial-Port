package net.onixary.shapeShifterCurseFabric.player_form.forms;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AbstractAnimStateController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateControllerDP.*;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateEnum;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimSystem;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimUtils;
import net.onixary.shapeShifterCurseFabric.player_form.NormalForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Form_Spider3 extends NormalForm {
    
    public Form_Spider3(Identifier formID) {
        super(formID);
    }

    // v3动画系统
    public static final AnimUtils.AnimationHolderData ANIM_IDLE = 
        new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("spider_3_idle"), 1.0f, 6);
    public static final AnimUtils.AnimationHolderData ANIM_RUN =
            new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("spider_3_run"), 1.0f, 6).setSpeed(1.8f);
    public static final AnimUtils.AnimationHolderData ANIM_WALK =
            new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("spider_3_walk"), 1.0f, 6).setSpeed(1.2f);
    public static final AnimUtils.AnimationHolderData ANIM_SNEAK_IDLE =
            new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("spider_3_sneak_idle"), 1.0f, 6);
    public static final AnimUtils.AnimationHolderData ANIM_SNEAK_WALK =
            new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("spider_3_sneak_walk"), 1.0f, 6);
    public static final AnimUtils.AnimationHolderData ANIM_SWIM_IDLE =
            new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("spider_3_swim_idle"), 1.0f, 6);
    public static final AnimUtils.AnimationHolderData ANIM_JUMP =
            new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("spider_3_jump"), 1.0f, 6);
    public static final AnimUtils.AnimationHolderData ANIM_FALL =
            new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("spider_3_fall"), 1.0f, 6);
    public static final AnimUtils.AnimationHolderData ANIM_CLIMB =
            new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("spider_3_climb"), 1.0f, 6);
    public static final AnimUtils.AnimationHolderData ANIM_CLIMB_IDLE =
            new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("spider_3_climb_idle"));
    public static final AnimUtils.AnimationHolderData ANIM_FLY =
            new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("spider_3_creative_flight"), 1.0f, 6);
    public static final AnimUtils.AnimationHolderData ANIM_RIDE =
            new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("spider_3_ride"), 1.0f, 6);

    public static final AbstractAnimStateController IDLE_CONTROLLER = 
        new WithSneakAnimController(ANIM_IDLE, ANIM_SNEAK_IDLE);
    public static final AbstractAnimStateController WALK_CONTROLLER =
            new WithSneakAnimController(ANIM_WALK, ANIM_SNEAK_WALK);
    public static final AbstractAnimStateController RUN_CONTROLLER =
            new WithSneakAnimController(ANIM_RUN, ANIM_SNEAK_WALK);
    public static final AbstractAnimStateController JUMP_CONTROLLER =
            new OneAnimController(ANIM_JUMP);
    public static final AbstractAnimStateController FALL_CONTROLLER =
            new OneAnimController(ANIM_FALL);
    public static final AbstractAnimStateController SWIM_CONTROLLER =
            new SwimAnimController(ANIM_SWIM_IDLE, null);
    public static final AbstractAnimStateController CLIMB_CONTROLLER =
            new ClimbAnimController(ANIM_CLIMB_IDLE, ANIM_CLIMB);
    public static final AbstractAnimStateController FLIGHT_CONTROLLER =
            new OneAnimController(ANIM_FLY);
    public static final AbstractAnimStateController RIDE_CONTROLLER =
            new RideAnimController(ANIM_RIDE, ANIM_RIDE);



    @Override
    public @Nullable AbstractAnimStateController getAnimStateController(
            PlayerEntity player, 
            AnimSystem.AnimSystemData animSystemData, 
            @NotNull Identifier animStateID) {
        
        AnimStateEnum state = AnimStateEnum.getStateEnum(animStateID);
        if (state != null) {
	        return switch (state) {
		        case ANIM_STATE_IDLE -> IDLE_CONTROLLER;
		        case ANIM_STATE_WALK -> WALK_CONTROLLER;
		        case ANIM_STATE_SPRINT -> RUN_CONTROLLER;
		        case ANIM_STATE_JUMP -> JUMP_CONTROLLER;
		        case ANIM_STATE_FALL -> FALL_CONTROLLER;
		        case ANIM_STATE_SWIM -> SWIM_CONTROLLER;
		        case ANIM_STATE_CLIMB -> CLIMB_CONTROLLER;
		        case ANIM_STATE_FLYING -> FLIGHT_CONTROLLER;
		        case ANIM_STATE_RIDE -> RIDE_CONTROLLER;
		        default -> null;
	        };
        }
        return super.getAnimStateController(player, animSystemData, animStateID);
    }
}
