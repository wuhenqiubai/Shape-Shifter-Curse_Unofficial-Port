package net.onixary.shapeShifterCurseFabric.player_form.forms;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AbstractAnimStateController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateControllerDP.RideAnimController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateEnum;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimSystem;

import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimUtils;
import net.onixary.shapeShifterCurseFabric.player_form.PlayerFormBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Form_SnowFox2 extends PlayerFormBase {
    public Form_SnowFox2(Identifier formID) {
        super(formID);
    }

    public static final AbstractAnimStateController RIDE_CONTROLLER = new RideAnimController(new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("snow_fox_2_riding"), 1.0f, 6, "EASE_IN_OUT_QUAD"), new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("ocelot_2_sneak_idle"), 1.0f, 6));

    @Override
    public @Nullable AbstractAnimStateController getAnimStateController(PlayerEntity player, AnimSystem.AnimSystemData animSystemData, @NotNull Identifier animStateID) {
        @Nullable AnimStateEnum animStateEnum = AnimStateEnum.getStateEnum(animStateID);
        if (animStateEnum != null) {
	        return switch (animStateEnum) {
		        case ANIM_STATE_IDLE -> Form_FamiliarFox2.IDLE_CONTROLLER;
		        case ANIM_STATE_RIDE -> RIDE_CONTROLLER;
		        default -> null;
	        };
        }
        return super.getAnimStateController(player, animSystemData, animStateID);
    }
}
