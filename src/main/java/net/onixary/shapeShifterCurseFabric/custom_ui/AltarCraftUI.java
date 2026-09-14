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

    // 合并 1.21.1：取 altar 改名后的贴图路径，保留 1.21.11 的 Identifier
    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(MOD_ID,"textures/gui/altar_craft_ui.png");
    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;
    private static final int TEXTURE_WIDTH = 200;
    private static final int TEXTURE_HEIGHT = 166;
    private int baseX;
    private int baseY;

    // 90,60,54,10

    public AltarCraftUI(AltarCraftUIHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    protected void init() {
        super.init();
        // 26.1: 原先在 render() 里每帧算，现在挪到 init()（resize 时框架会重调 init）
        baseX = width / 2 - WIDTH / 2;
        baseY = height / 2 - HEIGHT / 2;
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
            // clamp 到 [0,24]：防止 ratio>1 时 ProcessWidth>24，blit 采样 u1=(176+w)/200>1.0 越过纹理右缘 wrap（视觉"反转到左侧"）
            int ProcessWidth = (int) (24 * ((float) uiHandler.getNowProgress() / (float) maxProgress));
            ProcessWidth = Math.clamp(ProcessWidth, 0, 24);
            context.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, baseX+89, baseY+35, 176, 0, ProcessWidth, 17, ProcessWidth, 17, TEXTURE_WIDTH, TEXTURE_HEIGHT, -1);
        }
        int maxFuel = AltarBlockEntity.maxFuel;
        if (maxFuel > 0) {
            // clamp 到 [0,54]：防止 FuelWidth 越界(负值/超值)导致 fill 左端脱离 baseX+90(视觉"反转到增长起始点左侧")
            int FuelWidth = (int) (54 * ((float) uiHandler.getNowFuel() / (float) maxFuel));
            FuelWidth = Math.clamp(FuelWidth, 0, 54);
            context.fill(baseX + 90, baseY + 60, baseX + 90 + FuelWidth, baseY + 60 + 10, 0xFFFF00FF);
        }
    }
}
