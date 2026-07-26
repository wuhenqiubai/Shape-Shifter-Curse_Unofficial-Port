package net.onixary.shapeShifterCurseFabric.integration;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import java.lang.reflect.Method;

/**
 * 通过反射调用 EMF Animation API，避免硬依赖 Entity Model Features。
 */
public class EMFIntegration {
    private static final boolean EMF_LOADED;
    private static Method emfEntityOf;
    private static Method pauseAll;
    private static Method resumeAll;
    private static Method pauseParts;

    static {
        boolean loaded = false;
        if (FabricLoader.getInstance().isModLoaded("entity_model_features")) {
            try {
                Class<?> api = Class.forName("traben.entity_model_features.EMFAnimationApi");
                emfEntityOf = api.getMethod("emfEntityOf", Entity.class);
                Class<?> emfEntityType = emfEntityOf.getReturnType();
                pauseAll = api.getMethod("pauseAllCustomAnimationsForEntity", emfEntityType);
                resumeAll = api.getMethod("resumeAllCustomAnimationsForEntity", emfEntityType);
                pauseParts = api.getMethod("pauseCustomAnimationsForThesePartsOfEntity", emfEntityType, ModelPart[].class);
                loaded = true;
            } catch (Exception ignored) {}
        }
        EMF_LOADED = loaded;
    }

    public static void pauseAllAnimations(Entity entity) {
        if (!EMF_LOADED) return;
        try {
            Object emfEntity = emfEntityOf.invoke(null, entity);
            pauseAll.invoke(null, emfEntity);
        } catch (Exception ignored) {}
    }

    public static void pauseAnimationsForParts(Entity entity, ModelPart... parts) {
        if (!EMF_LOADED || parts == null || parts.length == 0) return;
        try {
            Object emfEntity = emfEntityOf.invoke(null, entity);
            pauseParts.invoke(null, emfEntity, (Object) parts);
        } catch (Exception ignored) {}
    }

    public static void resumeAnimations(Entity entity) {
        if (!EMF_LOADED) return;
        try {
            Object emfEntity = emfEntityOf.invoke(null, entity);
            resumeAll.invoke(null, emfEntity);
        } catch (Exception ignored) {}
    }
}
