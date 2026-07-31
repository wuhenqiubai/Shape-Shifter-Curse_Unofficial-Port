package net.onixary.shapeShifterCurseFabric.custom_ui.ui_part;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.NonNull;

import java.util.function.BiPredicate;

// Button Widget Other Key 其实也可以直接Mixin的 不过这样写兼容性更强
public class ButtonWidgetOKey extends Button {
    public BiPredicate<ButtonWidgetOKey, Integer> canClick = null;

    public static final BiPredicate<ButtonWidgetOKey, Integer> LEFT_CLICK = (buttonWidgetOKey, integer) -> integer == 0;
    public static final BiPredicate<ButtonWidgetOKey, Integer> RIGHT_CLICK = (buttonWidgetOKey, integer) -> integer == 1;
    public static final BiPredicate<ButtonWidgetOKey, Integer> MIDDLE_CLICK = (buttonWidgetOKey, integer) -> integer == 2;

    public static final CreateNarration DEFAULT_NARRATION_SUPPLIER = (textSupplier) -> (MutableComponent)textSupplier.get();

    public ButtonWidgetOKey(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration narrationSupplier) {
        super(x, y, width, height, message, onPress, narrationSupplier);
    }

    @Override
    protected boolean isValidClickButton(@NonNull MouseButtonInfo mouseButtonInfo) {
        if (canClick != null) {
            return canClick.test(this, mouseButtonInfo.button());
        }
        return super.isValidClickButton(mouseButtonInfo);
    }

    @Override
    protected void renderContents(@NonNull GuiGraphics guiGraphics, int i, int j, float f) {
        this.renderDefaultSprite(guiGraphics);
        this.renderDefaultLabel(guiGraphics.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.NONE));
    }
}