package net.onixary.shapeShifterCurseFabric.render.tech;

import net.minecraft.resources.Identifier;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class CocoonModel extends GeoModel<EmptyAnimatable> {
    @Override
    public Identifier getModelResource(GeoRenderState animatable) {
        // GL5: 模型资源 key = stripPrefixAndSuffix(资源路径) = geo/tech/enemy_cocoon（非 .geo.json 后缀）
        return ShapeShifterCurseFabric.identifier("geo/tech/enemy_cocoon");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState animatable) {
        return ShapeShifterCurseFabric.identifier("textures/tech/enemy_cocoon.png");
    }

    @Override
    public Identifier getAnimationResource(EmptyAnimatable animatable) {
        // GL5: cache key = stripPrefixAndSuffix(资源路径) = missing（非 missing.animation）
        return ShapeShifterCurseFabric.identifier("missing");
    }
}