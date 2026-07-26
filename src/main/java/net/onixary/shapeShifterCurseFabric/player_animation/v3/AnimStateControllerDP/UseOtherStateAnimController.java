package net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateControllerDP;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.player_animation.AnimationHolder;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AbstractAnimStateController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AbstractAnimStateControllerDP;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimSystem;
import org.jetbrains.annotations.Nullable;

public class UseOtherStateAnimController extends AbstractAnimStateControllerDP {
    public @Nullable ResourceLocation otherStateId;

    public UseOtherStateAnimController(@Nullable ResourceLocation otherStateId) {
        super();
        this.otherStateId = otherStateId;
    }

    public UseOtherStateAnimController(@Nullable JsonObject jsonData) {
        super(jsonData);
    }

    private @Nullable AbstractAnimStateController getOtherStateController(Player player, AnimSystem.AnimSystemData data) {
        if (this.otherStateId == null) {
            return null;
        }
        return data.playerForm.getAnimStateController(player, data, this.otherStateId);
    }

    @Override
    public boolean isRegistered(Player player, AnimSystem.AnimSystemData data) {
        AbstractAnimStateController otherStateController = this.getOtherStateController(player, data);
        if (otherStateController != null) {
            return otherStateController.isRegistered(player, data);
        }
        return true;
    }

    @Override
    public void registerAnim(Player player, AnimSystem.AnimSystemData data) {
        AbstractAnimStateController otherStateController = this.getOtherStateController(player, data);
        if (otherStateController != null) {
            otherStateController.registerAnim(player, data);
        }
        super.registerAnim(player, data);
    }

    @Override
    public @Nullable AnimationHolder getAnimation(Player player, AnimSystem.AnimSystemData data) {
        AbstractAnimStateController otherStateController = this.getOtherStateController(player, data);
        if (otherStateController != null) {
            if (!otherStateController.isRegistered(player, data)) {
                otherStateController.registerAnim(player, data);
            }
            return otherStateController.getAnimation(player, data);
        }
        // if (ShapeShifterCurseFabric.IsDevelopmentEnvironment()) {  // 由于otherStateId nullable但是逻辑上不推荐为null 所以在开发环境下提示
        //     ShapeShifterCurseFabric.LOGGER.warn("UseOtherStateAnimController State Not Found: {}", this.otherStateId);
        // }
        return null;
    }

    @Override
    public AbstractAnimStateController loadFormJson(JsonObject jsonData) {
        if (jsonData != null && jsonData.has("StateControllerId") && jsonData.get("StateControllerId").isJsonPrimitive())  {
            this.otherStateId = ResourceLocation.tryParse(jsonData.get("StateControllerId").getAsString());
        } else {
            this.otherStateId = null;
        }
	    return null;
    }
}
