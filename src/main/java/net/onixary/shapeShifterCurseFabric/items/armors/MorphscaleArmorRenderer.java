package net.onixary.shapeShifterCurseFabric.items.armors;

import com.geckolib.model.DefaultedItemGeoModel;
import com.geckolib.renderer.GeoArmorRenderer;
import net.minecraft.resources.Identifier;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

public class MorphscaleArmorRenderer extends GeoArmorRenderer<MorphScaleArmor, MorphscaleArmorRenderState> {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID,"morphscale_armor");

    public MorphscaleArmorRenderer() {
        super(new DefaultedItemGeoModel<>(MODEL));
    }
}