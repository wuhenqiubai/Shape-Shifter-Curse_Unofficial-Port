package net.onixary.shapeShifterCurseFabric.render.form_render;

import com.google.gson.JsonObject;
import software.bernie.geckolib.renderer.GeoObjectRenderer;
import net.minecraft.entity.player.PlayerEntity;

@SuppressWarnings("removal")
public class FormRenderer extends GeoObjectRenderer<FormAnimatable> {
    public FormModel realModel = null;

    public FormRenderer(JsonObject modelJson) {
        super(new FormModel(modelJson));
        this.realModel = (FormModel) this.model;
        this.animatable = new FormAnimatable();
    }

    public void setPlayer(PlayerEntity player, boolean slim) {
        if (this.animatable == null) {
            this.animatable = new FormAnimatable();
        }
        this.animatable.setPlayer(player);
        this.realModel.setPlayer(player, slim);
    }
}
