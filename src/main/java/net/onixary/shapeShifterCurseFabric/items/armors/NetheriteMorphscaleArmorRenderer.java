package net.onixary.shapeShifterCurseFabric.items.armors;

import com.geckolib.model.DefaultedItemGeoModel;
import com.geckolib.renderer.GeoArmorRenderer;
import net.minecraft.resources.Identifier;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

public class NetheriteMorphscaleArmorRenderer extends GeoArmorRenderer<NetheriteMorphScaleArmor, MorphscaleArmorRenderState> {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID,"netherite_morphscale_armor");

    public NetheriteMorphscaleArmorRenderer() {
        super(new DefaultedItemGeoModel<>(MODEL));
    }
}