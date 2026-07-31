package net.onixary.shapeShifterCurseFabric.render.tech;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Predicate;

public class ThirdPersonExtraHandItemRender<S extends EntityRenderState, M extends EntityModel<? super S> & ArmedModel<S>> extends RenderLayer<S, M> {

    public static abstract class TPEHR_Render {
        public abstract void render(ItemInHandRenderer heldItemRenderer, PoseStack matrices, SubmitNodeCollector submitNodeCollector, int light, AbstractClientPlayer player, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch);
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

    public ThirdPersonExtraHandItemRender(RenderLayerParent<S, M> context, ItemInHandRenderer heldItemRenderer) {
        super(context);
        this.heldItemRenderer = heldItemRenderer;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, S entityRenderState, float f, float g) {
        if (!(entityRenderState instanceof AvatarRenderState avatarRenderState)) return;
        Entity entity = Minecraft.getInstance().level.getEntity(avatarRenderState.id);
        if (!(entity instanceof AbstractClientPlayer player)) return;
        IForm curForm = FormTextureUtils.getPlayerForm_Render(player);
        if (data.containsKey(curForm)) {
            float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
            for (TPEHRData Rdata : data.get(curForm)) {
                if (Rdata.shouldRender().test(player)) {
                    poseStack.pushPose();
                    Rdata.render().render(heldItemRenderer, poseStack, submitNodeCollector, i, player,
                            avatarRenderState.walkAnimationPos, avatarRenderState.walkAnimationSpeed, tickDelta, 0f, f, g);
                    poseStack.popPose();
                }
            }
        }
    }
}
