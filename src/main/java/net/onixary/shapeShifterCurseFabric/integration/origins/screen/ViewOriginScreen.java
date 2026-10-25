package net.onixary.shapeShifterCurseFabric.integration.origins.screen;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;
import net.onixary.shapeShifterCurseFabric.integration.origins.OriginsClient;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.Origin;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.OriginLayer;
import net.onixary.shapeShifterCurseFabric.integration.origins.registry.ModComponents;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class ViewOriginScreen extends OriginDisplayScreen {

	private final ArrayList<Tuple<OriginLayer, Origin>> originLayers;
	private int currentLayer = 0;
	private Button chooseOriginButton;

	public ViewOriginScreen() {
		super(Component.translatable(Origins.MODID + ".screen.view_origin"), false);
		Player player = Minecraft.getInstance().player;
		HashMap<OriginLayer, Origin> origins = null;
		if (player != null) {
			origins = ModComponents.ORIGIN.get(player).getOrigins();
		}
		originLayers = new ArrayList<>(origins.size());

		origins.forEach((layer, origin) -> {
			ItemStack displayItem = origin.getDisplayItem();
			if(displayItem.getItem() == Items.PLAYER_HEAD) {
				ResolvableProfile profile = displayItem.get(DataComponents.PROFILE);
				if (profile == null || profile.name().isEmpty()) {
					if (player != null) {
						displayItem.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));
					}
				}
			}
			if((origin != Origin.EMPTY || layer.getOriginOptionCount(player) > 0) && !layer.isHidden()) {
				originLayers.add(new Tuple<>(layer, origin));
			}
		});
		originLayers.sort(Comparator.comparing(Tuple::getA));
		if (!this.originLayers.isEmpty()) {
			Tuple<OriginLayer, Origin> current = originLayers.get(currentLayer);
			showOrigin(current.getB(), current.getA(), false);
		} else {
			showOrigin(null, null, false);
		}
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}

	@Override
	protected void init() {
		super.init();
		if (!originLayers.isEmpty() && OriginsClient.isServerRunningOrigins) {
			addRenderableWidget(chooseOriginButton = Button.builder(Component.translatable(Origins.MODID + ".gui.choose"), b -> Minecraft.getInstance().setScreen(new ChooseOriginScreen(Lists.newArrayList(originLayers.get(currentLayer).getA()), 0, false))).bounds(guiLeft + windowWidth / 2 - 50, guiTop + windowHeight - 40, 100, 20).build());

			Player player = Minecraft.getInstance().player;
			chooseOriginButton.active = chooseOriginButton.visible = originLayers.get(currentLayer).getB() == Origin.EMPTY && originLayers.get(currentLayer).getA().getOriginOptionCount(player) > 0;
			if(originLayers.size() > 1) {
				addRenderableWidget(Button.builder(Component.nullToEmpty("<"), b -> {
					currentLayer = (currentLayer - 1 + originLayers.size()) % originLayers.size();
					Tuple<OriginLayer, Origin> current = originLayers.get(currentLayer);
					showOrigin(current.getB(), current.getA(), false);
					chooseOriginButton.active = chooseOriginButton.visible = current.getB() == Origin.EMPTY && current.getA().getOriginOptionCount(player) > 0;
				}).bounds(guiLeft - 40,this.height / 2 - 10, 20, 20).build());
				addRenderableWidget(Button.builder(Component.nullToEmpty(">"), b -> {
					currentLayer = (currentLayer + 1) % originLayers.size();
					Tuple<OriginLayer, Origin> current = originLayers.get(currentLayer);
					showOrigin(current.getB(), current.getA(), false);
					chooseOriginButton.active = chooseOriginButton.visible = current.getB() == Origin.EMPTY && current.getA().getOriginOptionCount(player) > 0;
				}).bounds(guiLeft + windowWidth + 20, this.height / 2 - 10, 20, 20).build());
			}
		}
		addRenderableWidget(Button.builder(Component.translatable(Origins.MODID + ".gui.close"), b -> Minecraft.getInstance().setScreen(null)).bounds(guiLeft + windowWidth / 2 - 50, guiTop + windowHeight + 5, 100, 20).build());
	}
	
	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		if (originLayers.isEmpty()) {
			if(OriginsClient.isServerRunningOrigins) {
				context.drawCenteredString(this.font, Component.translatable(Origins.MODID + ".gui.view_origin.empty").getString(), width / 2, guiTop + 48, 0xFFFFFF);
			} else {
				context.drawCenteredString(this.font, Component.translatable(Origins.MODID + ".gui.view_origin.not_installed").getString(), width / 2, guiTop + 48, 0xFFFFFF);
			}
		}
	}

	@Override
	protected Component getTitleText() {
		if (getCurrentLayer().shouldOverrideViewOriginTitle()) {
			return Component.translatable(getCurrentLayer().getTitleViewOriginTranslationKey());
		}
		return Component.translatable(Origins.MODID + ".gui.view_origin.title", Component.translatable(getCurrentLayer().getTranslationKey()));
	}

}
