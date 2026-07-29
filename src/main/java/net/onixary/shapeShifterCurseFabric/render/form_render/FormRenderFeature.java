package net.onixary.shapeShifterCurseFabric.render.form_render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import software.bernie.geckolib.cache.model.BakedGeoModel;
import software.bernie.geckolib.cache.model.GeoBone;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import java.util.List;

import static net.onixary.shapeShifterCurseFabric.util.ClientUtils.isOpenInventoryScreen;

public class FormRenderFeature <T extends Player, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {
    public FormRenderFeature(RenderLayerParent<T, M> context) {
        super(context);
    }

    private static final boolean IS_FIRST_PERSON_MOD_LOADED = FabricLoader.getInstance().isModLoaded("firstperson");
    private static final boolean BetterCombatInstalled = FabricLoader.getInstance().isModLoaded("bettercombat");
    private static final boolean IRISInstalled = FabricLoader.getInstance().isModLoaded("iris");
    private static final boolean ImmediatelyFastInstalled = FabricLoader.getInstance().isModLoaded("immediatelyfast");

    @Override
    public void render(PoseStack matrices, MultiBufferSource vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (entity instanceof AbstractClientPlayer abstractClientPlayerEntity) {
            boolean hasOutline = Minecraft.getInstance().shouldEntityAppearGlowing(abstractClientPlayerEntity);
            if (Minecraft.getInstance().options.getCameraType().isFirstPerson() && IS_FIRST_PERSON_MOD_LOADED) {
                if (abstractClientPlayerEntity == Minecraft.getInstance().player) {
                    hasOutline = false;
                }
            }
            if (abstractClientPlayerEntity.isInvisible() || abstractClientPlayerEntity.isSpectator()) { return; }
            List<FormRenderer> formRendererList = FormRenderUtils.getPlayerAllFormRenderer(abstractClientPlayerEntity);
            for (FormRenderer formRenderer : formRendererList) {
                if (formRenderer == null) {
                    continue;
                }
                AvatarRenderer playerEntityRenderer = (AvatarRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(abstractClientPlayerEntity);
                PlayerModel playerEntityModel = (PlayerModel) playerEntityRenderer.getModel();
                FormModel formModel = (FormModel) formRenderer.getGeoModel();
                FormAnimatable formAnimatable = formRenderer.realAnimatable;
                formRenderer.setPlayer(abstractClientPlayerEntity, playerEntityModel.slim);
                matrices.pushPose();
                matrices.mulPose(new Quaternionf().rotateX(180 * Mth.DEG_TO_RAD));
                matrices.translate(0, -1.51f, 0);
                matrices.translate(-0.5, -0.5, -0.5);
                formModel.AnimationSystem.beforeRender(formRenderer, formModel, playerEntityRenderer, abstractClientPlayerEntity, matrices, vertexConsumers, light, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
                formModel.AnimationSystem.processAnimation(formRenderer, formModel, playerEntityRenderer, abstractClientPlayerEntity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
                // 渲染部分
                formRenderer.render(matrices, formAnimatable, vertexConsumers, RenderType.entityTranslucent(formModel.getTextureResource(formAnimatable)), null, light, tickDelta);
                if (formModel.getFullbrightTextureResource(formAnimatable) != null) {
                    // GeckoLib 的 handleAnimations 在第一次 render 时会覆盖 processAnimation 设的骨骼
                    // 第二次 render 虽然跳过了 handleAnimations（重渲染保护），但骨骼已经不对了
                    // 所以在两次 render 之间重新 processAnimation 恢复正确骨骼
                    formModel.AnimationSystem.processAnimation(formRenderer, formModel, playerEntityRenderer, abstractClientPlayerEntity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
                    formRenderer.render(matrices, formAnimatable, vertexConsumers, RenderType.entityTranslucentEmissive(formModel.getFullbrightTextureResource(formAnimatable)), null, Integer.MAX_VALUE - 1, tickDelta);
                }
                if (hasOutline) {
                    formRenderer.render(matrices, formAnimatable, vertexConsumers, RenderType.outline(formModel.getTextureResource(formAnimatable)), vertexConsumers.getBuffer(RenderType.outline(formModel.getTextureResource(formAnimatable))), light, tickDelta);
                }
                formModel.AnimationSystem.afterRender(formRenderer, formModel, playerEntityRenderer, abstractClientPlayerEntity, matrices, vertexConsumers, light, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
                matrices.popPose();
                formModel.AnimationSystem.finishRender(formRenderer, formModel, playerEntityRenderer, abstractClientPlayerEntity, matrices, vertexConsumers, light, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
            }
        }
    }

    // 处理 BonePart 隐藏
    public static void rM_PartA(AvatarRenderer playerEntityRenderer, AbstractClientPlayer player, float f, float g, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i) {
        if (player.isSpectator()) {
            PlayerModel model = (PlayerModel) playerEntityRenderer.getModel();
            model.hat.skipDraw = false;
            model.head.skipDraw = false;
            model.body.skipDraw = false;
            model.jacket.skipDraw = false;
            model.leftArm.skipDraw = false;
            model.leftSleeve.skipDraw = false;
            model.rightArm.skipDraw = false;
            model.rightSleeve.skipDraw = false;
            model.leftLeg.skipDraw = false;
            model.leftPants.skipDraw = false;
            model.rightLeg.skipDraw = false;
            model.rightPants.skipDraw = false;
            return;
        }
        List<FormRenderer> formRendererList = FormRenderUtils.getPlayerAllFormRenderer(player);
        PlayerModel playerEntityModel = (PlayerModel) playerEntityRenderer.getModel();
        boolean hatHidden = !player.isModelPartShown(PlayerModelPart.HAT);
        boolean headHidden = false;
        boolean bodyHidden = false;
        boolean jacketHidden = !player.isModelPartShown(PlayerModelPart.JACKET);
        boolean leftArmHidden = false;
        boolean leftSleeveHidden = !player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
        boolean rightArmHidden = false;
        boolean rightSleeveHidden = !player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
        boolean leftLegHidden = false;
        boolean leftPantsHidden = !player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
        boolean rightLegHidden = false;
        boolean rightPantsHidden = !player.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
        // Better Combat 修复
//        if (FirstPersonMode.isFirstPersonPass() && ClientConfig.enableBetterCombatFix && player == MinecraftClient.getInstance().getCameraEntity()) {
//            AnimationApplier animationApplier = ((IAnimatedPlayer) player).playerAnimator_getAnimation();
//            FirstPersonConfiguration config = animationApplier.getFirstPersonConfiguration();
//            hatHidden = true;
//            headHidden = true;
//            bodyHidden = true;
//            jacketHidden = true;
//            if (!config.isShowLeftArm()) {
//                leftArmHidden = true;
//                leftSleeveHidden = true;
//            }
//            if (!config.isShowRightArm()) {
//                rightArmHidden = true;
//                rightSleeveHidden = true;
//            }
//            leftLegHidden = true;
//            leftPantsHidden = true;
//            rightLegHidden = true;
//            rightPantsHidden = true;
//        }
        for (FormRenderer formRenderer : formRendererList) {
            FormModel formModel = (FormModel) formRenderer.getGeoModel();
            hatHidden |= formModel.Hidden_Hat;
            headHidden |= formModel.Hidden_Head;
            bodyHidden |= formModel.Hidden_Body;
            jacketHidden |= formModel.Hidden_Jacket;
            leftArmHidden |= formModel.Hidden_LeftArm;
            leftSleeveHidden |= formModel.Hidden_LeftSleeve;
            rightArmHidden |= formModel.Hidden_RightArm;
            rightSleeveHidden |= formModel.Hidden_RightSleeve;
            leftLegHidden |= formModel.Hidden_LeftLeg;
            leftPantsHidden |= formModel.Hidden_LeftPants;
            rightLegHidden |= formModel.Hidden_RightLeg;
            rightPantsHidden |= formModel.Hidden_RightPants;
        }
        playerEntityModel.hat.visible = !hatHidden;
        playerEntityModel.head.visible = !headHidden;
        playerEntityModel.body.visible = !bodyHidden;
        playerEntityModel.jacket.visible = !jacketHidden;
        playerEntityModel.leftArm.visible = !leftArmHidden;
        playerEntityModel.leftSleeve.visible = !leftSleeveHidden;
        playerEntityModel.rightArm.visible = !rightArmHidden;
        playerEntityModel.rightSleeve.visible = !rightSleeveHidden;
        playerEntityModel.leftLeg.visible = !leftLegHidden;
        playerEntityModel.leftPants.visible = !leftPantsHidden;
        playerEntityModel.rightLeg.visible = !rightLegHidden;
        playerEntityModel.rightPants.visible = !rightPantsHidden;
    }

    public static void rM_PartB(AvatarRenderer playerEntityRenderer, AbstractClientPlayer player, float f, float g, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i) {
        int p = OverlayTexture.pack(OverlayTexture.u(playerEntityRenderer.getWhiteOverlayProgress(player, g)), OverlayTexture.v(player.hurtTime > 0 || player.deathTime > 0));
        if (player.isSpectator()) {
            return;
        }
        List<FormRenderer> formRendererList = FormRenderUtils.getPlayerAllFormRenderer(player);
        for (FormRenderer formRenderer : formRendererList) {
            PlayerModel<AbstractClientPlayer> playerEntityModel = playerEntityRenderer.getModel();
            FormModel formModel = (FormModel) formRenderer.getGeoModel();
            Identifier overlayTexture = formModel.getOverlayTextureResource(playerEntityModel.slim);
            Identifier emissiveTexture = formModel.getEmissiveTextureResource(playerEntityModel.slim);
            boolean bl = playerEntityRenderer.isBodyVisible(player);
            boolean bl2 = !bl && !player.isInvisibleTo(Minecraft.getInstance().player);
            if (overlayTexture != null) {
                RenderType l = null;
                if ((FormRenderUtils.isRenderingInWorld && IRISInstalled) || ImmediatelyFastInstalled) {
                    l = RenderType.entityCutoutNoCullZOffset(overlayTexture);
                } else {
                    l = RenderType.entityCutout(overlayTexture);
                }
                if (ImmediatelyFastInstalled && isOpenInventoryScreen) {
                    matrixStack.pushPose();
                    matrixStack.scale(1.02f, 1.02f, 1.02f);
                    playerEntityModel.renderToBuffer(matrixStack, vertexConsumerProvider.getBuffer(l), i, p, bl2 ? 0x26FFFFFF : 0xFFFFFFFF);
                    matrixStack.popPose();
                } else {
                    playerEntityModel.renderToBuffer(matrixStack, vertexConsumerProvider.getBuffer(l), i, p, bl2 ? 0x26FFFFFF : 0xFFFFFFFF);
                }
            }
            if (emissiveTexture != null) {
                RenderType l = RenderType.entityTranslucentEmissive(emissiveTexture);
                playerEntityModel.renderToBuffer(matrixStack, vertexConsumerProvider.getBuffer(l), i, p, bl2 ? 0x26FFFFFF : 0xFFFFFFFF);

            }
            playerEntityModel.hat.skipDraw = false;
            playerEntityModel.head.skipDraw = false;
            playerEntityModel.body.skipDraw = false;
            playerEntityModel.jacket.skipDraw = false;
            playerEntityModel.leftArm.skipDraw = false;
            playerEntityModel.leftSleeve.skipDraw = false;
            playerEntityModel.rightArm.skipDraw = false;
            playerEntityModel.rightSleeve.skipDraw = false;
            playerEntityModel.leftLeg.skipDraw = false;
            playerEntityModel.leftPants.skipDraw = false;
            playerEntityModel.rightLeg.skipDraw = false;
            playerEntityModel.rightPants.skipDraw = false;
        }
    }

    private static void renderGeoBone(FormRenderer formRenderer, GeoBone geoBone, PoseStack matrixStack, FormAnimatable formAnimatable, MultiBufferSource vertexConsumerProvider, RenderType renderLayer, VertexConsumer vertexConsumer, int packedLight) {
        renderGeoBone(formRenderer, geoBone, matrixStack, formAnimatable, vertexConsumerProvider, renderLayer, vertexConsumer, packedLight, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void renderGeoBone(FormRenderer formRenderer, GeoBone geoBone, PoseStack matrixStack, FormAnimatable formAnimatable, MultiBufferSource vertexConsumerProvider, RenderType renderLayer, VertexConsumer vertexConsumer, int packedLight, float R, float G, float B, float A) {
        FormModel formModel = (FormModel) formRenderer.getGeoModel();
        BakedGeoModel bakedGeoModel = formModel.getBakedModel(formModel.getModelResource((GeoRenderState) formAnimatable));
        float TickDelta = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        int packedOverlay = formRenderer.getPackedOverlay(formAnimatable, 0.0F, TickDelta);
        matrixStack.translate(-0.5, -0.51, -0.5); // 在 GeoObjectRenderer.preRender 中会 poseStack.translate(0.5, 0.51, 0.5) 因此需要手动调整
        int colour = ((int) (A * 255.0f) << 24) | ((int) (R * 255.0f) << 16) | ((int) (G * 255.0f) << 8) | ((int) (B * 255.0f));
        formRenderer.preRender(matrixStack, formAnimatable, bakedGeoModel, vertexConsumerProvider, vertexConsumer, false, TickDelta, packedLight, packedOverlay, colour);
        if (formRenderer.firePreRenderEvent(matrixStack, bakedGeoModel, vertexConsumerProvider, TickDelta, packedLight)) {
            formRenderer.preApplyRenderLayers(matrixStack, formAnimatable, bakedGeoModel, renderLayer, vertexConsumerProvider, vertexConsumer, (float)packedLight, packedLight, packedOverlay);
            matrixStack.pushPose();
            formRenderer.updateAnimatedTextureFrame(formAnimatable);
            formRenderer.renderRecursively(matrixStack, formAnimatable, geoBone, renderLayer, vertexConsumerProvider, vertexConsumer, false, TickDelta, packedLight, packedOverlay, colour);
            matrixStack.popPose();
            formRenderer.applyRenderLayers(matrixStack, formAnimatable, bakedGeoModel, renderLayer, vertexConsumerProvider, vertexConsumer, TickDelta, packedLight, packedOverlay);
            formRenderer.postRender(matrixStack, formAnimatable, bakedGeoModel, vertexConsumerProvider, vertexConsumer, false, TickDelta, packedLight, packedOverlay, colour);
            formRenderer.firePostRenderEvent(matrixStack, bakedGeoModel, vertexConsumerProvider, TickDelta, packedLight);
        }
    }

    public static void rFPM_PartA(AvatarRenderer playerEntityRenderer, PoseStack matrices, MultiBufferSource vertexConsumers, int light, AbstractClientPlayer player, ModelPart arm, ModelPart sleeve) {
        List<FormRenderer> formRendererList = FormRenderUtils.getPlayerAllFormRenderer(player);
        boolean IsRenderRight = arm.equals(playerEntityRenderer.getModel().rightArm);
        boolean ArmHidden = false;
        boolean SleeveHidden = IsRenderRight ? !player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE) : !player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
        for (FormRenderer formRenderer : formRendererList) {
            FormModel formModel = (FormModel) formRenderer.getGeoModel();
            // 设置手臂组件是否显示
            if (IsRenderRight) {
                ArmHidden |= formModel.Hidden_RightArm;
                SleeveHidden |= formModel.Hidden_RightSleeve;
            } else {
                ArmHidden |= formModel.Hidden_LeftArm;
                SleeveHidden |= formModel.Hidden_LeftSleeve;
            }
        }
        arm.visible = !ArmHidden;
        sleeve.visible = !SleeveHidden;
    }

    public static void rFPM_PartB(AvatarRenderer playerEntityRenderer, PoseStack matrices, MultiBufferSource vertexConsumers, int light, AbstractClientPlayer player, ModelPart arm, ModelPart sleeve) {
        boolean IsRenderRight = arm.equals(playerEntityRenderer.getModel().rightArm);
        List<FormRenderer> formRendererList = FormRenderUtils.getPlayerAllFormRenderer(player);
        for (FormRenderer formRenderer : formRendererList) {
            @Nullable GeoBone geoBone = null;
            if (formRenderer == null) {return;}
            PlayerModel<AbstractClientPlayer> playerEntityModel = playerEntityRenderer.getModel();
            FormModel formModel = (FormModel) formRenderer.getGeoModel();
            FormAnimatable formAnimatable = formRenderer.realAnimatable;
            formRenderer.setPlayer(player, playerEntityModel.slim);
            matrices.pushPose();
            matrices.mulPose(new Quaternionf().rotateX(180 * Mth.DEG_TO_RAD));
            matrices.translate(0, -1.51f, 0);
            geoBone = formModel.AnimationSystem.beforeRenderFirstPerson(geoBone, formRenderer, formModel, playerEntityRenderer, player, arm, sleeve);
            geoBone = formModel.AnimationSystem.processAnimationFirstPerson(geoBone, formRenderer, formModel, playerEntityRenderer, player, arm, sleeve);
            if (geoBone == null) {
                formModel.AnimationSystem.afterRenderFirstPerson(geoBone, formRenderer, formModel, playerEntityRenderer, player, arm, sleeve);
                matrices.popPose();
                continue;
            }
            RenderType renderLayerNormal = RenderType.entityTranslucent(formModel.getTextureResource(formAnimatable));
            renderGeoBone(formRenderer, geoBone, matrices, formAnimatable, vertexConsumers, renderLayerNormal, vertexConsumers.getBuffer(renderLayerNormal), light);
            RenderType renderLayerFullBright = RenderType.entityTranslucent(formModel.getFullbrightTextureResource(formAnimatable));
            renderGeoBone(formRenderer, geoBone, matrices, formAnimatable, vertexConsumers, renderLayerFullBright, vertexConsumers.getBuffer(renderLayerFullBright), Integer.MAX_VALUE - 1);
            matrices.popPose();

            // Render Overlay 藏得够深的 要不是发现悦灵手臂无法显示我都不会发现
            // 从 PlayerEntityRendererMixin.renderOverlayTexture 提取的代码并进行修改
            Identifier OverlayTextureID = formModel.getOverlayTextureResource(playerEntityModel.slim);
            if (OverlayTextureID != null) {
                // 玩家看自己绝对是非隐身
                // boolean bl = this.isVisible(player);
                // boolean bl2 = !bl && !player.isInvisibleTo(MinecraftClient.getInstance().player);
                RenderType OverlayLayer = null;
                if ((FormRenderUtils.isRenderingInWorld && IRISInstalled) || ImmediatelyFastInstalled) {
                    OverlayLayer = RenderType.entityCutoutNoCullZOffset(OverlayTextureID);
                } else {
                    OverlayLayer = RenderType.entityCutout(OverlayTextureID);
                }
                float animProgress = player.hurtTime > 0 ? (float) player.hurtTime - Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true) : 0;
                int OverlayInt = OverlayTexture.pack(OverlayTexture.u(animProgress), OverlayTexture.v(player.hurtTime > 0 || player.deathTime > 0));
                arm.render(matrices, vertexConsumers.getBuffer(OverlayLayer), light, OverlayInt);
            }
            formModel.AnimationSystem.afterRenderFirstPerson(geoBone, formRenderer, formModel, playerEntityRenderer, player, arm, sleeve);
        }
    }
}