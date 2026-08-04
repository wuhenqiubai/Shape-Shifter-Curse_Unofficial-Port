package net.onixary.shapeShifterCurseFabric.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.additional_power.AdditionalPowers;
import net.onixary.shapeShifterCurseFabric.additional_power.CustomEdiblePower;
import net.onixary.shapeShifterCurseFabric.additional_power.LevitatePower;
import net.onixary.shapeShifterCurseFabric.blocks.RegCustomBlock;
import net.onixary.shapeShifterCurseFabric.cursed_moon.CursedMoonSkyTextures;
import net.onixary.shapeShifterCurseFabric.custom_ui.BookOfShapeShifterScreenV2_P1;
import net.onixary.shapeShifterCurseFabric.custom_ui.StartBookScreenV2;
import net.onixary.shapeShifterCurseFabric.data.StaticParams;
import net.onixary.shapeShifterCurseFabric.entity.RegCustomEntityRenderer;
import net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.axolotl.TAxolotlEntityRenderer;
import net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.bat.BatEntityRenderer;
import net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.ocelot.TOcelotEntityRenderer;
import net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.spider.TSpiderEntityRenderer;
import net.onixary.shapeShifterCurseFabric.mana.ManaRegistriesClient;
import net.onixary.shapeShifterCurseFabric.mana.ManaUtils;
import net.onixary.shapeShifterCurseFabric.minion.MinionRegisterClient;
import net.onixary.shapeShifterCurseFabric.minion.mobs.AnubisWolfMinionEntityRenderer;
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsC2S;
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsS2C;
import net.onixary.shapeShifterCurseFabric.player_form.utils.TransformManager;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormRenderUtils;
import net.onixary.shapeShifterCurseFabric.util.ClientTicker;
import net.onixary.shapeShifterCurseFabric.util.FormColorData;
import net.onixary.shapeShifterCurseFabric.util.PatronUtils;
import net.onixary.shapeShifterCurseFabric.util.TickManager;
import net.onixary.shapeShifterCurseFabric.util.Verify.AuthClient;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.UUID;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.*;

public class ShapeShifterCurseFabricClient implements ClientModInitializer {
	// 由于咒文生物使用的都是原版模型，所以无需注册Layer
	//public static final EntityModelLayer T_BAT_LAYER = new EntityModelLayer(Identifier.of(MOD_ID, "t_bat"), "main");
	//public static final EntityModelLayer T_AXOLOTL_LAYER = new EntityModelLayer(Identifier.of(MOD_ID, "t_axolotl"), "main");
	//public static final EntityModelLayer T_OCELOT_LAYER = new EntityModelLayer(Identifier.of(MOD_ID, "t_ocelot"), "main");

	public static final FormColorData formColorData = new FormColorData();

	public static Minecraft getClient() {
		return Minecraft.getInstance();
	}
	private static final KeyMapping.Category SSC_CATEGORY = KeyMapping.Category.register(
			Identifier.fromNamespaceAndPath(MOD_ID, "keys"));
	// TODO: 1.21.11 Satin已移除，需要新方案注册着色器
	// private static ShaderInstance furGradientShader;

	public static KeyMapping makeSound;
	public static KeyMapping useActiveSkill1PowerKeybind;
	public static KeyMapping useActiveSkill2PowerKeybind;
	public static KeyMapping useActiveSkill3PowerKeybind;
	public static KeyMapping useActiveSkill4PowerKeybind;
	public static KeyMapping useActiveSkill5PowerKeybind;
	public static KeyMapping useActiveSkill6PowerKeybind;

	public static boolean isBlockingClipAtLedge = false;

	public static void openBookScreen(Player user) {
		if (!(Minecraft.getInstance().screen instanceof BookOfShapeShifterScreenV2_P1)) {
			BookOfShapeShifterScreenV2_P1 bookScreen = new BookOfShapeShifterScreenV2_P1();
			bookScreen.currentPlayer = user;
			Minecraft.getInstance().setScreen(bookScreen);
		}
	}
	public static void openStartBookScreen(Player user) {
		if (!(Minecraft.getInstance().screen instanceof StartBookScreenV2)) {
			StartBookScreenV2 startScreen = new StartBookScreenV2();
			startScreen.currentPlayer = user;
			Minecraft.getInstance().setScreen(startScreen);
		}
	}

	public static void registerEntityModels() {
		EntityRendererRegistry.register(T_BAT, BatEntityRenderer::new);
		EntityRendererRegistry.register(T_AXOLOTL, TAxolotlEntityRenderer::new);
		EntityRendererRegistry.register(T_OCELOT, TOcelotEntityRenderer::new);
		EntityRendererRegistry.register(T_WOLF, AnubisWolfMinionEntityRenderer::new);
		EntityRendererRegistry.register(T_SPIDER, TSpiderEntityRenderer::new);

		MinionRegisterClient.registerClient();
	}


	private static void onClientTick(Minecraft minecraftClient){
		// 预加载咒月月亮纹理：onInitializeClient 时 getTextureManager() 仍为 null（初始化太早），
		// 改在首帧 tick 上传（textureManager 已就绪，且 tick 阶段不在渲染 pass 内）
		CursedMoonSkyTextures.preload();
		TickManager.tickClientAll();
		LocalPlayer clientPlayer = minecraftClient.player;
		if(clientPlayer == null){
			return;
		}
		// Mana System
		if (!Minecraft.getInstance().isPaused()) {
			ManaUtils.manaTick(minecraftClient.player);
			// Transform overlay tick (black screen + nausea)
			TransformManager.clientTick();
		}
		// ItemStorePower restored — clientTick needs PowerHolderComponent.getPowers usage
	}

	public static void emitTransformParticle(int duration) {
		LocalPlayer clientPlayer = Minecraft.getInstance().player;
		if(clientPlayer == null){
			return;
		}


		// similar to DOTween in Unity
		Runnable particleTask = () -> {
			for (int i = 0; i < 2; i++) {
				clientPlayer.level().addParticle(StaticParams.PLAYER_TRANSFORM_PARTICLE,
					clientPlayer.getX() + (clientPlayer.getRandom().nextDouble() - 0.5) * 0.9,
					clientPlayer.getY() + clientPlayer.getRandom().nextDouble() * 1.5 + 1,
					clientPlayer.getZ() + (clientPlayer.getRandom().nextDouble() - 0.5) * 0.9,
					0, -1, 0);
				//ShapeShifterCurseFabric.LOGGER.info("Particle effect emitted");
			}
		};

		// Get the Minecraft client instance
		Minecraft client = Minecraft.getInstance();
		// Create and start the client ticker
		ClientTicker ticker = new ClientTicker(client, particleTask, duration);
		ticker.start();
	}

	public static void applyInstinctThresholdEffect() {
		LocalPlayer clientPlayer = Minecraft.getInstance().player;
		if(clientPlayer == null){
			return;
		}

		for (int i = 0; i < 1; i++) {
			clientPlayer.level().addParticle(StaticParams.PLAYER_TRANSFORM_PARTICLE,
				clientPlayer.getX() + (clientPlayer.getRandom().nextDouble() - 0.5) * 0.5,
				clientPlayer.getY() + clientPlayer.getRandom().nextDouble() * 1,
				clientPlayer.getZ() + (clientPlayer.getRandom().nextDouble() - 0.5) * 0.5,
				0, 1, 0.5);
		}
	}

	// 原先的仅考虑到当前玩家变身动作 其他玩家变身动作不会更新
	public static class TransformingStage {
		public boolean IsTransforming = false;
        public String TransformFromForm = null;
        public String TransformToForm = null;
	}

	public static HashMap<UUID, TransformingStage> clientTransformState = new HashMap<>();

	public static TransformingStage getClientTransformState(UUID playerUuid) {
		if (!clientTransformState.containsKey(playerUuid)) {
			clientTransformState.put(playerUuid, new TransformingStage());
		}
		return clientTransformState.get(playerUuid);
	}

	public static void updateTransformState(UUID playerUuid, boolean isTransforming, String fromForm, String toForm) {
		if (!clientTransformState.containsKey(playerUuid)) {
			clientTransformState.put(playerUuid, new TransformingStage());
		}
		TransformingStage stage = clientTransformState.get(playerUuid);
		stage.IsTransforming = isTransforming;
		stage.TransformFromForm = fromForm;
		stage.TransformToForm = toForm;
		ShapeShifterCurseFabric.LOGGER.info("Updated client transform state: {}, from: {}, to: {}", isTransforming, fromForm, toForm);
	}

	// 获取客户端变身状态的方法（供动画系统使用）
	public static boolean isClientTransforming(UUID playerUuid) {
		return getClientTransformState(playerUuid).IsTransforming;
	}

	public static String getClientTransformFromForm(UUID playerUuid) {
		return getClientTransformState(playerUuid).TransformFromForm;
	}

	public static String getClientTransformToForm(UUID playerUuid) {
		return getClientTransformState(playerUuid).TransformToForm;
	}

	/* TODO: 1.21.11 已移除Satin
	private void registerShaderResource()
	{
		CoreShaderRegistrationCallback.EVENT.register(context -> {
			// 1. 定义着色器的 Identifier
			ResourceLocation shaderId = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "fur_gradient_remap");

			// 2. 使用 context.register 方法注册
			//    这个方法会处理底层的加载逻辑
			context.register(shaderId, DefaultVertexFormat.NEW_ENTITY, program -> {
				// 3. 将加载好的 ShaderProgram 实例保存到我们的静态变量中
				ShapeShifterCurseFabricClient.furGradientShader = program;
			});
		});
	}
	 */

	@Override
	public void onInitializeClient() {
		registerEntityModels();
		ModPacketsS2C.register();
        ModPacketsC2S.registerClient();

		// TODO: 1.21.11 Satin已移除，需新方案注册着色器
		// registerShaderResource();
		// FurGradientRenderLayer.onInitializeClient();

		ManaRegistriesClient.register();
		RegCustomEntityRenderer.init();

		FormRenderUtils.onClientInit();

		formColorData.loadFormConfig();

		ClientTickEvents.END_CLIENT_TICK.register(ShapeShifterCurseFabricClient::onClientTick);
		// 客户端能力处理
		ClientTickEvents.START_CLIENT_TICK.register((minecraftClient) -> {
			LocalPlayer clientPlayer = minecraftClient.player;
			if(clientPlayer == null){
				return;
			}
			// 由于LevitatePower覆写了isActive没法通过getPowers获取到
			// List<LevitatePower> clientLevitatePower = PowerHolderComponent.getPowers(clientPlayer, LevitatePower.class);
			// if (!clientLevitatePower.isEmpty()) {
			// 	// getFirst是Java21的新特性 Java17没有
			// 	LevitatePower power = clientLevitatePower.get(0);
			// 	power.clientTick(clientPlayer);
			// }
			PowerHolderComponent.KEY.get(clientPlayer).getPowers().stream().filter(p -> p instanceof LevitatePower).forEach(p -> ((LevitatePower) p).clientTick(clientPlayer));
			CustomEdiblePower.OnClientTick(clientPlayer);
		});

		makeSound = new KeyMapping("key.shape-shifter-curse.make_sound", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT, SSC_CATEGORY);
		ApoliClient.registerPowerKeybinding("make_sound", makeSound);
		KeyBindingHelper.registerKeyBinding(makeSound);

		// 4个技能按键基本够用了 一般Mod的常用技能一般也是4个 后续如果还要加按键 可以做成轮盘(应该可以虚拟触发按键 反正只用触发Apoli的就行)等压缩按键形式 然后这些按键可以当做快捷触发键使用
		// 绑定顺序
		// 主动常用技能 1->2->3->4
		// 类似大招技能 3->4->2->1
		// 打开菜单 5->6
		// 切换能力 6->5
		// 这2个分配给常用技能 一般推荐绑鼠标侧键上
		useActiveSkill1PowerKeybind = new KeyMapping("key.shape-shifter-curse.active_skill_1", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, SSC_CATEGORY);
		useActiveSkill2PowerKeybind = new KeyMapping("key.shape-shifter-curse.active_skill_2", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, SSC_CATEGORY);
		// 这2个介于常用和非常用之间 尽量别放类似火球这种能力 如果鼠标侧键多也可以绑侧键上
		useActiveSkill3PowerKeybind = new KeyMapping("key.shape-shifter-curse.active_skill_3", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, SSC_CATEGORY);
		useActiveSkill4PowerKeybind = new KeyMapping("key.shape-shifter-curse.active_skill_4", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, SSC_CATEGORY);
		// 这2个给打开UI/可切换功能使用 不推荐给主动能力用 当然 你要是加个自爆技能也能绑这2个按键 推荐绑键盘不太常按的按键上
		useActiveSkill5PowerKeybind = new KeyMapping("key.shape-shifter-curse.active_skill_5", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, SSC_CATEGORY);
		useActiveSkill6PowerKeybind = new KeyMapping("key.shape-shifter-curse.active_skill_6", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, SSC_CATEGORY);
		ApoliClient.registerPowerKeybinding("key.shape-shifter-curse.active_skill_1", useActiveSkill1PowerKeybind);
		ApoliClient.registerPowerKeybinding("key.shape-shifter-curse.active_skill_2", useActiveSkill2PowerKeybind);
		ApoliClient.registerPowerKeybinding("key.shape-shifter-curse.active_skill_3", useActiveSkill3PowerKeybind);
		ApoliClient.registerPowerKeybinding("key.shape-shifter-curse.active_skill_4", useActiveSkill4PowerKeybind);
		ApoliClient.registerPowerKeybinding("key.shape-shifter-curse.active_skill_5", useActiveSkill5PowerKeybind);
		ApoliClient.registerPowerKeybinding("key.shape-shifter-curse.active_skill_6", useActiveSkill6PowerKeybind);
		KeyBindingHelper.registerKeyBinding(useActiveSkill1PowerKeybind);
		KeyBindingHelper.registerKeyBinding(useActiveSkill2PowerKeybind);
		KeyBindingHelper.registerKeyBinding(useActiveSkill3PowerKeybind);
		KeyBindingHelper.registerKeyBinding(useActiveSkill4PowerKeybind);
		KeyBindingHelper.registerKeyBinding(useActiveSkill5PowerKeybind);
		KeyBindingHelper.registerKeyBinding(useActiveSkill6PowerKeybind);

		ClientTickEvents.END_CLIENT_TICK.register((client) -> {
			if (client.player == null) {
				return;
			}
			if (AdditionalPowers.TOGGLE_CLIP_AT_LEDGE.isActive(client.player)) {
				if (!isBlockingClipAtLedge) {
					isBlockingClipAtLedge = true;
					client.player.displayClientMessage(Component.translatable("message.shape-shifter-curse.clip_at_ledge.off"), true);
				}
			} else {
				if (isBlockingClipAtLedge) {
					isBlockingClipAtLedge = false;
					client.player.displayClientMessage(Component.translatable("message.shape-shifter-curse.clip_at_ledge.on"), true);
				}
			}
		});

		RegCustomBlock.ClientInit();
		PatronUtils.OnClientInit();
		AuthClient.init();
	}

	// TODO: 1.21.11 Satin已移除，需新方案注册着色器
	// public static ShaderInstance getFurGradientShader() {
	// 	return furGradientShader;
	// }

}