package net.onixary.shapeShifterCurseFabric.items.armors;

import net.minecraft.resources.ResourceLocation;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class NetheriteMorphscaleArmorRenderer extends GeoArmorRenderer<NetheriteMorphScaleArmor> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID,"netherite_morphscale_armor");

    public NetheriteMorphscaleArmorRenderer() {
        super(new DefaultedItemGeoModel<>(MODEL));
    }
}
