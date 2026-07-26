package net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateControllerDP;

import com.google.gson.JsonObject;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.player_animation.AnimationHolder;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AbstractAnimStateController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AbstractAnimStateControllerDP;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimSystem;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimUtils;
import org.jetbrains.annotations.Nullable;

public class RushJumpAnimController extends AbstractAnimStateControllerDP {
    private AnimUtils.AnimationHolderData animationHolderData;
    private @Nullable AnimationHolder animationHolder = null;
    private AnimUtils.AnimationHolderData sneakAnimationHolderData;
    private @Nullable AnimationHolder sneakAnimationHolder = null;
    private AnimUtils.AnimationHolderData rushJumpAnimationHolderData;
    private @Nullable AnimationHolder rushJumpAnimationHolder = null;
    private AnimUtils.AnimationHolderData sneakRushJumpAnimationHolderData;
    private @Nullable AnimationHolder sneakRushJumpAnimationHolder = null;

    public RushJumpAnimController(@Nullable JsonObject jsonData) {
        super(jsonData);
    }

    public RushJumpAnimController(@Nullable AnimUtils.AnimationHolderData animationHolderData, @Nullable AnimUtils.AnimationHolderData sneakAnimationHolderData, AnimUtils.AnimationHolderData rushJumpAnimationHolderData, AnimUtils.AnimationHolderData sneakRushJumpAnimationHolderData) {
        super();
        this.animationHolderData = AnimUtils.ensureAnimHolderDataNotNull(animationHolderData);
        this.sneakAnimationHolderData = AnimUtils.ensureAnimHolderDataNotNull(sneakAnimationHolderData);
        this.rushJumpAnimationHolderData = AnimUtils.ensureAnimHolderDataNotNull(rushJumpAnimationHolderData);
        this.sneakRushJumpAnimationHolderData = AnimUtils.ensureAnimHolderDataNotNull(sneakRushJumpAnimationHolderData);
    }

    @Override
    public @Nullable AnimationHolder getAnimation(Player player, AnimSystem.AnimSystemData data) {
        if (Math.abs(player.getDeltaMovement().z) > 0.15 || Math.abs(player.getDeltaMovement().x) > 0.15) {
            if (player.isShiftKeyDown()) {
                return sneakRushJumpAnimationHolder;
            } else {
                return rushJumpAnimationHolder;
            }
        } else {
            if (player.isShiftKeyDown()) {
                return sneakAnimationHolder;
            } else {
                return animationHolder;
            }
        }
    }

    @Override
    public void registerAnim(Player player, AnimSystem.AnimSystemData data) {
        this.animationHolder = this.animationHolderData.build();
        this.sneakAnimationHolder = this.sneakAnimationHolderData.build();
        this.rushJumpAnimationHolder = this.rushJumpAnimationHolderData.build();
        this.sneakRushJumpAnimationHolder = this.sneakRushJumpAnimationHolderData.build();
        super.registerAnim(player, data);
    }

    @Override
    public AbstractAnimStateController loadFormJson(JsonObject jsonObject) {
        this.animationHolderData = AnimUtils.readAnimInJson(jsonObject, "anim", null);
        this.sneakAnimationHolderData = AnimUtils.readAnimInJson(jsonObject, "sneakAnim", null);
        this.rushJumpAnimationHolderData = AnimUtils.readAnimInJson(jsonObject, "rushJumpAnim", null);
        this.sneakRushJumpAnimationHolderData = AnimUtils.readAnimInJson(jsonObject, "sneakRushJumpAnim", null);
	    return null;
    }
}
