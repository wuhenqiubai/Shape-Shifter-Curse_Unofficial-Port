package net.onixary.shapeShifterCurseFabric.render.form_render;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;

@SuppressWarnings("removal")
public interface IModelAnimationSystem {
    public void loadConfig(@Nullable JsonObject json);

    public default void beforeRender(FormRenderer formRenderer, FormModel model, PlayerRenderer renderer, Player player, PoseStack matrices, MultiBufferSource vertexConsumers, int light, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {}

    public void processAnimation(FormRenderer formRenderer, FormModel model, PlayerRenderer renderer, Player player, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch);

    public default void afterRender(FormRenderer formRenderer, FormModel model, PlayerRenderer renderer, Player player, PoseStack matrices, MultiBufferSource vertexConsumers, int light, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {}

    public default void finishRender(FormRenderer formRenderer, FormModel model, PlayerRenderer renderer, Player player, PoseStack matrices, MultiBufferSource vertexConsumers, int light, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {}

    public default @Nullable GeoBone beforeRenderFirstPerson(@Nullable GeoBone geoBone, FormRenderer formRenderer, FormModel model, PlayerRenderer renderer, Player player, ModelPart arm, ModelPart sleeve) { return geoBone; }

    public @Nullable GeoBone processAnimationFirstPerson(@Nullable GeoBone geoBone, FormRenderer formRenderer, FormModel model, PlayerRenderer renderer, Player player, ModelPart arm, ModelPart sleeve);

    public default @Nullable GeoBone afterRenderFirstPerson(@Nullable GeoBone geoBone, FormRenderer formRenderer, FormModel model, PlayerRenderer renderer, Player player, ModelPart arm, ModelPart sleeve) { return geoBone; }

}
