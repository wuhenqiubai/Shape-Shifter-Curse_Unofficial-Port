package net.onixary.shapeShifterCurseFabric.render.form_render;

import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.onixary.shapeShifterCurseFabric.util.ClientUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.List;

public class FormRenderFeature <T extends PlayerEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> extends FeatureRenderer<T, M> {
    public FormRenderFeature(FeatureRendererContext<T, M> context) {
        super(context);
    }

    private static final boolean IS_FIRST_PERSON_MOD_LOADED = FabricLoader.getInstance().isModLoaded("firstperson");
    private static final boolean BetterCombatInstalled = FabricLoader.getInstance().isModLoaded("bettercombat");
    private static final boolean IRISInstalled = FabricLoader.getInstance().isModLoaded("iris");

    /** Reset hidden flags on vanilla model parts after rendering.
     *  Overlay/emissive texture rendering is handled by OverlayRenderMixin. */
    public static void rM_PartB(PlayerEntityRenderer playerEntityRenderer, AbstractClientPlayerEntity player, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        if (player.isSpectator()) return;
        PlayerEntityModel<?> m = playerEntityRenderer.getModel();
        m.hat.hidden = false; m.head.hidden = false; m.body.hidden = false;
        m.jacket.hidden = false; m.leftArm.hidden = false; m.leftSleeve.hidden = false;
        m.rightArm.hidden = false; m.rightSleeve.hidden = false;
        m.leftLeg.hidden = false; m.leftPants.hidden = false;
        m.rightLeg.hidden = false; m.rightPants.hidden = false;
    }

    /** Aggregate Hidden_* flags from all active form renderers and apply to the vanilla model. */
    private static void applyVisibility(PlayerEntity player, List<FormRenderer> formRendererList,
                                        PlayerEntityModel<AbstractClientPlayerEntity> model) {
        boolean hatH = !player.isPartVisible(PlayerModelPart.HAT);
        boolean headH = false, bodyH = false, leftArmH = false, rightArmH = false;
        boolean leftLegH = false, rightLegH = false;
        boolean jacketH = !player.isPartVisible(PlayerModelPart.JACKET);
        boolean leftSleeveH = !player.isPartVisible(PlayerModelPart.LEFT_SLEEVE);
        boolean rightSleeveH = !player.isPartVisible(PlayerModelPart.RIGHT_SLEEVE);
        boolean leftPantsH = !player.isPartVisible(PlayerModelPart.LEFT_PANTS_LEG);
        boolean rightPantsH = !player.isPartVisible(PlayerModelPart.RIGHT_PANTS_LEG);
        for (FormRenderer fr : formRendererList) {
            FormModel fm = (FormModel) fr.getGeoModel();
            hatH |= fm.Hidden_Hat;       headH |= fm.Hidden_Head;
            bodyH |= fm.Hidden_Body;     jacketH |= fm.Hidden_Jacket;
            leftArmH |= fm.Hidden_LeftArm;   rightArmH |= fm.Hidden_RightArm;
            leftSleeveH |= fm.Hidden_LeftSleeve; rightSleeveH |= fm.Hidden_RightSleeve;
            leftLegH |= fm.Hidden_LeftLeg;   rightLegH |= fm.Hidden_RightLeg;
            leftPantsH |= fm.Hidden_LeftPants; rightPantsH |= fm.Hidden_RightPants;
        }
        model.hat.visible = !hatH;
        model.head.visible = !headH;
        model.body.visible = !bodyH;
        model.jacket.visible = !jacketH;
        model.leftArm.visible = !leftArmH;
        model.leftSleeve.visible = !leftSleeveH;
        model.rightArm.visible = !rightArmH;
        model.rightSleeve.visible = !rightSleeveH;
        model.leftLeg.visible = !leftLegH;
        model.leftPants.visible = !leftPantsH;
        model.rightLeg.visible = !rightLegH;
        model.rightPants.visible = !rightPantsH;
    }

    // 处理 BonePart 隐藏
    public static void rM_PartA(PlayerEntityRenderer playerEntityRenderer, AbstractClientPlayerEntity player, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        if (player.isSpectator()) {
            PlayerEntityModel<?> model = playerEntityRenderer.getModel();
            model.hat.hidden = false;
            model.head.hidden = false;
            model.body.hidden = false;
            model.jacket.hidden = false;
            model.leftArm.hidden = false;
            model.leftSleeve.hidden = false;
            model.rightArm.hidden = false;
            model.rightSleeve.hidden = false;
            model.leftLeg.hidden = false;
            model.leftPants.hidden = false;
            model.rightLeg.hidden = false;
            model.rightPants.hidden = false;
            return;
        }
        List<FormRenderer> formRendererList = FormRenderUtils.getPlayerAllFormRenderer(player);
        PlayerEntityModel<AbstractClientPlayerEntity> playerEntityModel = playerEntityRenderer.getModel();
        applyVisibility(player, formRendererList, playerEntityModel);

        boolean IsClientNowPlayedPlayer = player instanceof ClientPlayerEntity;
        boolean IsFirstPersonView = MinecraftClient.getInstance().options.getPerspective().isFirstPerson();

        if (BetterCombatInstalled && IsFirstPersonView && IsClientNowPlayedPlayer && ClientUtils.ShouldEnableBetterCombatFix()) {
            playerEntityModel.hat.visible = false;
            playerEntityModel.head.visible = false;
        }

        if (IS_FIRST_PERSON_MOD_LOADED && IsFirstPersonView && IsClientNowPlayedPlayer
            && MinecraftClient.getInstance().currentScreen == null) {
            playerEntityModel.hat.visible = false;
            playerEntityModel.head.visible = false;
        }
    }

    private static void renderGeoBone(FormRenderer formRenderer, GeoBone geoBone, MatrixStack matrixStack, FormAnimatable formAnimatable, VertexConsumerProvider vertexConsumerProvider, RenderLayer renderLayer, VertexConsumer vertexConsumer, int packedLight, float R, float G, float B, float A) {
        FormModel formModel = (FormModel) formRenderer.getGeoModel();
        BakedGeoModel bakedGeoModel = formModel.getBakedModel(formModel.getModelResource(formAnimatable));
        float TickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true);
        int packedOverlay = formRenderer.getPackedOverlay(formAnimatable, 0.0F, TickDelta);
        matrixStack.translate(-0.5, -0.51, -0.5); // 在 GeoObjectRenderer.preRender 中会 poseStack.translate(0.5, 0.51, 0.5) 因此需要手动调整
        int colour = ((int) (A * 255.0f) << 24) | ((int) (R * 255.0f) << 16) | ((int) (G * 255.0f) << 8) | ((int) (B * 255.0f));
        formRenderer.preRender(matrixStack, formAnimatable, bakedGeoModel, vertexConsumerProvider, vertexConsumer, false, TickDelta, packedLight, packedOverlay, colour);
        if (formRenderer.firePreRenderEvent(matrixStack, bakedGeoModel, vertexConsumerProvider, TickDelta, packedLight)) {
            formRenderer.preApplyRenderLayers(matrixStack, formAnimatable, bakedGeoModel, renderLayer, vertexConsumerProvider, vertexConsumer, (float)packedLight, packedLight, packedOverlay);
            matrixStack.push();
            formRenderer.updateAnimatedTextureFrame(formAnimatable);
            formRenderer.renderRecursively(matrixStack, formAnimatable, geoBone, renderLayer, vertexConsumerProvider, vertexConsumer, false, TickDelta, packedLight, packedOverlay, colour);
            matrixStack.pop();
            formRenderer.applyRenderLayers(matrixStack, formAnimatable, bakedGeoModel, renderLayer, vertexConsumerProvider, vertexConsumer, TickDelta, packedLight, packedOverlay);
            formRenderer.postRender(matrixStack, formAnimatable, bakedGeoModel, vertexConsumerProvider, vertexConsumer, false, TickDelta, packedLight, packedOverlay, colour);
            formRenderer.firePostRenderEvent(matrixStack, bakedGeoModel, vertexConsumerProvider, TickDelta, packedLight);
        }
    }

    private static void renderGeoBone(FormRenderer formRenderer, GeoBone geoBone, MatrixStack matrixStack, FormAnimatable formAnimatable, VertexConsumerProvider vertexConsumerProvider, RenderLayer renderLayer, VertexConsumer vertexConsumer, int packedLight) {
        renderGeoBone(formRenderer, geoBone, matrixStack, formAnimatable, vertexConsumerProvider, renderLayer, vertexConsumer, packedLight, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void rFPM_PartB(PlayerEntityRenderer playerEntityRenderer, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, ModelPart arm, ModelPart sleeve) {
        boolean IsRenderRight = arm.equals(playerEntityRenderer.getModel().rightArm);
        List<FormRenderer> formRendererList = FormRenderUtils.getPlayerAllFormRenderer(player);
        for (FormRenderer formRenderer : formRendererList) {
            @Nullable GeoBone geoBone = null;
            if (formRenderer == null) {return;}
            PlayerEntityModel<AbstractClientPlayerEntity> playerEntityModel = playerEntityRenderer.getModel();
            FormModel formModel = (FormModel) formRenderer.getGeoModel();
            formRenderer.setPlayer(player, playerEntityModel.thinArms);
            FormAnimatable formAnimatable = formRenderer.getAnimatable();
            matrices.push();
            matrices.multiply(new Quaternionf().rotateX(180 * MathHelper.RADIANS_PER_DEGREE));
            matrices.translate(0, -1.51f, 0);
            geoBone = formModel.AnimationSystem.beforeRenderFirstPerson(geoBone, formRenderer, formModel, playerEntityRenderer, player, arm, sleeve);
            geoBone = formModel.AnimationSystem.processAnimationFirstPerson(geoBone, formRenderer, formModel, playerEntityRenderer, player, arm, sleeve);
            if (geoBone == null) {
                formModel.AnimationSystem.afterRenderFirstPerson(geoBone, formRenderer, formModel, playerEntityRenderer, player, arm, sleeve);
                matrices.pop();
                continue;
            }
            RenderLayer renderLayerNormal = RenderLayer.getEntityTranslucent(formModel.getTextureResource(formAnimatable));
            renderGeoBone(formRenderer, geoBone, matrices, formAnimatable, vertexConsumers, renderLayerNormal, vertexConsumers.getBuffer(renderLayerNormal), light);
            RenderLayer renderLayerFullBright = RenderLayer.getEntityTranslucent(formModel.getFullbrightTextureResource(formAnimatable));
            renderGeoBone(formRenderer, geoBone, matrices, formAnimatable, vertexConsumers, renderLayerFullBright, vertexConsumers.getBuffer(renderLayerFullBright), Integer.MAX_VALUE - 1);
            matrices.pop();

            // Render Overlay 藏得够深的 要不是发现悦灵手臂无法显示我都不会发现
            // 从 PlayerEntityRendererMixin.renderOverlayTexture 提取的代码并进行修改
            Identifier OverlayTextureID = formModel.getOverlayTextureResource(playerEntityModel.thinArms);
            if (OverlayTextureID != null) {
                RenderLayer OverlayLayer = null;
                if (FormRenderUtils.isRenderingInWorld && IRISInstalled) {
                    OverlayLayer = RenderLayer.getEntityCutout(OverlayTextureID);
                } else {
                    OverlayLayer = RenderLayer.getEntityCutout(OverlayTextureID);
                }
                float animProgress = player.hurtTime > 0 ? (float) player.hurtTime - MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true) : 0;
                int OverlayInt = OverlayTexture.packUv(OverlayTexture.getU(animProgress), OverlayTexture.getV(player.hurtTime > 0 || player.deathTime > 0));
                arm.render(matrices, vertexConsumers.getBuffer(OverlayLayer), light, OverlayInt);
            }
            formModel.AnimationSystem.afterRenderFirstPerson(geoBone, formRenderer, formModel, playerEntityRenderer, player, arm, sleeve);
        }
    }

    public static void rFPM_PartA(PlayerEntityRenderer playerEntityRenderer, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, ModelPart arm, ModelPart sleeve) {
        List<FormRenderer> formRendererList = FormRenderUtils.getPlayerAllFormRenderer(player);
        boolean IsRenderRight = arm.equals(playerEntityRenderer.getModel().rightArm);
        boolean ArmHidden = false;
        boolean SleeveHidden = IsRenderRight ? !player.isPartVisible(PlayerModelPart.RIGHT_SLEEVE) : !player.isPartVisible(PlayerModelPart.LEFT_SLEEVE);
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

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (entity instanceof AbstractClientPlayerEntity abstractClientPlayerEntity) {
            boolean hasOutline = MinecraftClient.getInstance().hasOutline(abstractClientPlayerEntity);
            if (MinecraftClient.getInstance().options.getPerspective().isFirstPerson() && IS_FIRST_PERSON_MOD_LOADED) {
                if (abstractClientPlayerEntity == MinecraftClient.getInstance().player) {
                    hasOutline = false;
                }
            }
            if (abstractClientPlayerEntity.isInvisible() || abstractClientPlayerEntity.isSpectator()) { return; }
            List<FormRenderer> formRendererList = FormRenderUtils.getPlayerAllFormRenderer(abstractClientPlayerEntity);
            for (FormRenderer formRenderer : formRendererList) {
                if (formRenderer == null) {
                    continue;
                }
	            var _ed = MinecraftClient.getInstance().getEntityRenderDispatcher();
	            if (_ed == null) continue;
	            PlayerEntityRenderer playerEntityRenderer = (PlayerEntityRenderer) _ed.getRenderer(abstractClientPlayerEntity);
	            if (playerEntityRenderer == null) continue;
                PlayerEntityModel<AbstractClientPlayerEntity> playerEntityModel = playerEntityRenderer.getModel();
                FormModel formModel = (FormModel) formRenderer.getGeoModel();
                formRenderer.setPlayer(abstractClientPlayerEntity, playerEntityModel.thinArms);
                FormAnimatable formAnimatable = formRenderer.getAnimatable();
                matrices.push();
                matrices.multiply(new Quaternionf().rotateX(180 * MathHelper.RADIANS_PER_DEGREE));
                matrices.translate(0, -1.51f, 0);
                matrices.translate(-0.5, -0.5, -0.5);
                formModel.AnimationSystem.beforeRender(formRenderer, formModel, playerEntityRenderer, abstractClientPlayerEntity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
                // processAnimation is now deferred to handleAnimations (after GeckoLib AC)
                // Emissive pass handled by FormEmissiveLayer (registered on FormRenderer)
                formRenderer.render(matrices, formAnimatable, vertexConsumers, RenderLayer.getEntityTranslucent(formModel.getTextureResource(formAnimatable)), null, light, tickDelta);
                if (hasOutline) {
                    formRenderer.render(matrices, formAnimatable, vertexConsumers, RenderLayer.getOutline(formModel.getTextureResource(formAnimatable)), vertexConsumers.getBuffer(RenderLayer.getOutline(formModel.getTextureResource(formAnimatable))), light, tickDelta);
                }
                matrices.pop();
                formModel.AnimationSystem.afterRender(formRenderer, formModel, playerEntityRenderer, abstractClientPlayerEntity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
            }
        }
    }
}
