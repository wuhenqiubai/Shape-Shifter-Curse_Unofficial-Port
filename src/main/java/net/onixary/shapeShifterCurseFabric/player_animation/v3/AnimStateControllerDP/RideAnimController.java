package net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateControllerDP;

import com.google.gson.JsonObject;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.onixary.shapeShifterCurseFabric.player_animation.AnimationHolder;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AbstractAnimStateController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AbstractAnimStateControllerDP;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimSystem;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimUtils;
import org.jetbrains.annotations.Nullable;

public class RideAnimController extends AbstractAnimStateControllerDP {
    private AnimUtils.AnimationHolderData animationHolderData;
    private @Nullable AnimationHolder animationHolder = null;
    private AnimUtils.AnimationHolderData RideVehicleAnimationHolderData;
    private @Nullable AnimationHolder RideVehicleAnimationHolder = null;

    public RideAnimController(@Nullable JsonObject jsonData) {
        super(jsonData);
    }

    public RideAnimController(@Nullable AnimUtils.AnimationHolderData animationHolderData, @Nullable AnimUtils.AnimationHolderData RideVehicleAnimationHolderData) {
        super();
        this.animationHolderData = AnimUtils.ensureAnimHolderDataNotNull(animationHolderData);
        this.RideVehicleAnimationHolderData = AnimUtils.ensureAnimHolderDataNotNull(RideVehicleAnimationHolderData);
    }

    @Override
    public @Nullable AnimationHolder getAnimation(Player player, AnimSystem.AnimSystemData data) {
        if (player.getVehicle() instanceof Boat || player.getVehicle() instanceof Minecart) {
            return RideVehicleAnimationHolder;
        } else {
            return animationHolder;
        }
    }

    @Override
    public void registerAnim(Player player, AnimSystem.AnimSystemData data) {
        this.animationHolder = this.animationHolderData.build();
        this.RideVehicleAnimationHolder = this.RideVehicleAnimationHolderData.build();
        super.registerAnim(player, data);
    }

    @Override
    public AbstractAnimStateController loadFormJson(JsonObject jsonObject) {
        this.animationHolderData = AnimUtils.readAnimInJson(jsonObject, "anim", null);
        this.RideVehicleAnimationHolderData = AnimUtils.readAnimInJson(jsonObject, "rideVehicleAnim", null);
	    return null;
    }
}