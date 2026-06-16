package net.onixary.shapeShifterCurseFabric.player_animation.v3;

/**
 * Accessor interface for retrieving a player's AnimSystem.
 * Mixed into AbstractClientPlayerEntity by PlayerEntityAnimOverrideMixin.
 */
public interface IAnimSystemAccessor {
    AnimSystem shape_shifter_curse$getAnimSystem();
}
