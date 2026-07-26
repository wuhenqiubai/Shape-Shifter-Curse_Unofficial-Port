package net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateControllerDP;

import com.google.gson.JsonObject;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.player_animation.AnimationHolder;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AbstractAnimStateController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AbstractAnimStateControllerDP;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimSystem;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimUtils;
import org.jetbrains.annotations.Nullable;

public class SwimAnimController extends AbstractAnimStateControllerDP {
    private AnimUtils.AnimationHolderData animationHolderData1;
    private @Nullable AnimationHolder animationHolder1 = null;
    private AnimUtils.AnimationHolderData animationHolderData2;
    private @Nullable AnimationHolder animationHolder2 = null;

    public SwimAnimController(@Nullable JsonObject jsonData) {
        super(jsonData);
    }

    public SwimAnimController(@Nullable AnimUtils.AnimationHolderData animationHolderDataFloat, @Nullable AnimUtils.AnimationHolderData animationHolderDataSwimming) {
        super();
        this.animationHolderData1 = AnimUtils.ensureAnimHolderDataNotNull(animationHolderDataFloat);
        this.animationHolderData2 = AnimUtils.ensureAnimHolderDataNotNull(animationHolderDataSwimming);
    }

    @Override
    public @Nullable AnimationHolder getAnimation(Player player, AnimSystem.AnimSystemData data) {
        if (player.isSwimming()) {
            return this.animationHolder2;
        } else {
            return this.animationHolder1;
        }
    }

    @Override
    public void registerAnim(Player player, AnimSystem.AnimSystemData data) {
        this.animationHolder1 = this.animationHolderData1.build();
        this.animationHolder2 = this.animationHolderData2.build();
        super.registerAnim(player, data);
    }

    @Override
    public AbstractAnimStateController loadFormJson(JsonObject jsonObject) {
        this.animationHolderData1 = AnimUtils.readAnimInJson(jsonObject, "anim", null);
        this.animationHolderData2 = AnimUtils.readAnimInJson(jsonObject, "swimAnim", null);
	    return null;
    }
}