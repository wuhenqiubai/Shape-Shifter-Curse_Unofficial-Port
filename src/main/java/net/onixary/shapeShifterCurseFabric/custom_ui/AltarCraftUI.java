package net.onixary.shapeShifterCurseFabric.custom_ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.onixary.shapeShifterCurseFabric.blocks.block_entity.AltarBlockEntity;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

public class AltarCraftUI extends AbstractContainerScreen<AltarCraftUIHandler> {

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
        // 26.1: 原先在 render() 里每帧算，现在挪到 init()（resize 时框架会重调 init）
        baseX = width / 2 - WIDTH / 2;
        baseY = height / 2 - HEIGHT / 2;
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.renderTooltip(context, mouseX, mouseY);
        this.drawProcess(context);
        this.drawFuel(context);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        // 26.1: Screen#render 已改名为 extractRenderState（原名不再存在，继续写 render 不构成覆写 = 静默死代码）。
        // 背景由框架经 extractBackground 自动调用、tooltip 由 AbstractContainerScreen#extractRenderState
        // 自动调用，故原先手动的 renderBackground/renderTooltip 两行一并删除，否则会重复提交。
        super.extractRenderState(context, mouseX, mouseY, delta);
        this.drawBar(context);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float a) {
        // 26.1: AbstractContainerScreen#renderBg 被删除，改由覆写 Screen#extractBackground 实现。
        // ⚠ 两处变化：① 可见性必须是 public（Screen 里是 public，旧的 renderBg 是 protected，不能收窄）
        //            ② 参数顺序变了 —— delta 从第 2 位挪到第 4 位（旧 renderBg(context, delta, mouseX, mouseY)）
        super.extractBackground(context, mouseX, mouseY, a);
        context.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, baseX, baseY, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT, -1);
    }

    public void drawBar(GuiGraphicsExtractor context) {
        AltarCraftUIHandler uiHandler = this.getMenu();
        int maxProgress = uiHandler.getMaxProgress();
        if (maxProgress > 0) {
            int ProcessWidth = (int) (PROCESS_BAR_W * ((float) uiHandler.getNowProgress() / (float) maxProgress));
            // ⚠ 必须用带 RenderPipeline 的 13 参重载。1.21.11 里另有一个 9 参
            // blit(tex, x1,y1,x2,y2, u1,u2,v1,v2) —— 收的是「像素坐标 + 归一化 UV」，
            // 与 1.21.1 的 9 参同名不同义；照搬旧写法会把宽度/纹理尺寸当成 UV 传进去（表现为密集采样条纹）。
            context.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, baseX + PROCESS_BAR_ORIG_X, baseY + PROCESS_BAR_ORIG_Y, PROCESS_BAR_FULL_X, PROCESS_BAR_FULL_Y, ProcessWidth, PROCESS_BAR_H, ProcessWidth, PROCESS_BAR_H, TEXTURE_WIDTH, TEXTURE_HEIGHT, -1);
        }
    }

    public void drawFuel(GuiGraphics context) {
        AltarCraftUIHandler uiHandler = this.getMenu();
        int maxFuel = AltarBlockEntity.maxFuel;
        if (maxFuel > 0) {
            int FuelWidth = (int) (FUEL_BAR_W * ((float) uiHandler.getNowFuel() / (float) maxFuel));
            context.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, baseX + FUEL_BAR_ORIG_X, baseY + FUEL_BAR_ORIG_Y, FUEL_BAR_FULL_X, FUEL_BAR_FULL_Y, FuelWidth, FUEL_BAR_H, FuelWidth, FUEL_BAR_H, TEXTURE_WIDTH, TEXTURE_HEIGHT, -1);
        }
    }
}
