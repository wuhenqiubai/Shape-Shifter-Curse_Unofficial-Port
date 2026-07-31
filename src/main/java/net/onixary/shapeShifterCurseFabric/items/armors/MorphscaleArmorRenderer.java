package net.onixary.shapeShifterCurseFabric.items.armors;

import net.minecraft.resources.Identifier;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class MorphscaleArmorRenderer extends GeoArmorRenderer<MorphScaleArmor, MorphscaleArmorRenderState> {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID,"morphscale_armor");

    public MorphscaleArmorRenderer() {
        super(new DefaultedItemGeoModel<>(MODEL));
    }
}