package net.onixary.shapeShifterCurseFabric.render.form_render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

import java.util.Optional;

public class PlayerBaseModel extends GeoModel<FormAnimatable> {
    private static final Identifier WIDE_MODEL = Identifier.of("shape-shifter-curse", "geo/player_base_wide.geo.json");
    private static final Identifier SLIM_MODEL = Identifier.of("shape-shifter-curse", "geo/player_base_slim.geo.json");

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
        if (animatable.getEntity() instanceof AbstractClientPlayerEntity player) {
            return player.getSkinTextures().texture();
        }
        return Identifier.of("minecraft", "textures/entity/steve.png");
    }

    @Override
    public Identifier getAnimationResource(FormAnimatable animatable) {
        return null;
    }

    public void syncBoneTransforms(FormModel source) {
        BakedGeoModel baked = getBakedModel(getModelResource(null));
        if (baked == null) return;
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
