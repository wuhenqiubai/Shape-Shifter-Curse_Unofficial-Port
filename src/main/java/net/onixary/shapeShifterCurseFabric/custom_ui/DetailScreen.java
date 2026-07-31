package net.onixary.shapeShifterCurseFabric.custom_ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.onixary.shapeShifterCurseFabric.custom_ui.ui_part.ScaleScrollTextWidget;
import net.onixary.shapeShifterCurseFabric.custom_ui.ui_part.WidgetEXUtils;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class DetailScreen extends Screen implements WidgetEXUtils.IWidgetEX {
    private final Screen PreviousScreen;
    private final Component DetailText;

    public DetailScreen(Screen PreviousScreen, Component DetailText) {
        super(Component.nullToEmpty("Detail Screen"));
        this.PreviousScreen = PreviousScreen;
        this.DetailText = DetailText;
    }

    public void init() {
        int TextX = 20;
        int TextY = 40;
        int TextSizeX = width - TextX * 2;
        int TextSizeY = height - 60;
        int TextDefaultColor = 0xFFFFFF;
        ScaleScrollTextWidget DetailTextWidget = (ScaleScrollTextWidget) new ScaleScrollTextWidget(TextX, TextY, TextSizeX, TextSizeY / 9, 1.0f, DetailText, font).setColor(TextDefaultColor);
        DetailTextWidget.setEnableScrollableIconRender(true);
        this.addWidget((WidgetEXUtils.IWidgetEX) DetailTextWidget);
        this.addRenderableWidget(DetailTextWidget);
        int ButtonX = width - 30;
        int ButtonY = 10;
        int ButtonSizeX = 20;
        int ButtonSizeY = 20;
        Button CloseButton = Button.builder(Component.nullToEmpty("X"), (button) -> {this.onClose();}).pos(ButtonX, ButtonY).size(ButtonSizeX, ButtonSizeY).build();
        this.addRenderableWidget(CloseButton);
    }

    @Override
    public void onClose() {
	    if (this.minecraft != null) {
		    this.minecraft.setScreen(this.PreviousScreen);
	    }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public WidgetEXUtils.WidgetRect getRect() {
        return null;
    }

    public List<WidgetEXUtils.IWidgetEX> WidgetList = new ArrayList<>();

    @Override
    public List<WidgetEXUtils.IWidgetEX> getWidgetList() {
        return this.WidgetList;
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent mouseButtonEvent, boolean bl) {
        this.onClickWidget(mouseButtonEvent.x(), mouseButtonEvent.y(), mouseButtonEvent.button());
        return super.mouseClicked(mouseButtonEvent, bl);
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent mouseButtonEvent) {
        this.onReleaseWidget(mouseButtonEvent.x(), mouseButtonEvent.y(), mouseButtonEvent.button());
        return super.mouseReleased(mouseButtonEvent);
    }

    @Override
    public boolean mouseDragged(@NonNull MouseButtonEvent mouseButtonEvent, double d, double e) {
        this.onDragWidget(mouseButtonEvent.x(), mouseButtonEvent.y(), mouseButtonEvent.button(), d, e);
        return super.mouseDragged(mouseButtonEvent, d, e);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.onScrollWidget(mouseX, mouseY, verticalAmount);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}