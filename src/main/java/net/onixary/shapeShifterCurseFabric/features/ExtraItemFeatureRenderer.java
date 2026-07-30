package net.onixary.shapeShifterCurseFabric.features;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.tr7zw.firstperson.FirstPersonModelCore;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.PlayerFormBodyType;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public class ExtraItemFeatureRenderer <T extends EntityRenderState, M extends EntityModel<T>> extends RenderLayer<T, M> {
    //private final HeldItemRenderer heldItemRenderer;
    private final CustomFeralItemRenderer customFeralItemRenderer;
    private static final boolean IS_FIRST_PERSON_MOD_LOADED = FabricLoader.getInstance().isModLoaded("firstperson");
    private static boolean IS_FIRST_PERSON_MOD_NEW_VERSION = false;
    private static boolean IS_FIRST_PERSON_MOD_VERSION_CHECK_FAIL = false;

    public ExtraItemFeatureRenderer(RenderLayerParent<T, M> context, EntityRenderDispatcher entityRenderDispatcher, ItemInHandRenderer itemRenderer) {
        super(context);
        this.customFeralItemRenderer = new CustomFeralItemRenderer(Minecraft.getInstance(), entityRenderDispatcher, itemRenderer);
    }

    static {
        if (IS_FIRST_PERSON_MOD_LOADED) {
            try {
                Optional<ModContainer> FPM_Container = FabricLoader.getInstance().getModContainer("firstperson");
                if (FPM_Container.isPresent()) {
                    IS_FIRST_PERSON_MOD_NEW_VERSION = FPM_Container.get().getMetadata().getVersion().compareTo(Version.parse("2.6.0")) >= 0;
                }
            } catch (Exception e) {
                ShapeShifterCurseFabric.LOGGER.error("Failed to check FirstPerson Mod version");
                IS_FIRST_PERSON_MOD_VERSION_CHECK_FAIL = true;
            }
        }
    }

    @Override
    public void submit(
		    @NonNull PoseStack matrices,
		    @NonNull SubmitNodeCollector vertexConsumers,
		    int light,
		    @NonNull T renderState,
		    float limbAngle,
		    float limbDistance
    ) {

        Minecraft mc = Minecraft.getInstance();
        AbstractClientPlayer player = mc.player;
        if (player != null) {
            IForm curForm = FormTextureUtils.getPlayerForm_Render(player);
            boolean isFeral = curForm.getBodyType() == PlayerFormBodyType.FERAL;

            if (isFeral && Minecraft.getInstance().options.getCameraType().isFirstPerson() && player == Minecraft.getInstance().player) {

                if(IS_FIRST_PERSON_MOD_LOADED) {
                    // Feral形态的forstperson配置必须固定为-25 offset，否则会导致物品位置不正确以及模型看不到
                    FirstPersonModelCore fpm = FirstPersonModelCore.instance;
                    if (ShapeShifterCurseFabric.clientConfig.enableChangeFPMConfig) {
                        if (!IS_FIRST_PERSON_MOD_VERSION_CHECK_FAIL) {
                            if (IS_FIRST_PERSON_MOD_NEW_VERSION) {
                                fpm.getConfig().xOffset = 12;
                                fpm.getConfig().sitXOffset = 12;
                                fpm.getConfig().sneakXOffset = 12;
                            } else {
                                fpm.getConfig().xOffset = -25;
                                fpm.getConfig().sitXOffset = -25;
                                fpm.getConfig().sneakXOffset = -25;
                            }
                        }
                    }

                    // 已知Bug 在开启 FirstPersonModel 并启用时 且在第一人称时 物品位置不对
                    if (fpm.isEnabled()) {
                        // 仅限开启FirstPersonModel时渲染额外物品
                        matrices.pushPose();
                        //var eR = (PlayerEntityRenderer) MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(player);
                        //var head = ((PlayerModel)eR.getModel()).head;
                        // FirstPerson Mod会直接将head.pivot移动到某个非常远的位置来“隐藏”头部，所以需要直接定义好一个固定位置
                        // Vec3d posOffset = new Vec3d(0.0F, 0.0F, 0.0F);
                        // Vec3d rotCenter = ShapeShifterCurseFabric.feralItemCenter;
                        Vec3 rotCenter = new Vec3(0.0F, -4.0F, -6.0F);
                        matrices.translate(rotCenter.x / 16.0F, rotCenter.y / 16.0F, rotCenter.z / 16.0F);
                        //Vec3d posOffset = ShapeShifterCurseFabric.feralItemPosOffset;
                        Vec3 posOffset = new Vec3(-12.0F, 15.0F, 4.0F);
                        float headYaw = Mth.lerp(limbAngle, player.yHeadRotO, player.getYHeadRot());
                        float headPitch = Mth.lerp(limbAngle, player.xRotO, player.getXRot());
                        matrices.mulPose(Axis.YP.rotationDegrees(headYaw));
                        matrices.mulPose(Axis.XP.rotationDegrees(headPitch));
                        matrices.mulPose(Axis.ZP.rotationDegrees(240.0F));
                        matrices.translate(posOffset.x / 16.0F, posOffset.y / 16.0F, posOffset.z / 16.0F);
                        float pitch = Mth.lerp(limbAngle, player.xRotO, player.getXRot());
                        float equipProgress = 1.0F - Mth.lerp(limbAngle, customFeralItemRenderer.prevEquipProgressMainHand, customFeralItemRenderer.equipProgressMainHand);
                        // 调用第一人称物品渲染逻辑
                        customFeralItemRenderer.renderFirstPersonItem(
                                player,
                                limbAngle,
                                pitch,
                                InteractionHand.MAIN_HAND,
                                player.getAttackAnim(limbAngle),
                                player.getMainHandItem(),
                                equipProgress,
                                matrices,
                                (MultiBufferSource)vertexConsumers,
                                light
                        );
                        matrices.popPose();
                    }
                }
            }
        }
    }
}