package net.onixary.shapeShifterCurseFabric.render.tech;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.onixary.shapeShifterCurseFabric.util.Accessory.AccessoryUtils;

public class SpiderTPEHR extends ThirdPersonExtraHandItemRender.TPEHR_Render {
    public static final String GROUP_STRING = "hand";
    public static final String INV_STRING = "extra_hand";

    @Override
    public void render(ItemInHandRenderer heldItemRenderer, PoseStack matrices, MultiBufferSource vertexConsumers, int light, AbstractClientPlayer player, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        ItemStack stack = AccessoryUtils.getEntitySlot(player, "auto", GROUP_STRING, INV_STRING, 0);
        if (stack == null || stack.isEmpty()) {
            return;
        }

	    EntityRenderDispatcher ed = Minecraft.getInstance().getEntityRenderDispatcher();
	    if (ed == null) return;
	    AvatarRenderer eR = (AvatarRenderer) ed.getRenderer(player);
	    if (eR == null) return;
        ModelPart body = ((PlayerModel)eR.getModel()).body;
        body.translateAndRotate(matrices);
        if(stack.getItem() == Items.SHIELD){
            // 适用于spider_2、spider_3额外手臂盾牌的transform，需要将其转向正面
            //matrices.translate(-0.1F, 0.6F, -0.5F);
            matrices.translate(0.1F, 0.2F, -0.2F);
            matrices.scale(0.65F, 0.65F, 0.65F);
            matrices.mulPose(Axis.XP.rotationDegrees(180.0F));
            matrices.mulPose(Axis.ZP.rotationDegrees(30.0F));
            matrices.mulPose(Axis.YP.rotationDegrees(-90.0F));
            matrices.translate(1.0 / 16.0F, -2.0 / 16.0F, 1.0 / 16.0F);
        }
        else{
            // 适用于spider_2、spider_3额外手臂通常道具的transform
            matrices.translate(-0.1F, 0.6F, -0.5F);
            matrices.scale(0.8F, 0.8F, 0.8F);
            matrices.mulPose(Axis.XP.rotationDegrees(45.0F));
            matrices.mulPose(Axis.ZP.rotationDegrees(180.0F));
            matrices.mulPose(Axis.YP.rotationDegrees(15.0F));
            matrices.translate(1.0 / 16.0F, -2.0 / 16.0F, 1.0 / 16.0F);
        }
        heldItemRenderer.renderItem(player, stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, matrices, (SubmitNodeCollector)vertexConsumers, light);

    }
}