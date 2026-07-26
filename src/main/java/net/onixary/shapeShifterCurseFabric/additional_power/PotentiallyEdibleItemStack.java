package net.onixary.shapeShifterCurseFabric.additional_power;

import net.minecraft.world.food.FoodProperties;

import java.util.Optional;

public interface PotentiallyEdibleItemStack {
    Optional<FoodProperties> apoli$getFoodComponent();
}