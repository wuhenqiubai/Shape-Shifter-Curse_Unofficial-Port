package net.onixary.shapeShifterCurseFabric.player_form.forms;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.*;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateControllerDP.RideAnimController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateControllerDP.RushJumpAnimController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateControllerDP.SneakRushAnimController;

import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateControllerDP.WithSneakAnimController;
import net.onixary.shapeShifterCurseFabric.player_form.PlayerFormBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Form_Ocelot2 extends PlayerFormBase {
    public Form_Ocelot2(Identifier formID) {
        super(formID);
    }

    private static final AnimUtils.AnimationHolderData SNEAK_RUSH_JUMP_ANIM = new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("ocelot_2_rush_jump"), 1.0f, 6);

    public static final AbstractAnimStateController IDLE_CONTROLLER = new WithSneakAnimController(null, new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("ocelot_2_sneak_idle"), 1.0f, 6));
    public static final AbstractAnimStateController RIDE_CONTROLLER = new RideAnimController(new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("ocelot_2_riding"), 1.0f, 6, "EASE_IN_OUT_QUAD"), new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("ocelot_2_sneak_idle"), 1.0f, 6));
    public static final AbstractAnimStateController SNEAK_RUSH_CONTROLLER = new SneakRushAnimController(null, null, new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("ocelot_2_sneak_rush_2"), 3.3f, 2));
    public static final AbstractAnimStateController RUSH_JUMP_CONTROLLER = new RushJumpAnimController(null, SNEAK_RUSH_JUMP_ANIM, null, SNEAK_RUSH_JUMP_ANIM);
    public static final AbstractAnimStateController FALL_CONTROLLER = new WithSneakAnimController(null, SNEAK_RUSH_JUMP_ANIM);

    @Override
    public @Nullable AbstractAnimStateController getAnimStateController(PlayerEntity player, AnimSystem.AnimSystemData animSystemData, @NotNull Identifier animStateID) {
        @Nullable AnimStateEnum animStateEnum = AnimStateEnum.getStateEnum(animStateID);
        if (animStateEnum != null) {
	        return switch (animStateEnum) {
		        case ANIM_STATE_IDLE -> IDLE_CONTROLLER;
		        case ANIM_STATE_RIDE -> RIDE_CONTROLLER;
		        case ANIM_STATE_WALK, ANIM_STATE_SPRINT -> SNEAK_RUSH_CONTROLLER;
		        case ANIM_STATE_JUMP -> RUSH_JUMP_CONTROLLER;
		        case ANIM_STATE_FALL -> FALL_CONTROLLER;
		        default -> null;
	        };
        }
        return super.getAnimStateController(player, animSystemData, animStateID);
    }

}
