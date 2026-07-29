package net.onixary.shapeShifterCurseFabric.features;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.tr7zw.firstperson.FirstPersonModelCore;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.PlayerFormBodyType;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormModel;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormRenderUtils;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormRenderer;
import net.onixary.shapeShifterCurseFabric.render.form_render.IModifyHead_MAS;
import net.onixary.shapeShifterCurseFabric.util.FeralRenderUtils;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;

public class MouthItemFeature<T extends LivingEntity, M extends EntityModel<T> & ArmedModel> extends RenderLayer<T, M> {
    private final ItemInHandRenderer heldItemRenderer;

    private static final boolean IS_FIRST_PERSON_MOD_LOADED = FabricLoader.getInstance().isModLoaded("firstperson");

    public MouthItemFeature(RenderLayerParent<T, M> context, ItemInHandRenderer heldItemRenderer) {
        super(context);
        this.heldItemRenderer = heldItemRenderer;
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
        // 已知Bug 在开启 FirstPersonModel 并启用时 且在第一人称时 物品位置不对
        if (IS_FIRST_PERSON_MOD_LOADED && Minecraft.getInstance().options.getCameraType().isFirstPerson() && livingEntity == Minecraft.getInstance().player) {
            // 防止出现双物品
            FirstPersonModelCore fpm = FirstPersonModelCore.instance;
            if (fpm.isEnabled()) {
                return;
            }
        }

        if (!(livingEntity instanceof AbstractClientPlayer player)) {
            return;
        }

        IForm curForm = FormTextureUtils.getPlayerForm_Render(player);
        if (curForm.getBodyType() != PlayerFormBodyType.FERAL) {
            return;
        }

        ItemStack mainHandStack = player.getMainHandItem();
        ItemStack offHandStack = player.getOffhandItem();


        boolean isBlocking = player.isUsingItem() && player.getUseItem().getUseAnimation() == UseAnim.BLOCK;
        InteractionHand activeHand = player.getUsedItemHand();

        // 检查是否正在用主手持盾格挡
        boolean isBlockingWithMainHandShield = isBlocking && activeHand == InteractionHand.MAIN_HAND && mainHandStack.getItem() instanceof ShieldItem;

        // --- 主手物品渲染 ---
        if (!mainHandStack.isEmpty()) {
            if (isBlockingWithMainHandShield) {
                // 如果用主手盾牌格挡，则渲染在背后
                renderShieldOnBack(matrixStack, vertexConsumerProvider, i, livingEntity, mainHandStack, true);
            } else {
                // 否则，渲染在嘴里
                renderItemInMouth(matrixStack, vertexConsumerProvider, i, livingEntity, mainHandStack, k, l);
            }
        }

        // --- 副手物品渲染 ---
        if (!offHandStack.isEmpty()) {
            // 检查是否正在用副手持盾格挡
            boolean isBlockingWithOffHandShield = isBlocking && activeHand == InteractionHand.OFF_HAND && offHandStack.getItem() instanceof ShieldItem;

            if (isBlockingWithOffHandShield) {
                // 如果用副手盾牌格挡，则渲染在背后
                renderShieldOnBack(matrixStack, vertexConsumerProvider, i, livingEntity, offHandStack, false);
            } else {
                // 否则，渲染在背后的默认位置
                renderDefaultItemOnBack(matrixStack, vertexConsumerProvider, i, livingEntity, offHandStack);
            }
        }
    }

    private void renderItemInMouth(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, T livingEntity, ItemStack itemStack, float k, float l) {
        if (FeralRenderUtils.isFeralMouthItemBlackListed(itemStack)) {
            return;
        }
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        // if (livingEntity instanceof AbstractClientPlayerEntity player && LongNeckRenderUtils.hasLongNeck(player)) {
        //     return;
        // }
        matrixStack.pushPose();
        var eR = (AvatarRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(livingEntity);
        FormRenderer renderer = FormRenderUtils.searchFirstRenderer(player, formRenderer -> {
            FormModel model = formRenderer.realModel;
            if (model == null) {
                return false;
            }
            return model.AnimationSystem instanceof IModifyHead_MAS;
        });
        var head = eR.getModel().head;
        float headRoll = 0.0f;
        if (renderer != null) {
            ((IModifyHead_MAS)renderer.realModel.AnimationSystem).modifyHeadPart(player, eR.getModel(), renderer.realModel);
            k = (float) Math.toDegrees(head.yRot);
            l = (float) Math.toDegrees(head.xRot);
            headRoll = (float) Math.toDegrees(head.zRot);
        }
        matrixStack.translate(head.x / 16.0F, head.y / 16.0F, head.z / 16.0F);
        matrixStack.mulPose(Axis.ZP.rotationDegrees(headRoll));
        matrixStack.mulPose(Axis.YP.rotationDegrees(k));
        matrixStack.mulPose(Axis.XP.rotationDegrees(l));
        matrixStack.translate(0.06F, 0.085F, -0.35D);
        matrixStack.scale(1.25F, 1.25F, 1.25F);
        matrixStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        matrixStack.translate(1.0 / 16.0F, -2.0 / 16.0F, 1.0 / 16.0F);
        heldItemRenderer.renderItem(livingEntity, itemStack, ItemDisplayContext.GROUND, false, matrixStack, vertexConsumerProvider, i);
        matrixStack.popPose();
    }

    private void renderShieldOnBack(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, T livingEntity, ItemStack itemStack, boolean isLeftHand) {
        matrixStack.pushPose();
        var eR = (AvatarRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(livingEntity);
        var body = eR.getModel().body;
        body.translateAndRotate(matrixStack);
        // --- 格挡时盾牌的调整 ---
        matrixStack.translate(-0.7f, 1.2f, 0.8f);
        matrixStack.mulPose(Axis.XP.rotationDegrees(180.0f - 20.0f));
        matrixStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(-75.0f));
        matrixStack.scale(1.2f, 1.2f, 1.2f);
        // --- 调整结束 ---
        heldItemRenderer.renderItem(livingEntity, itemStack, ItemDisplayContext.FIXED, isLeftHand, matrixStack, vertexConsumerProvider, i);
        matrixStack.popPose();
    }

    private void renderDefaultItemOnBack(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, T livingEntity, ItemStack itemStack) {
        matrixStack.pushPose();
        var eR = (AvatarRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(livingEntity);
        var body = eR.getModel().body;
        body.translateAndRotate(matrixStack);
        matrixStack.translate(0.0F, 0.5F, 0.25F);
        matrixStack.scale(1.5F, 1.5F, 1.5F);
        heldItemRenderer.renderItem(livingEntity, itemStack, ItemDisplayContext.GROUND, false, matrixStack, vertexConsumerProvider, i);
        matrixStack.popPose();
    }
}