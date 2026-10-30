package net.onixary.shapeShifterCurseFabric.custom_ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

public class SubFormSelectScreen_NoForm extends Screen {
    private static final int MENU_WIDTH = 420;
    private static final int MENU_HEIGHT = 227;
    private static final Identifier MENU_TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/sub_form_menu_2.png");

    private static final int BG_WIDTH = 420;
    private static final int BG_HEIGHT = 227;
    private static final Identifier BG_TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/sub_form_menu_bg.png");

    public SubFormSelectScreen_NoForm(Component title) {
        super(title);
    }
    @Override
    public void init() {
        int baseX = (this.width - MENU_WIDTH) / 2;
        int baseY = (this.height - MENU_HEIGHT) / 2;
        // 152 32 116 14 - Label
        this.addRenderableWidget(new StringWidget(baseX + 152, baseY + 32, 116, 14, Component.translatable("gui.shape-shifter-curse.sub_form.title"), this.font));
        // 393 7 20 20 - Close
        this.addRenderableWidget(Button.builder(Component.literal("X"), button -> this.onClose()).pos(baseX + 393, baseY + 7).size(20, 20).build());
        // 132 107 156 36 - No Form Can Select Tip
        this.addRenderableWidget(new StringWidget(baseX + 132, baseY + 107, 156, 36, Component.translatable("gui.shape-shifter-curse.sub_form.no_forms"), this.font));
        super.init();
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractBackground(context, mouseX, mouseY, delta);
        this.renderBackgroundTexture(context);
    }

    public void renderBackgroundTexture(GuiGraphicsExtractor context) {
        // 计算居中位置，保持固定尺寸
        context.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, BG_TEXTURE, 0, 0, 0, 0, this.width, this.height, BG_WIDTH, BG_HEIGHT, BG_WIDTH, BG_HEIGHT, -1);
        int bgX = (this.width - MENU_WIDTH) / 2;
        int bgY = (this.height - MENU_HEIGHT) / 2;
        context.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, MENU_TEXTURE, bgX, bgY, 0, 0, MENU_WIDTH, MENU_HEIGHT, MENU_WIDTH, MENU_HEIGHT, MENU_WIDTH, MENU_HEIGHT, -1);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        if (super.keyPressed(event)) {
            return true;
        } else if (this.minecraft.options.keyInventory.matches(event)) {
            this.onClose();
            return true;
        }
        return false;
    }
}
