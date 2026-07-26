package net.onixary.shapeShifterCurseFabric.features;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class CustomFeralItemRenderer {
	private static final RenderType MAP_BACKGROUND = RenderType.text(ResourceLocation.parse("textures/map/map_background.png"));
	private static final RenderType MAP_BACKGROUND_CHECKERBOARD = RenderType.text(ResourceLocation.parse("textures/map/map_background_checkerboard.png"));
	private static final float field_32735 = -0.4F;
	private static final float field_32736 = 0.2F;
	private static final float field_32737 = -0.2F;
	private static final float field_32738 = -0.6F;
	private static final float EQUIP_OFFSET_TRANSLATE_X = 0.56F;
	private static final float EQUIP_OFFSET_TRANSLATE_Y = -0.52F;
	private static final float EQUIP_OFFSET_TRANSLATE_Z = -0.72F;
	private static final float field_32742 = 45.0F;
	private static final float field_32743 = -80.0F;
	private static final float field_32744 = -20.0F;
	private static final float field_32745 = -20.0F;
	private static final float EAT_OR_DRINK_X_ANGLE_MULTIPLIER = 10.0F;
	private static final float EAT_OR_DRINK_Y_ANGLE_MULTIPLIER = 90.0F;
	private static final float EAT_OR_DRINK_Z_ANGLE_MULTIPLIER = 30.0F;
	private static final float field_32749 = 0.6F;
	private static final float field_32750 = -0.5F;
	private static final float field_32751 = 0.0F;
	private static final double field_32752 = 27.0;
	private static final float field_32753 = 0.8F;
	private static final float field_32754 = 0.1F;
	private static final float field_32755 = -0.3F;
	private static final float field_32756 = 0.4F;
	private static final float field_32757 = -0.4F;
	private static final float ARM_HOLDING_ITEM_SECOND_Y_ANGLE_MULTIPLIER = 70.0F;
	private static final float ARM_HOLDING_ITEM_FIRST_Z_ANGLE_MULTIPLIER = -20.0F;
	private static final float field_32690 = -0.6F;
	private static final float field_32691 = 0.8F;
	private static final float field_32692 = 0.8F;
	private static final float field_32693 = -0.75F;
	private static final float field_32694 = -0.9F;
	private static final float field_32695 = 45.0F;
	private static final float field_32696 = -1.0F;
	private static final float field_32697 = 3.6F;
	private static final float field_32698 = 3.5F;
	private static final float ARM_HOLDING_ITEM_TRANSLATE_X = 5.6F;
	private static final int ARM_HOLDING_ITEM_X_ANGLE_MULTIPLIER = 200;
	private static final int ARM_HOLDING_ITEM_THIRD_Y_ANGLE_MULTIPLIER = -135;
	private static final int ARM_HOLDING_ITEM_SECOND_Z_ANGLE_MULTIPLIER = 120;
	private static final float field_32703 = -0.4F;
	private static final float field_32704 = -0.2F;
	private static final float field_32705 = 0.0F;
	private static final float field_32706 = 0.04F;
	private static final float field_32707 = -0.72F;
	private static final float field_32708 = -1.2F;
	private static final float field_32709 = -0.5F;
	private static final float field_32710 = 45.0F;
	private static final float field_32711 = -85.0F;
	private static final float ARM_X_ANGLE_MULTIPLIER = 45.0F;
	private static final float ARM_Y_ANGLE_MULTIPLIER = 92.0F;
	private static final float ARM_Z_ANGLE_MULTIPLIER = -41.0F;
	private static final float ARM_TRANSLATE_X = 0.3F;
	private static final float ARM_TRANSLATE_Y = -1.1F;
	private static final float ARM_TRANSLATE_Z = 0.45F;
	private static final float field_32718 = 20.0F;
	private static final float FIRST_PERSON_MAP_FIRST_SCALE = 0.38F;
	private static final float FIRST_PERSON_MAP_TRANSLATE_X = -0.5F;
	private static final float FIRST_PERSON_MAP_TRANSLATE_Y = -0.5F;
	private static final float FIRST_PERSON_MAP_TRANSLATE_Z = 0.0F;
	private static final float FIRST_PERSON_MAP_SECOND_SCALE = 0.0078125F;
	private static final int field_32724 = 7;
	private static final int field_32725 = 128;
	private static final int field_32726 = 128;
	private static final float field_32727 = 0.0F;
	private static final float field_32728 = 0.0F;
	private static final float field_32729 = 0.04F;
	private static final float field_32730 = 0.0F;
	private static final float field_32731 = 0.004F;
	private static final float field_32732 = 0.0F;
	private static final float field_32733 = 0.2F;
	private static final float field_32734 = 0.1F;
	private final Minecraft client;
	public ItemStack mainHand = ItemStack.EMPTY;
	private ItemStack offHand = ItemStack.EMPTY;
	public float equipProgressMainHand;
	public float prevEquipProgressMainHand;
	private float equipProgressOffHand;
	private float prevEquipProgressOffHand;
	private final EntityRenderDispatcher entityRenderDispatcher;
	private final ItemRenderer itemRenderer;

	public CustomFeralItemRenderer(Minecraft client, EntityRenderDispatcher entityRenderDispatcher, ItemRenderer itemRenderer) {
		this.client = client;
		this.entityRenderDispatcher = entityRenderDispatcher;
		this.itemRenderer = itemRenderer;
	}

	public void renderItem(
		LivingEntity entity,
		ItemStack stack,
		ItemDisplayContext renderMode,
		boolean leftHanded,
		PoseStack matrices,
		MultiBufferSource vertexConsumers,
		int light
	) {
		if (!stack.isEmpty()) {
			this.itemRenderer
				.renderStatic(
					entity,
					stack,
					renderMode,
					leftHanded,
					matrices,
					vertexConsumers,
					entity.level(),
					light,
					OverlayTexture.NO_OVERLAY,
					entity.getId() + renderMode.ordinal()
				);
		}
	}

	private float getMapAngle(float tickDelta) {
		float f = 1.0F - tickDelta / 45.0F + 0.1F;
		f = Mth.clamp(f, 0.0F, 1.0F);
		return -Mth.cos(f * (float) Math.PI) * 0.5F + 0.5F;
	}

	private void renderArm(PoseStack matrices, MultiBufferSource vertexConsumers, int light, HumanoidArm arm) {
		if (this.client.player == null) {
			return;
		}
		RenderSystem.setShaderTexture(0, this.client.player.getSkin().texture());
		PlayerRenderer playerEntityRenderer = (PlayerRenderer)this.entityRenderDispatcher.<AbstractClientPlayer>getRenderer(this.client.player);
		matrices.pushPose();
		float f = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
		matrices.mulPose(Axis.YP.rotationDegrees(92.0F));
		matrices.mulPose(Axis.XP.rotationDegrees(45.0F));
		matrices.mulPose(Axis.ZP.rotationDegrees(f * -41.0F));
		matrices.translate(f * 0.3F, -1.1F, 0.45F);
		if (arm == HumanoidArm.RIGHT) {
			playerEntityRenderer.renderRightHand(matrices, vertexConsumers, light, this.client.player);
		} else {
			playerEntityRenderer.renderLeftHand(matrices, vertexConsumers, light, this.client.player);
		}

		matrices.popPose();
	}

	private void renderMapInOneHand(
		PoseStack matrices, MultiBufferSource vertexConsumers, int light, float equipProgress, HumanoidArm arm, float swingProgress, ItemStack stack
	) {
		if (this.client.player == null) {
			return;
		}

		float f = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
		matrices.translate(f * 0.125F, -0.125F, 0.0F);
		if (!this.client.player.isInvisible()) {
			matrices.pushPose();
			matrices.mulPose(Axis.ZP.rotationDegrees(f * 10.0F));
			this.renderArmHoldingItem(matrices, vertexConsumers, light, equipProgress, swingProgress, arm);
			matrices.popPose();
		}

		matrices.pushPose();
		matrices.translate(f * 0.51F, -0.08F + equipProgress * -1.2F, -0.75F);
		float g = Mth.sqrt(swingProgress);
		float h = Mth.sin(g * (float) Math.PI);
		float i = -0.5F * h;
		float j = 0.4F * Mth.sin(g * (float) (Math.PI * 2));
		float k = -0.3F * Mth.sin(swingProgress * (float) Math.PI);
		matrices.translate(f * i, j - 0.3F * h, k);
		matrices.mulPose(Axis.XP.rotationDegrees(h * -45.0F));
		matrices.mulPose(Axis.YP.rotationDegrees(f * h * -30.0F));
		this.renderFirstPersonMap(matrices, vertexConsumers, light, stack);
		matrices.popPose();
	}

	private void renderMapInBothHands(
		PoseStack matrices, MultiBufferSource vertexConsumers, int light, float pitch, float equipProgress, float swingProgress
	) {
		if (this.client.player == null) {
			return;
		}

		float f = Mth.sqrt(swingProgress);
		float g = -0.2F * Mth.sin(swingProgress * (float) Math.PI);
		float h = -0.4F * Mth.sin(f * (float) Math.PI);
		matrices.translate(0.0F, -g / 2.0F, h);
		float i = this.getMapAngle(pitch);
		matrices.translate(0.0F, 0.04F + equipProgress * -1.2F + i * -0.5F, -0.72F);
		matrices.mulPose(Axis.XP.rotationDegrees(i * -85.0F));
		if (!this.client.player.isInvisible()) {
			matrices.pushPose();
			matrices.mulPose(Axis.YP.rotationDegrees(90.0F));
			this.renderArm(matrices, vertexConsumers, light, HumanoidArm.RIGHT);
			this.renderArm(matrices, vertexConsumers, light, HumanoidArm.LEFT);
			matrices.popPose();
		}

		float j = Mth.sin(f * (float) Math.PI);
		matrices.mulPose(Axis.XP.rotationDegrees(j * 20.0F));
		matrices.scale(2.0F, 2.0F, 2.0F);
		this.renderFirstPersonMap(matrices, vertexConsumers, light, this.mainHand);
	}

	private void renderFirstPersonMap(PoseStack matrices, MultiBufferSource vertexConsumers, int swingProgress, ItemStack stack) {
		if (this.client.level == null || stack == null || vertexConsumers == null) {
			return;
		}

		matrices.mulPose(Axis.YP.rotationDegrees(180.0F));
		matrices.mulPose(Axis.ZP.rotationDegrees(180.0F));
		matrices.scale(0.38F, 0.38F, 0.38F);
		matrices.translate(-0.5F, -0.5F, 0.0F);
		matrices.scale(0.0078125F, 0.0078125F, 0.0078125F);
		MapId mapId = stack.get(DataComponents.MAP_ID);
		MapItemSavedData mapState = MapItem.getSavedData(mapId, this.client.level);
		VertexConsumer vertexConsumer = vertexConsumers.getBuffer(mapState == null ? MAP_BACKGROUND : MAP_BACKGROUND_CHECKERBOARD);
		if (vertexConsumer == null || matrices.last() == null) {
			return;
		}
		Matrix4f matrix4f = matrices.last().pose();
		vertexConsumer.addVertex(matrix4f, -7.0F, 135.0F, 0.0F).setColor(255, 255, 255, 255).setUv(0.0F, 1.0F).setLight(swingProgress);
		vertexConsumer.addVertex(matrix4f, 135.0F, 135.0F, 0.0F).setColor(255, 255, 255, 255).setUv(1.0F, 1.0F).setLight(swingProgress);
		vertexConsumer.addVertex(matrix4f, 135.0F, -7.0F, 0.0F).setColor(255, 255, 255, 255).setUv(1.0F, 0.0F).setLight(swingProgress);
		vertexConsumer.addVertex(matrix4f, -7.0F, -7.0F, 0.0F).setColor(255, 255, 255, 255).setUv(0.0F, 0.0F).setLight(swingProgress);
		if (mapState != null) {
			this.client.gameRenderer.getMapRenderer().render(matrices, vertexConsumers, mapId, mapState, false, swingProgress);
		}
	}

	private void renderArmHoldingItem(PoseStack matrices, MultiBufferSource vertexConsumers, int light, float equipProgress, float swingProgress, HumanoidArm arm) {
		if (this.client.player == null) {
			return;
		}

		boolean bl = arm != HumanoidArm.LEFT;
		float f = bl ? 1.0F : -1.0F;
		float g = Mth.sqrt(swingProgress);
		float h = -0.3F * Mth.sin(g * (float) Math.PI);
		float i = 0.4F * Mth.sin(g * (float) (Math.PI * 2));
		// 减少摆动幅度
		//float j = -0.4F * MathHelper.sin(swingProgress * (float) Math.PI);
		float j = -0.1F * Mth.sin(swingProgress * (float) Math.PI);
		matrices.translate(f * (h + 0.64000005F), i + -0.6F + equipProgress * -0.6F, j + -0.71999997F);
		matrices.mulPose(Axis.YP.rotationDegrees(f * 45.0F));
		float k = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
		float l = Mth.sin(g * (float) Math.PI);
		matrices.mulPose(Axis.YP.rotationDegrees(f * l * 70.0F));
		matrices.mulPose(Axis.ZP.rotationDegrees(f * k * -20.0F));
		AbstractClientPlayer abstractClientPlayerEntity = this.client.player;
		RenderSystem.setShaderTexture(0, abstractClientPlayerEntity.getSkin().texture());
		matrices.translate(f * -1.0F, 3.6F, 3.5F);
		matrices.mulPose(Axis.ZP.rotationDegrees(f * 120.0F));
		matrices.mulPose(Axis.XP.rotationDegrees(200.0F));
		matrices.mulPose(Axis.YP.rotationDegrees(f * -135.0F));
		matrices.translate(f * 5.6F, 0.0F, 0.0F);
		PlayerRenderer playerEntityRenderer = (PlayerRenderer)this.entityRenderDispatcher
			.<AbstractClientPlayer>getRenderer(abstractClientPlayerEntity);
		if (bl) {
			playerEntityRenderer.renderRightHand(matrices, vertexConsumers, light, abstractClientPlayerEntity);
		} else {
			playerEntityRenderer.renderLeftHand(matrices, vertexConsumers, light, abstractClientPlayerEntity);
		}
	}

	private void applyEatOrDrinkTransformation(PoseStack matrices, float tickDelta, HumanoidArm arm, ItemStack stack) {
		if (this.client.player == null || stack == null) {
			return;
		}

		float f = (float)this.client.player.getUseItemRemainingTicks() - tickDelta + 1.0F;
		float g = f / (float)stack.getUseDuration(this.client.player);
		if (g < 0.8F) {
			float h = Mth.abs(Mth.cos(f / 4.0F * (float) Math.PI) * 0.1F);
			matrices.translate(0.0F, h, 0.0F);
		}

		float h = 1.0F - (float)Math.pow((double)g, 27.0);
		int i = arm == HumanoidArm.RIGHT ? 1 : -1;
		matrices.translate(h * 0.6F * (float)i, h * -0.5F, h * 0.0F);
		matrices.mulPose(Axis.YP.rotationDegrees((float)i * h * 90.0F));
		matrices.mulPose(Axis.XP.rotationDegrees(h * 10.0F));
		matrices.mulPose(Axis.ZP.rotationDegrees((float)i * h * 30.0F));
	}

	private void applyBrushTransformation(PoseStack matrices, float tickDelta, HumanoidArm arm, ItemStack stack, float equipProgress) {
		if (this.client.player == null || stack == null) {
			return;
		}

		this.applyEquipOffset(matrices, arm, equipProgress);
		float f = (float)(this.client.player.getUseItemRemainingTicks() % 10);
		float g = f - tickDelta + 1.0F;
		float h = 1.0F - g / 10.0F;
		float i = -90.0F;
		float j = 60.0F;
		float k = 150.0F;
		float l = -15.0F;
		int m = 2;
		float n = -15.0F + 75.0F * Mth.cos(h * 2.0F * (float) Math.PI);
		if (arm != HumanoidArm.RIGHT) {
			matrices.translate(0.1, 0.83, 0.35);
			matrices.mulPose(Axis.XP.rotationDegrees(-80.0F));
			matrices.mulPose(Axis.YP.rotationDegrees(-90.0F));
			matrices.mulPose(Axis.XP.rotationDegrees(n));
			matrices.translate(-0.3, 0.22, 0.35);
		} else {
			matrices.translate(-0.25, 0.22, 0.35);
			matrices.mulPose(Axis.XP.rotationDegrees(-80.0F));
			matrices.mulPose(Axis.YP.rotationDegrees(90.0F));
			matrices.mulPose(Axis.ZP.rotationDegrees(0.0F));
			matrices.mulPose(Axis.XP.rotationDegrees(n));
		}
	}

	private void applySwingOffset(PoseStack matrices, HumanoidArm arm, float swingProgress) {
		int i = arm == HumanoidArm.RIGHT ? 1 : -1;
		float f = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
		matrices.mulPose(Axis.YP.rotationDegrees((float)i * (45.0F + f * -20.0F)));
		float g = Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
		matrices.mulPose(Axis.ZP.rotationDegrees((float)i * g * -20.0F));
		matrices.mulPose(Axis.XP.rotationDegrees(g * -80.0F));
		matrices.mulPose(Axis.YP.rotationDegrees((float)i * -45.0F));
	}

	private void applyEquipOffset(PoseStack matrices, HumanoidArm arm, float equipProgress) {
		int i = arm == HumanoidArm.RIGHT ? 1 : -1;
		matrices.translate((float)i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
	}

	public void renderItem(float tickDelta, PoseStack matrices, MultiBufferSource.BufferSource vertexConsumers, LocalPlayer player, int light) {
		if (player == null || this.client.level == null) {
			return;
		}

		float f = player.getAttackAnim(tickDelta);
		InteractionHand hand = MoreObjects.firstNonNull(player.swingingArm, InteractionHand.MAIN_HAND);
		float g = Mth.lerp(tickDelta, player.xRotO, player.getXRot());
		CustomFeralItemRenderer.HandRenderType handRenderType = getHandRenderType(player);
		float h = Mth.lerp(tickDelta, player.xBobO, player.xBob);
		float i = Mth.lerp(tickDelta, player.yBobO, player.yBob);
		matrices.mulPose(Axis.XP.rotationDegrees((player.getViewXRot(tickDelta) - h) * 0.1F));
		matrices.mulPose(Axis.YP.rotationDegrees((player.getViewYRot(tickDelta) - i) * 0.1F));
		if (handRenderType.renderMainHand) {
			float j = hand == InteractionHand.MAIN_HAND ? f : 0.0F;
			float k = 1.0F - Mth.lerp(tickDelta, this.prevEquipProgressMainHand, this.equipProgressMainHand);
			this.renderFirstPersonItem(player, tickDelta, g, InteractionHand.MAIN_HAND, j, this.mainHand, k, matrices, vertexConsumers, light);
		}

		if (handRenderType.renderOffHand) {
			float j = hand == InteractionHand.OFF_HAND ? f : 0.0F;
			float k = 1.0F - Mth.lerp(tickDelta, this.prevEquipProgressOffHand, this.equipProgressOffHand);
			this.renderFirstPersonItem(player, tickDelta, g, InteractionHand.OFF_HAND, j, this.offHand, k, matrices, vertexConsumers, light);
		}

		vertexConsumers.endBatch();
	}

	@VisibleForTesting
	static CustomFeralItemRenderer.HandRenderType getHandRenderType(LocalPlayer player) {
		ItemStack itemStack = player.getMainHandItem();
		ItemStack itemStack2 = player.getOffhandItem();
		boolean bl = itemStack.is(Items.BOW) || itemStack2.is(Items.BOW);
		boolean bl2 = itemStack.is(Items.CROSSBOW) || itemStack2.is(Items.CROSSBOW);
		if (!bl && !bl2) {
			return CustomFeralItemRenderer.HandRenderType.RENDER_BOTH_HANDS;
		} else if (player.isUsingItem()) {
			return getUsingItemHandRenderType(player);
		} else {
			return isChargedCrossbow(itemStack) ? CustomFeralItemRenderer.HandRenderType.RENDER_MAIN_HAND_ONLY : CustomFeralItemRenderer.HandRenderType.RENDER_BOTH_HANDS;
		}
	}

	private static CustomFeralItemRenderer.HandRenderType getUsingItemHandRenderType(LocalPlayer player) {
		if (player == null) {
			return CustomFeralItemRenderer.HandRenderType.RENDER_BOTH_HANDS;
		}

		ItemStack itemStack = player.getUseItem();
		InteractionHand hand = player.getUsedItemHand();
		if (!itemStack.is(Items.BOW) && !itemStack.is(Items.CROSSBOW)) {
			return hand == InteractionHand.MAIN_HAND && isChargedCrossbow(player.getOffhandItem())
					? CustomFeralItemRenderer.HandRenderType.RENDER_MAIN_HAND_ONLY
					: CustomFeralItemRenderer.HandRenderType.RENDER_BOTH_HANDS;
		} else {
			return CustomFeralItemRenderer.HandRenderType.shouldOnlyRender(hand);
		}
	}

	private static boolean isChargedCrossbow(ItemStack stack) {
		if (stack == null) {
			return false;
		}
		return stack.is(Items.CROSSBOW) && CrossbowItem.isCharged(stack);
	}

	public void renderFirstPersonItem(
		AbstractClientPlayer player,
		float tickDelta,
		float pitch,
		InteractionHand hand,
		float swingProgress,
		ItemStack item,
		float equipProgress,
		PoseStack matrices,
		MultiBufferSource vertexConsumers,
		int light
	) {
		if (player == null || matrices == null || vertexConsumers == null) {
			return;
		}

		if (!player.isScoping()) {
			boolean bl = hand == InteractionHand.MAIN_HAND;
			HumanoidArm arm = bl ? player.getMainArm() : player.getMainArm().getOpposite();
			matrices.pushPose();
			if (item.isEmpty()) {
				// do not render arm

				//if (bl && !player.isInvisible()) {
				//	this.renderArmHoldingItem(matrices, vertexConsumers, light, equipProgress, swingProgress, arm);
				//}
			} else if (item.is(Items.FILLED_MAP)) {
				if (bl && this.offHand.isEmpty()) {
					this.renderMapInBothHands(matrices, vertexConsumers, light, pitch, equipProgress, swingProgress);
				} else {
					this.renderMapInOneHand(matrices, vertexConsumers, light, equipProgress, arm, swingProgress, item);
				}
			} else if (item.is(Items.CROSSBOW)) {
				boolean bl2 = CrossbowItem.isCharged(item);
				boolean bl3 = arm == HumanoidArm.RIGHT;
				int i = bl3 ? 1 : -1;
				if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand) {
					this.applyEquipOffset(matrices, arm, equipProgress);
					matrices.translate((float)i * -0.4785682F, -0.094387F, 0.05731531F);
					matrices.mulPose(Axis.XP.rotationDegrees(-11.935F));
					matrices.mulPose(Axis.YP.rotationDegrees((float)i * 65.3F));
					matrices.mulPose(Axis.ZP.rotationDegrees((float)i * -9.785F));
					float f = 0;
					if (this.client.player != null) {
						f = (float)item.getUseDuration(this.client.player) - ((float)this.client.player.getUseItemRemainingTicks() - tickDelta + 1.0F);
					}
					float g = 0;
					if (this.client.player != null) {
						g = f / (float) CrossbowItem.getChargeDuration(item, this.client.player);
					}
					if (g > 1.0F) {
						g = 1.0F;
					}

					if (g > 0.1F) {
						float h = Mth.sin((f - 0.1F) * 1.3F);
						float j = g - 0.1F;
						float k = h * j;
						matrices.translate(k * 0.0F, k * 0.004F, k * 0.0F);
					}

					matrices.translate(g * 0.0F, g * 0.0F, g * 0.04F);
					matrices.scale(1.0F, 1.0F, 1.0F + g * 0.2F);
					matrices.mulPose(Axis.YN.rotationDegrees((float)i * 45.0F));
				} else {
					float fx = -0.4F * Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
					float gx = 0.2F * Mth.sin(Mth.sqrt(swingProgress) * (float) (Math.PI * 2));
					float h = -0.2F * Mth.sin(swingProgress * (float) Math.PI);
					matrices.translate((float)i * fx, gx, h);
					this.applyEquipOffset(matrices, arm, equipProgress);
					this.applySwingOffset(matrices, arm, swingProgress);
					if (bl2 && swingProgress < 0.001F && bl) {
						matrices.translate((float)i * -0.641864F, 0.0F, 0.0F);
						matrices.mulPose(Axis.YP.rotationDegrees((float)i * 10.0F));
					}
				}

				this.renderItem(
					player,
					item,
					bl3 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
					!bl3,
					matrices,
					vertexConsumers,
					light
				);
			} else {
				boolean bl2 = arm == HumanoidArm.RIGHT;
				if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand) {
					int l = bl2 ? 1 : -1;
					switch (item.getUseAnimation()) {
						case NONE:
							this.applyEquipOffset(matrices, arm, equipProgress);
							break;
						case EAT:
						case DRINK:
							this.applyEatOrDrinkTransformation(matrices, tickDelta, arm, item);
							this.applyEquipOffset(matrices, arm, equipProgress);
							break;
						case BLOCK:
							this.applyEquipOffset(matrices, arm, equipProgress);
							break;
						case BOW:
							this.applyEquipOffset(matrices, arm, equipProgress);
							matrices.translate((float)l * -0.2785682F, 0.18344387F, 0.15731531F);
							matrices.mulPose(Axis.XP.rotationDegrees(-13.935F));
							matrices.mulPose(Axis.YP.rotationDegrees((float)l * 35.3F));
							matrices.mulPose(Axis.ZP.rotationDegrees((float)l * -9.785F));
							float mx = 0;
							if (this.client.player != null) {
								mx = (float)item.getUseDuration(this.client.player) - ((float)this.client.player.getUseItemRemainingTicks() - tickDelta + 1.0F);
							}
							float fxx = mx / 20.0F;
							fxx = (fxx * fxx + fxx * 2.0F) / 3.0F;
							if (fxx > 1.0F) {
								fxx = 1.0F;
							}

							if (fxx > 0.1F) {
								float gx = Mth.sin((mx - 0.1F) * 1.3F);
								float h = fxx - 0.1F;
								float j = gx * h;
								matrices.translate(j * 0.0F, j * 0.004F, j * 0.0F);
							}

							matrices.translate(fxx * 0.0F, fxx * 0.0F, fxx * 0.04F);
							matrices.scale(1.0F, 1.0F, 1.0F + fxx * 0.2F);
							matrices.mulPose(Axis.YN.rotationDegrees((float)l * 45.0F));
							break;
						case SPEAR:
							this.applyEquipOffset(matrices, arm, equipProgress);
							matrices.translate((float)l * -0.5F, 0.7F, 0.1F);
							matrices.mulPose(Axis.XP.rotationDegrees(-55.0F));
							matrices.mulPose(Axis.YP.rotationDegrees((float)l * 35.3F));
							matrices.mulPose(Axis.ZP.rotationDegrees((float)l * -9.785F));
							float m = 0;
							if (this.client.player != null) {
								m = (float)item.getUseDuration(this.client.player) - ((float)this.client.player.getUseItemRemainingTicks() - tickDelta + 1.0F);
							}
							float fx = m / 10.0F;
							if (fx > 1.0F) {
								fx = 1.0F;
							}

							if (fx > 0.1F) {
								float gx = Mth.sin((m - 0.1F) * 1.3F);
								float h = fx - 0.1F;
								float j = gx * h;
								matrices.translate(j * 0.0F, j * 0.004F, j * 0.0F);
							}

							matrices.translate(0.0F, 0.0F, fx * 0.2F);
							matrices.scale(1.0F, 1.0F, 1.0F + fx * 0.2F);
							matrices.mulPose(Axis.YN.rotationDegrees((float)l * 45.0F));
							break;
						case BRUSH:
							this.applyBrushTransformation(matrices, tickDelta, arm, item, equipProgress);
					}
				} else if (player.isAutoSpinAttack()) {
					this.applyEquipOffset(matrices, arm, equipProgress);
					int l = bl2 ? 1 : -1;
					matrices.translate((float)l * -0.4F, 0.8F, 0.3F);
					matrices.mulPose(Axis.YP.rotationDegrees((float)l * 65.0F));
					matrices.mulPose(Axis.ZP.rotationDegrees((float)l * -85.0F));
				} else {
					float n = -0.4F * Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
					float mxx = 0.2F * Mth.sin(Mth.sqrt(swingProgress) * (float) (Math.PI * 2));
					float fxxx = -0.2F * Mth.sin(swingProgress * (float) Math.PI);
					int o = bl2 ? 1 : -1;
					matrices.translate((float)o * n, mxx, fxxx);
					this.applyEquipOffset(matrices, arm, equipProgress);
					this.applySwingOffset(matrices, arm, swingProgress);
				}

				this.renderItem(
					player,
					item,
					bl2 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
					!bl2,
					matrices,
					vertexConsumers,
					light
				);
			}

			matrices.popPose();
		}
	}

	public void updateHeldItems() {
		if (this.client.player == null) {
			return;
		}

		this.prevEquipProgressMainHand = this.equipProgressMainHand;
		this.prevEquipProgressOffHand = this.equipProgressOffHand;
		LocalPlayer clientPlayerEntity = this.client.player;
		ItemStack itemStack = clientPlayerEntity.getMainHandItem();
		ItemStack itemStack2 = clientPlayerEntity.getOffhandItem();
		if (ItemStack.matches(this.mainHand, itemStack)) {
			this.mainHand = itemStack;
		}

		if (ItemStack.matches(this.offHand, itemStack2)) {
			this.offHand = itemStack2;
		}

		if (clientPlayerEntity.isHandsBusy()) {
			this.equipProgressMainHand = Mth.clamp(this.equipProgressMainHand - 0.4F, 0.0F, 1.0F);
			this.equipProgressOffHand = Mth.clamp(this.equipProgressOffHand - 0.4F, 0.0F, 1.0F);
		} else {
			float f = clientPlayerEntity.getAttackStrengthScale(1.0F);
			this.equipProgressMainHand = this.equipProgressMainHand
					+ Mth.clamp((this.mainHand == itemStack ? f * f * f : 0.0F) - this.equipProgressMainHand, -0.4F, 0.4F);
			this.equipProgressOffHand = this.equipProgressOffHand
				+ Mth.clamp((float)(this.offHand == itemStack2 ? 1 : 0) - this.equipProgressOffHand, -0.4F, 0.4F);
		}

		if (this.equipProgressMainHand < 0.1F) {
			this.mainHand = itemStack;
		}

		if (this.equipProgressOffHand < 0.1F) {
			this.offHand = itemStack2;
		}
	}

	public void resetEquipProgress(InteractionHand hand) {
		if (hand == InteractionHand.MAIN_HAND) {
			this.equipProgressMainHand = 0.0F;
		} else {
			this.equipProgressOffHand = 0.0F;
		}
	}

	@Environment(EnvType.CLIENT)
	@VisibleForTesting
	static enum HandRenderType {
		RENDER_BOTH_HANDS(true, true),
		RENDER_MAIN_HAND_ONLY(true, false),
		RENDER_OFF_HAND_ONLY(false, true);

		final boolean renderMainHand;
		final boolean renderOffHand;

		private HandRenderType(boolean renderMainHand, boolean renderOffHand) {
			this.renderMainHand = renderMainHand;
			this.renderOffHand = renderOffHand;
		}

		public static CustomFeralItemRenderer.HandRenderType shouldOnlyRender(InteractionHand hand) {
			return hand == InteractionHand.MAIN_HAND ? RENDER_MAIN_HAND_ONLY : RENDER_OFF_HAND_ONLY;
		}
	}
}