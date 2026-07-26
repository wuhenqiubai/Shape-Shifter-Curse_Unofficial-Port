package net.onixary.shapeShifterCurseFabric.render.tech;

import net.minecraft.resources.ResourceLocation;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import software.bernie.geckolib.model.GeoModel;

public class CocoonModel extends GeoModel<EmptyAnimatable> {
    @Override
    public ResourceLocation getModelResource(EmptyAnimatable animatable) {
        return ShapeShifterCurseFabric.identifier("geo/tech/enemy_cocoon.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EmptyAnimatable animatable) {
        return ShapeShifterCurseFabric.identifier("textures/tech/enemy_cocoon.png");
    }

    @Override
    public ResourceLocation getAnimationResource(EmptyAnimatable animatable) {
        return ShapeShifterCurseFabric.identifier("animations/missing.animation.json");
    }
}
