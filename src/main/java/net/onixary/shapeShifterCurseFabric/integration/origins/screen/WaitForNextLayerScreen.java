package net.onixary.shapeShifterCurseFabric.integration.origins.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.integration.origins.component.OriginComponent;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.OriginLayer;
import net.onixary.shapeShifterCurseFabric.integration.origins.registry.ModComponents;
import java.util.ArrayList;

public class WaitForNextLayerScreen extends Screen {

    private final ArrayList<OriginLayer> layerList;
    private final int currentLayerIndex;
    private final boolean showDirtBackground;
    private final int maxSelection;

    protected WaitForNextLayerScreen(ArrayList<OriginLayer> layerList, int currentLayerIndex, boolean showDirtBackground) {
        super(Component.empty());
        this.layerList = layerList;
        this.currentLayerIndex = currentLayerIndex;
        this.showDirtBackground = showDirtBackground;
        Player player = Minecraft.getInstance().player;
        OriginLayer currentLayer = layerList.get(currentLayerIndex);
        maxSelection = currentLayer.getOriginOptionCount(player);
    }

    public void openSelection() {
        int index = currentLayerIndex + 1;
        Player player = Minecraft.getInstance().player;
	    OriginComponent component = null;
	    if (player != null) {
		    component = ModComponents.ORIGIN.get(player);
	    }
	    while (index < layerList.size()) {
		    if (component != null && !component.hasOrigin(layerList.get(index)) && !layerList.get(index).getOrigins(player).isEmpty()) {
			    Minecraft.getInstance().setScreen(new ChooseOriginScreen(layerList, index, showDirtBackground));
			    return;
		    }
		    index++;
        }
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if(maxSelection == 0) {
            openSelection();
            return;
        }
        super.render(context, mouseX, mouseY, delta);
    }

}
