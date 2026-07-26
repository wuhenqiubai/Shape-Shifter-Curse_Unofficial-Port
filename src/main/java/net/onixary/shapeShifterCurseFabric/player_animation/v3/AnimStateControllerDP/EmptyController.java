package net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateControllerDP;

import com.google.gson.JsonObject;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.player_animation.AnimationHolder;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AbstractAnimStateController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AbstractAnimStateControllerDP;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimSystem;
import org.jetbrains.annotations.Nullable;

public class EmptyController extends AbstractAnimStateControllerDP {
    public EmptyController() {
        super();
    }

    public EmptyController(@Nullable JsonObject jsonData) {
        super(jsonData);
    }

    @Override
    public boolean isRegistered(Player player, AnimSystem.AnimSystemData data) {
        return true;
    }

    @Override
    public @Nullable AnimationHolder getAnimation(Player player, AnimSystem.AnimSystemData data) {
        return null;
    }

    @Override
    public AbstractAnimStateController loadFormJson(JsonObject jsonData) {
        return this;
    }
}