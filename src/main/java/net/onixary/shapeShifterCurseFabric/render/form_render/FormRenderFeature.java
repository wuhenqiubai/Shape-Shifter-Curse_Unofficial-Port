package net.onixary.shapeShifterCurseFabric.render.form_render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerModelType;
import net.onixary.shapeShifterCurseFabric.integration.EMFIntegration;
import net.onixary.shapeShifterCurseFabric.player_form.PlayerFormBodyType;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import software.bernie.geckolib.cache.model.BakedGeoModel;
import software.bernie.geckolib.cache.model.GeoBone;
import software.bernie.geckolib.renderer.base.BoneSnapshots;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.RenderPassInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 1.21.11 渲染管线（submit）适配版 FormRenderFeature。
 * 主渲染通过 GL5 GeoObjectRenderer.performRenderPass 走 SubmitNodeCollector，
 * 骨骼操作在 GL5 的 adjustModelBonesForRender（BoneUpdater）钩子中执行。
 */
public class FormRenderFeature<S extends EntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
    public FormRenderFeature(RenderLayerParent<S, M> context) {
        super(context);
    }

    private static final boolean IS_FIRST_PERSON_MOD_LOADED = FabricLoader.getInstance().isModLoaded("firstperson");
    private static final boolean IRISInstalled = FabricLoader.getInstance().isModLoaded("iris");
    private static final boolean ImmediatelyFastInstalled = FabricLoader.getInstance().isModLoaded("immediatelyfast");

    private static boolean isSlim(AbstractClientPlayer player) {
        return player.getSkin().model() == PlayerModelType.SLIM;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, S entityRenderState, float f, float g) {
        if (!(entityRenderState instanceof AvatarRenderState avatarRenderState)) return;
        Entity entity = Minecraft.getInstance().level.getEntity(avatarRenderState.id);
        if (!(entity instanceof AbstractClientPlayer player)) return;
        if (player.isInvisible() || player.isSpectator()) return;
        if (!(getParentModel() instanceof PlayerModel playerModel)) return;
        AvatarRenderer avatarRenderer = (AvatarRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
        boolean slim = isSlim(player);

        // EMF 暂停（feral 形态/第一人称时暂停 EMF 动画）
        boolean pause = FormTextureUtils.getPlayerForm_Render(player).getBodyType() == PlayerFormBodyType.FERAL;
        if (!pause && IS_FIRST_PERSON_MOD_LOADED
                && Minecraft.getInstance().options.getCameraType().isFirstPerson()
                && player == Minecraft.getInstance().player) {
            pause = true;
        }
        if (pause) EMFIntegration.pauseAllAnimations(player);

        // 隐藏 vanilla 部件（form 模型覆盖时）
        rM_PartA(avatarRenderer, player, playerModel, poseStack, i);

        boolean hasOutline = Minecraft.getInstance().shouldEntityAppearGlowing(player);
        List<FormRenderer> formRendererList = FormRenderUtils.getPlayerAllFormRenderer(player);
        CameraRenderState cameraState = Minecraft.getInstance().gameRenderer.getLevelRenderState().cameraRenderState;
        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float limbAngle = avatarRenderState.walkAnimationPos;
        float limbDistance = avatarRenderState.walkAnimationSpeed;

        // beforeRender（tailDrag 插值等）
        for (FormRenderer formRenderer : formRendererList) {
            if (formRenderer == null) continue;
            formRenderer.realModel.AnimationSystem.beforeRender(formRenderer, formRenderer.realModel, avatarRenderer, player, poseStack, null, i, limbAngle, limbDistance, partialTick, 0f, f, g);
        }

        for (FormRenderer formRenderer : formRendererList) {
            if (formRenderer == null) {
                continue;
            }
            FormModel formModel = formRenderer.realModel;
            FormAnimatable formAnimatable = formRenderer.realAnimatable;
            formRenderer.setPlayer(player, slim);

            poseStack.pushPose();
            poseStack.mulPose(new Quaternionf().rotateX(180 * Mth.DEG_TO_RAD));
            poseStack.translate(0, -1.51f, 0);
            poseStack.translate(-0.5, -0.5, -0.5);

            // 主通道
            submitPass(formRenderer, formAnimatable, poseStack, submitNodeCollector, cameraState, i, partialTick,
                    FormRenderer.CHANNEL_NORMAL, player, limbAngle, limbDistance, f, g);
            // fullbright 通道
            if (formModel.getFullBrightTextureResource(slim) != null) {
                submitPass(formRenderer, formAnimatable, poseStack, submitNodeCollector, cameraState, Integer.MAX_VALUE - 1, partialTick,
                        FormRenderer.CHANNEL_FULLBRIGHT, player, limbAngle, limbDistance, f, g);
            }
            // outline 通道
            if (hasOutline) {
                submitPass(formRenderer, formAnimatable, poseStack, submitNodeCollector, cameraState, i, partialTick,
                        FormRenderer.CHANNEL_OUTLINE, player, limbAngle, limbDistance, f, g);
            }

            poseStack.popPose();
        }

        // afterRender + finishRender
        for (FormRenderer formRenderer : formRendererList) {
            if (formRenderer == null) continue;
            formRenderer.realModel.AnimationSystem.afterRender(formRenderer, formRenderer.realModel, avatarRenderer, player, poseStack, null, i, limbAngle, limbDistance, partialTick, 0f, f, g);
            formRenderer.realModel.AnimationSystem.finishRender(formRenderer, formRenderer.realModel, avatarRenderer, player, poseStack, null, i, limbAngle, limbDistance, partialTick, 0f, f, g);
        }

        // overlay/emissive 纹理（vanilla PlayerModel 额外通道）
        rM_PartB(avatarRenderer, player, playerModel, avatarRenderState, poseStack, submitNodeCollector, i);

        if (pause) EMFIntegration.resumeAnimations(player);
    }

    private void submitPass(FormRenderer formRenderer, FormAnimatable formAnimatable, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                            CameraRenderState cameraState, int light, float partialTick, int channel, Player player,
                            float limbAngle, float limbDistance, float headYaw, float headPitch) {
        GeoRenderState.Impl rs = formRenderer.createRenderState(formAnimatable, null);
        formRenderer.fillRenderState(formAnimatable, null, rs, partialTick);
        rs.addGeckolibData(FormRenderer.TICKET_PLAYER, player);
        rs.addGeckolibData(FormRenderer.TICKET_CHANNEL, channel);
        rs.addGeckolibData(FormRenderer.TICKET_LIMB_ANGLE, limbAngle);
        rs.addGeckolibData(FormRenderer.TICKET_LIMB_DISTANCE, limbDistance);
        rs.addGeckolibData(FormRenderer.TICKET_HEAD_YAW, headYaw);
        rs.addGeckolibData(FormRenderer.TICKET_HEAD_PITCH, headPitch);
        formRenderer.performRenderPass(rs, poseStack, submitNodeCollector, cameraState, null);
    }

    // 处理 BonePart 隐藏（vanilla PlayerModel 部件）
    public static void rM_PartA(AvatarRenderer playerEntityRenderer, AbstractClientPlayer player, PlayerModel playerEntityModel, PoseStack matrixStack, int i) {
        if (player.isSpectator()) {
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
            return;
        }
        List<FormRenderer> formRendererList = FormRenderUtils.getPlayerAllFormRenderer(player);
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

    // 渲染 vanilla PlayerModel 的额外纹理（form overlay/emissive），1.21.11 submit 模式
    public static void rM_PartB(AvatarRenderer playerEntityRenderer, AbstractClientPlayer player, PlayerModel playerEntityModel,
                                AvatarRenderState avatarRenderState, PoseStack matrixStack, SubmitNodeCollector submitNodeCollector, int i) {
        int p = LivingEntityRenderer.getOverlayCoords(avatarRenderState, 0.0F);
        if (player.isSpectator()) {
            return;
        }
        boolean bl2 = avatarRenderState.isInvisibleToPlayer;
        int color = bl2 ? 0x26FFFFFF : 0xFFFFFFFF;
        List<FormRenderer> formRendererList = FormRenderUtils.getPlayerAllFormRenderer(player);
        boolean slim = isSlim(player);
        for (FormRenderer formRenderer : formRendererList) {
            FormModel formModel = (FormModel) formRenderer.getGeoModel();
            Identifier overlayTexture = formModel.getOverlayTextureResource(slim);
            Identifier emissiveTexture = formModel.getEmissiveTextureResource(slim);
            if (overlayTexture != null) {
                RenderType l = (FormRenderUtils.isRenderingInWorld && IRISInstalled) || ImmediatelyFastInstalled
                        ? RenderTypes.entityCutoutNoCullZOffset(overlayTexture)
                        : RenderTypes.entityCutout(overlayTexture);
                submitNodeCollector.submitModel(playerEntityModel, avatarRenderState, matrixStack, l, i, p, color, null, avatarRenderState.outlineColor, null);
            }
            if (emissiveTexture != null) {
                RenderType l = RenderTypes.entityTranslucentEmissive(emissiveTexture);
                submitNodeCollector.submitModel(playerEntityModel, avatarRenderState, matrixStack, l, i, p, color, null, avatarRenderState.outlineColor, null);
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

    public static void rFPM_PartA(AvatarRenderer playerEntityRenderer, AbstractClientPlayer player, ModelPart arm, ModelPart sleeve) {
        List<FormRenderer> formRendererList = FormRenderUtils.getPlayerAllFormRenderer(player);
        PlayerModel playerEntityModel = (PlayerModel) playerEntityRenderer.getModel();
        boolean IsRenderRight = arm.equals(playerEntityModel.rightArm);
        boolean ArmHidden = false;
        boolean SleeveHidden = IsRenderRight ? !player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE) : !player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
        for (FormRenderer formRenderer : formRendererList) {
            FormModel formModel = (FormModel) formRenderer.getGeoModel();
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

    // 第一人称手臂：自定义手动渲染（GL5 BoneSnapshot + 隐藏非手臂骨骼 + positionAndRender 语义）
    public static void rFPM_PartB(AvatarRenderer playerEntityRenderer, PoseStack matrices, SubmitNodeCollector submitNodeCollector, int light, AbstractClientPlayer player, ModelPart arm, ModelPart sleeve) {
        boolean IsRenderRight = arm.equals(((PlayerModel) playerEntityRenderer.getModel()).rightArm);
        List<FormRenderer> formRendererList = FormRenderUtils.getPlayerAllFormRenderer(player);
        CameraRenderState cameraState = Minecraft.getInstance().gameRenderer.getLevelRenderState().cameraRenderState;
        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        boolean slim = isSlim(player);
        for (FormRenderer formRenderer : formRendererList) {
            if (formRenderer == null) {
                return;
            }
            FormModel formModel = (FormModel) formRenderer.getGeoModel();
            FormAnimatable formAnimatable = formRenderer.realAnimatable;
            formRenderer.setPlayer(player, slim);

            // 先确定手臂骨骼（缓存查找，不依赖 render pass）
            GeoBone armGeoBone = formModel.AnimationSystem.beforeRenderFirstPerson(null, formRenderer, formModel, playerEntityRenderer, player, arm, sleeve);
            String armBoneName = armGeoBone != null ? armGeoBone.name() : null;

            matrices.pushPose();
            matrices.mulPose(new Quaternionf().rotateX(180 * Mth.DEG_TO_RAD));
            matrices.translate(0, -1.51f, 0);

            // 正常通道 + fullbright 通道（单骨骼渲染）
            submitArmPass(formRenderer, formAnimatable, matrices, submitNodeCollector, cameraState, light, partialTick,
                    FormRenderer.CHANNEL_NORMAL, player, armBoneName, armGeoBone, arm, sleeve, playerEntityRenderer);
            if (formModel.getFullBrightTextureResource(slim) != null) {
                submitArmPass(formRenderer, formAnimatable, matrices, submitNodeCollector, cameraState, Integer.MAX_VALUE - 1, partialTick,
                        FormRenderer.CHANNEL_FULLBRIGHT, player, armBoneName, armGeoBone, arm, sleeve, playerEntityRenderer);
            }

            matrices.popPose();

            // Overlay 纹理（vanilla 手臂 overlay）
            Identifier OverlayTextureID = formModel.getOverlayTextureResource(slim);
            if (OverlayTextureID != null) {
                RenderType OverlayLayer = (FormRenderUtils.isRenderingInWorld && IRISInstalled) || ImmediatelyFastInstalled
                        ? RenderTypes.entityCutoutNoCullZOffset(OverlayTextureID)
                        : RenderTypes.entityCutout(OverlayTextureID);
                float animProgress = player.hurtTime > 0 ? (float) player.hurtTime - partialTick : 0;
                int OverlayInt = OverlayTexture.pack(OverlayTexture.u(animProgress), OverlayTexture.v(player.hurtTime > 0 || player.deathTime > 0));
                submitNodeCollector.submitModelPart(arm, matrices, OverlayLayer, light, OverlayInt, null);
            }
            formModel.AnimationSystem.afterRenderFirstPerson(armGeoBone, formRenderer, formModel, playerEntityRenderer, player, arm, sleeve);
        }
    }

    private static void submitArmPass(FormRenderer formRenderer, FormAnimatable formAnimatable, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                                      CameraRenderState cameraState, int light, float partialTick, int channel, Player player,
                                      String armBoneName, @Nullable GeoBone armGeoBone, ModelPart arm, ModelPart sleeve, AvatarRenderer renderer) {
        GeoRenderState.Impl rs = formRenderer.createRenderState(formAnimatable, null);
        formRenderer.fillRenderState(formAnimatable, null, rs, partialTick);
        rs.addGeckolibData(FormRenderer.TICKET_PLAYER, player);
        rs.addGeckolibData(FormRenderer.TICKET_CHANNEL, channel);
        formRenderer.performRenderPass(rs, poseStack, submitNodeCollector, cameraState, (renderPassInfo, snapshots) -> {
            FormModel formModel = formRenderer.realModel;
            formModel.beginRenderPass(snapshots);
            try {
                if (armBoneName != null) {
                    hideNonArmBones(renderPassInfo, snapshots, armBoneName);
                }
                formModel.AnimationSystem.processAnimationFirstPerson(armGeoBone, formRenderer, formModel, renderer, player, arm, sleeve);
            } finally {
                formModel.endRenderPass();
            }
        });
    }

    // 隐藏模型中除指定手臂骨骼子树以外的所有骨骼（等价于 renderRecursively(armBone) 语义）
    private static void hideNonArmBones(RenderPassInfo<?> renderPassInfo, BoneSnapshots snapshots, String armBoneName) {
        BakedGeoModel model = renderPassInfo.model();
        GeoBone armBone = model.getBone(armBoneName).orElse(null);
        if (armBone == null) return;
        Set<GeoBone> ancestors = new HashSet<>();
        for (GeoBone b = armBone; b != null; b = b.parent()) {
            ancestors.add(b);
        }
        for (GeoBone topBone : model.topLevelBones()) {
            if (ancestors.contains(topBone)) {
                if (topBone != armBone) {
                    // 祖先骨骼：隐藏自身几何，但保留子树（手臂可见）
                    snapshots.get(topBone.name()).ifPresent(s -> s.skipRender(true));
                }
            } else {
                // 无关骨骼：完全隐藏
                snapshots.get(topBone.name()).ifPresent(s -> s.skipRender(true).skipChildrenRender(true));
            }
        }
    }
}