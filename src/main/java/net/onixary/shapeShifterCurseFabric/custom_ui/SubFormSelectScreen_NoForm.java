package net.onixary.shapeShifterCurseFabric.custom_ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

public class SubFormSelectScreen_NoForm extends Screen {
    private static final int MENU_WIDTH = 420;
    private static final int MENU_HEIGHT = 227;
    private static final ResourceLocation MENU_TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/sub_form_menu_2.png");

    private static final int BG_WIDTH = 420;
    private static final int BG_HEIGHT = 227;
    private static final ResourceLocation BG_TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/sub_form_menu_bg.png");

    public SubFormSelectScreen_NoForm(Component title) {
        super(title);
    }
    @Override
    public void init() {
        int baseX = (this.width - MENU_WIDTH) / 2;
        int baseY = (this.height - MENU_HEIGHT) / 2;
        // 152 32 116 14 - Label
        this.addRenderableWidget(new StringWidget(baseX + 152, baseY + 32, 116, 14, Component.literal("Sub Form Select Menu"), this.font));
        // 393 7 20 20 - Close
        this.addRenderableWidget(Button.builder(Component.literal("X"), button -> this.onClose()).pos(baseX + 393, baseY + 7).size(20, 20).build());
        // 132 107 156 36 - No Form Can Select Tip
        this.addRenderableWidget(new StringWidget(baseX + 132, baseY + 107, 156, 36, Component.literal("No Form Can Select"), this.font));
        super.init();
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    public void renderBackgroundTexture(GuiGraphics context) {
        // 计算居中位置，保持固定尺寸
        context.blit(BG_TEXTURE, 0, 0, this.width, this.height, 0, 0, BG_WIDTH, BG_HEIGHT, BG_WIDTH, BG_HEIGHT);
        int bgX = (this.width - MENU_WIDTH) / 2;
        int bgY = (this.height - MENU_HEIGHT) / 2;
        context.blit(MENU_TEXTURE, bgX, bgY, 0, 0, MENU_WIDTH, MENU_HEIGHT, MENU_WIDTH, MENU_HEIGHT);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(context);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return false;
    }
}
