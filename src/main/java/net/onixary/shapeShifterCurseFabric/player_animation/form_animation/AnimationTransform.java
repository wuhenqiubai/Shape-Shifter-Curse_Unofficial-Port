package net.onixary.shapeShifterCurseFabric.player_animation.form_animation;

import net.minecraft.util.Identifier;
import net.onixary.shapeShifterCurseFabric.player_animation.AnimationHolder;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

public class AnimationTransform {
    private AnimationTransform() {
    }

    private static AnimationHolder anim_on_transform_default = AnimationHolder.EMPTY;
    private static AnimationHolder anim_on_transform_normal_to_feral = AnimationHolder.EMPTY;
    private static AnimationHolder anim_on_transform_feral_to_normal = AnimationHolder.EMPTY;


    public static void registerAnims() {
        anim_on_transform_default = new AnimationHolder(Identifier.of(MOD_ID, "player_on_transform"), true);
        anim_on_transform_normal_to_feral = new AnimationHolder(Identifier.of(MOD_ID, "player_on_transform_normal_to_feral"), true);
        anim_on_transform_feral_to_normal = new AnimationHolder(Identifier.of(MOD_ID, "player_on_transform_feral_to_normal"), true);
    }
}
