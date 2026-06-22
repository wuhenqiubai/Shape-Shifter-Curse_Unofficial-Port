package net.onixary.shapeShifterCurseFabric.render.form_render;

import com.google.gson.JsonObject;
import software.bernie.geckolib.renderer.GeoObjectRenderer;
import net.minecraft.entity.player.PlayerEntity;

public class FormRenderer extends GeoObjectRenderer<FormAnimatable> {
    public FormModel realModel = null;
    // Persistent reference — GeoObjectRenderer.doPostRenderCleanup() nulls the parent field each frame,
    // but FormAnimatable must survive across frames for AnimationController predicate state caching.
    private FormAnimatable persistentAnimatable;

    public FormRenderer(JsonObject modelJson) {
        super(new FormModel(modelJson));
        this.realModel = (FormModel) this.model;
        this.persistentAnimatable = new FormAnimatable();
        this.animatable = this.persistentAnimatable;
    }

    public void setPlayer(PlayerEntity player, boolean slim) {
        this.persistentAnimatable.setPlayer(player);
        this.animatable = this.persistentAnimatable;
        this.realModel.setPlayer(player, slim);
    }
}
