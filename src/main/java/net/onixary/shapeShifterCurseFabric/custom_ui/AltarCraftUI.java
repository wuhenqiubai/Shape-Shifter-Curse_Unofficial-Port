package net.onixary.shapeShifterCurseFabric.custom_ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.onixary.shapeShifterCurseFabric.blocks.block_entity.AltarBlockEntity;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

public class AltarCraftUI extends AbstractContainerScreen<AltarCraftUIHandler> {

    // 合并 1.21.1：取 altar 改名后的贴图路径，保留 1.21.11 的 Identifier
    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(MOD_ID,"textures/gui/altar_craft_ui.png");
    private static final int WIDTH = 174;
    private static final int HEIGHT = 166;
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int PROCESS_BAR_ORIG_X = 84;
    private static final int PROCESS_BAR_ORIG_Y = 39;
    private static final int PROCESS_BAR_FULL_X = 174;
    private static final int PROCESS_BAR_FULL_Y = 0;
    private static final int PROCESS_BAR_W = 43;
    private static final int PROCESS_BAR_H = 9;
    private static final int FUEL_BAR_ORIG_X = 84;
    private static final int FUEL_BAR_ORIG_Y = 49;
    private static final int FUEL_BAR_FULL_X = 174;
    private static final int FUEL_BAR_FULL_Y = 9;
    private static final int FUEL_BAR_W = 43;
    private static final int FUEL_BAR_H = 3;
    private int baseX;
    private int baseY;

    public AltarCraftUI(AltarCraftUIHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        // 藏的还挺深 要不是我修槽位偏移我都不知道这个
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
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
        this.drawProcess(context);
        this.drawFuel(context);
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        context.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, baseX, baseY, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT, -1);
    }

    public void drawProcess(GuiGraphics context) {
        AltarCraftUIHandler uiHandler = this.getMenu();
        int maxProgress = uiHandler.getMaxProgress();
        if (maxProgress > 0) {
            // clamp 到 [0,PROCESS_BAR_W]：防止 ratio>1 时 ProcessWidth 越界，blit 采样越过纹理右缘 wrap（视觉"反转到左侧"）
            int ProcessWidth = (int) (PROCESS_BAR_W * ((float) uiHandler.getNowProgress() / (float) maxProgress));
            ProcessWidth = Math.clamp(ProcessWidth, 0, PROCESS_BAR_W);
            context.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, baseX + PROCESS_BAR_ORIG_X, baseY + PROCESS_BAR_ORIG_Y, PROCESS_BAR_FULL_X, PROCESS_BAR_FULL_Y, ProcessWidth, PROCESS_BAR_H, ProcessWidth, PROCESS_BAR_H, TEXTURE_WIDTH, TEXTURE_HEIGHT, -1);
        }
    }

    public void drawFuel(GuiGraphics context) {
        AltarCraftUIHandler uiHandler = this.getMenu();
        int maxFuel = AltarBlockEntity.maxFuel;
        if (maxFuel > 0) {
            // clamp 到 [0,FUEL_BAR_W]：防止 FuelWidth 越界(负值/超值)导致左端脱离 FUEL_BAR_ORIG_X
            int FuelWidth = (int) (FUEL_BAR_W * ((float) uiHandler.getNowFuel() / (float) maxFuel));
            FuelWidth = Math.clamp(FuelWidth, 0, FUEL_BAR_W);
            context.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, baseX + FUEL_BAR_ORIG_X, baseY + FUEL_BAR_ORIG_Y, FUEL_BAR_FULL_X, FUEL_BAR_FULL_Y, FuelWidth, FUEL_BAR_H, FuelWidth, FUEL_BAR_H, TEXTURE_WIDTH, TEXTURE_HEIGHT, -1);
        }
    }
}
