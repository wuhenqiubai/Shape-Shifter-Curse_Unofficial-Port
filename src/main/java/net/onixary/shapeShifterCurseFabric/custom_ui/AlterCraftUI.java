package net.onixary.shapeShifterCurseFabric.custom_ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

public class AlterCraftUI extends AbstractContainerScreen<AlterCraftUIHandler> {

    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(MOD_ID,"textures/gui/alter_craft_ui.png");
    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;
    private static final int TEXTURE_WIDTH = 200;
    private static final int TEXTURE_HEIGHT = 166;
    private int baseX;
    private int baseY;

    // 90,60,54,10

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
        context.blit(BACKGROUND, baseX, baseY, 0, 0, WIDTH, HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public void drawBar(GuiGraphics context) {
        AlterCraftUIHandler uiHandler = this.getMenu();
        int maxProgress = uiHandler.getMaxProgress();
        if (maxProgress > 0) {
            int ProcessWidth = (int) (24 * ((float) uiHandler.getNowProgress() / (float) maxProgress));
            context.blit(BACKGROUND, baseX+89, baseY+35, 176, 0, ProcessWidth, 17, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
        int maxFuel = AlterBlockEntity.maxFuel;
        if (maxFuel > 0) {
            int FuelWidth = (int) (54 * ((float) uiHandler.getNowFuel() / (float) maxFuel));
            context.fill(baseX + 90, baseY + 60, baseX + 90 + FuelWidth, baseY + 60 + 10, 0xFFFF00FF);
        }
    }
}
