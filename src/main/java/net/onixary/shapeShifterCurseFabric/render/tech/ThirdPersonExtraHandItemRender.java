package net.onixary.shapeShifterCurseFabric.render.tech;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Predicate;

public class ThirdPersonExtraHandItemRender<T extends LivingEntity, M extends EntityModel<T> & ArmedModel> extends RenderLayer<T, M> {

    public static abstract class TPEHR_Render {
        public abstract void render(ItemInHandRenderer heldItemRenderer, PoseStack matrices, MultiBufferSource vertexConsumers, int light, AbstractClientPlayer player, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch);
    }

    public record TPEHRData(Predicate<AbstractClientPlayer> shouldRender, TPEHR_Render render) { }

    public static HashMap<IForm, ArrayList<TPEHRData>> data = new HashMap<>();

    public final ItemInHandRenderer heldItemRenderer;

    static {
        register(RegPlayerForms.SPIDER_3, new TPEHRData(p -> true, new SpiderTPEHR()));
        register(RegPlayerForms.SPIDER_2, new TPEHRData(p -> true, new SpiderTPEHR()));
    }

    public static void register(IForm form, TPEHRData Rdata) {
        if (!data.containsKey(form)) {
            data.put(form, new ArrayList<>());
        }
        data.get(form).add(Rdata);
    }

    public ThirdPersonExtraHandItemRender(RenderLayerParent<T, M> context, ItemInHandRenderer heldItemRenderer) {
        super(context);
        this.heldItemRenderer = heldItemRenderer;
    }

    public void render(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
        if (livingEntity instanceof AbstractClientPlayer player) {
            IForm curForm = FormTextureUtils.getPlayerForm_Render(player);
            if (data.containsKey(curForm)) {
                for (TPEHRData Rdata : data.get(curForm)) {
                    if (Rdata.shouldRender().test(player)) {
                        matrixStack.pushPose();
                        Rdata.render().render(heldItemRenderer, matrixStack, vertexConsumerProvider, i, player, f, g, h, j, k, l);
                        matrixStack.popPose();
                    }
                }
            }
        }
    }
}