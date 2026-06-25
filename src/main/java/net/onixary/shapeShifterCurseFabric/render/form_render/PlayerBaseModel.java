package net.onixary.shapeShifterCurseFabric.render.form_render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.function.Function;

/**
 * Shared GeoModel for the player base mesh (wide/slim variant).
 * Loaded once and cached; bone transforms are synced from FormModel before each render.
 */
public class PlayerBaseModel extends GeoModel<FormAnimatable> {
    private static final Identifier WIDE_MODEL = Identifier.of("shape-shifter-curse", "geo/player_base_wide");
    private static final Identifier SLIM_MODEL = Identifier.of("shape-shifter-curse", "geo/player_base_slim");

    private static PlayerBaseModel WIDE_INSTANCE;
    private static PlayerBaseModel SLIM_INSTANCE;

    private final boolean slim;

    public PlayerBaseModel(boolean slim) {
        this.slim = slim;
    }

    public static PlayerBaseModel get(boolean slim) {
        if (slim) {
            if (SLIM_INSTANCE == null) SLIM_INSTANCE = new PlayerBaseModel(true);
            return SLIM_INSTANCE;
        }
        if (WIDE_INSTANCE == null) WIDE_INSTANCE = new PlayerBaseModel(false);
        return WIDE_INSTANCE;
    }

    @Override
    public Identifier getModelResource(FormAnimatable animatable) {
        return slim ? SLIM_MODEL : WIDE_MODEL;
    }

    @Override
    public Identifier getTextureResource(FormAnimatable animatable) {
        // Return the player's skin texture
        if (animatable.getEntity() instanceof AbstractClientPlayerEntity player) {
            return player.getSkinTextures().texture();
        }
        return Identifier.of("minecraft", "textures/entity/steve.png");
    }

    @Override
    public Identifier getAnimationResource(FormAnimatable animatable) {
        return null; // No animations; driven by FormModel
    }

    /** Copy bone transforms from FormModel's corresponding bones to this model. */
    public void syncBoneTransforms(FormModel source) {
        BakedGeoModel baked = getBakedModel(getModelResource(null));
        for (GeoBone targetBone : baked.topLevelBones()) {
            syncBoneRecursive(targetBone, source);
        }
    }

    private void syncBoneRecursive(GeoBone targetBone, FormModel source) {
        Optional<GeoBone> sourceBone = source.getBone(targetBone.getName());
        if (sourceBone.isPresent()) {
            GeoBone src = sourceBone.get();
            targetBone.setRotX(src.getRotX());
            targetBone.setRotY(src.getRotY());
            targetBone.setRotZ(src.getRotZ());
            targetBone.setPosX(src.getPosX());
            targetBone.setPosY(src.getPosY());
            targetBone.setPosZ(src.getPosZ());
            targetBone.setScaleX(src.getScaleX());
            targetBone.setScaleY(src.getScaleY());
            targetBone.setScaleZ(src.getScaleZ());
        }
        for (GeoBone child : targetBone.getChildBones()) {
            syncBoneRecursive(child, source);
        }
    }
}
