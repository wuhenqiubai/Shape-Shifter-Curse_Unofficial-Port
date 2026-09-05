package net.onixary.shapeShifterCurseFabric.custom_ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

public class AlterCraftUI extends AbstractContainerScreen<AlterCraftUIHandler> {

    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(MOD_ID,"textures/gui/alter_craft_ui.png");
    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;
    private int baseX;
    private int baseY;

    // 90,60,54,5
    // 90,65,54,5

    public AlterCraftUI(AlterCraftUIHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    protected void init() {
        super.init();
    }

    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        baseX = width / 2 - WIDTH / 2;
        baseY = height / 2 - HEIGHT / 2;
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.renderTooltip(context, mouseX, mouseY);
        this.drawBar(context);
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        context.blit(BACKGROUND, baseX, baseY, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
    }

    public void drawBar(GuiGraphics context) {
        AlterCraftUIHandler uiHandler = this.getMenu();
        int maxProgress = uiHandler.getMaxProgress();
        if (maxProgress > 0) {
            int ProcessWidth = (int) (54 * ((float) uiHandler.getNowProgress() / (float) maxProgress));
            context.fill(baseX + 90, baseY + 65, baseX + 90 + ProcessWidth, baseY + 65 + 5, 0xFF00FF00);
        }
        int maxFuel = uiHandler.getMaxFuel();
        if (maxFuel > 0) {
            int FuelWidth = (int) (54 * ((float) uiHandler.getNowFuel() / (float) maxFuel));
            context.fill(baseX + 90, baseY + 60, baseX + 90 + FuelWidth, baseY + 60 + 5, 0xFFFF00FF);
        }
    }
}
