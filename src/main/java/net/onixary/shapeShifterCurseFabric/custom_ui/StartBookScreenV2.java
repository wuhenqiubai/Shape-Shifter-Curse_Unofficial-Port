package net.onixary.shapeShifterCurseFabric.custom_ui;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.custom_ui.ui_part.ScaleScrollTextWidget;
import net.onixary.shapeShifterCurseFabric.custom_ui.ui_part.WidgetEXUtils;
import net.onixary.shapeShifterCurseFabric.networking.BytePayload;
import net.onixary.shapeShifterCurseFabric.networking.ModPackets;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

public class StartBookScreenV2 extends Screen implements WidgetEXUtils.IWidgetEX {
    private static final Identifier StartBook_TexID = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/start_book.png");
    public Player currentPlayer;

    public static final int BookSizeX = 360;
    public static final int BookSizeY = 330;
    public static final int TextSizeX = 270;
    public static final int TextSizeY = 300;
    public static final int ButtonSizeX = 200;
    public static final int ButtonSizeY = 30;

    public StartBookScreenV2() {
        super(Component.nullToEmpty("ShapeShifterCurse_StartBook_Screen_V2"));
    }

    @Override
    public void init() {
        int TextPosYFix = 75;
        int ButtonPosYFix = -100;
        if (ShapeShifterCurseFabric.clientConfig.newStartBookForBiggerScreen) {
            TextPosYFix = 0;
            ButtonPosYFix = -50;
        }
        // 渲染文字
        int TextPosX = width / 2 - TextSizeX / 2;
        int TextPosY = height / 2 - TextSizeY / 2 + TextPosYFix;
        ScaleScrollTextWidget StartBookLabel = new ScaleScrollTextWidget(TextPosX, TextPosY, TextSizeX, TextSizeY / 9, 1.0f, Component.translatable("screen.shape-shifter-curse.book_of_shape_shifter.start_content_text"), font);
        StartBookLabel.setEnableScrollableIconRender(true);
        this.addWidget((WidgetEXUtils.IWidgetEX) StartBookLabel);
        this.addRenderableWidget(StartBookLabel);
        // 渲染按钮
        int BookBottomY = height / 2 + BookSizeY / 2;
        int ButtonPosX = width / 2 - ButtonSizeX / 2;
        int ButtonPosY = BookBottomY + ButtonPosYFix;
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.shape-shifter-curse.book_of_shape_shifter.start_button_text"),
                button -> {
                    FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                    // buf.writeUuid(currentPlayer.getUuid());
                    // 发送到服务端
                    ClientPlayNetworking.send(new BytePayload(BytePayload.id(ModPackets.VALIDATE_START_BOOK_BUTTON),  buf));
                    if(Minecraft.getInstance().screen instanceof StartBookScreenV2){
                        Minecraft.getInstance().setScreen(null);
                    }
                    this.onClose(); // 关闭当前界面
                }
        ).size(ButtonSizeX, ButtonSizeY).pos(ButtonPosX, ButtonPosY).build());
    }

    private void RenderBook(GuiGraphics context) {
        int BookPosX = width / 2 - BookSizeX / 2;
        int BookPosY = height / 2 - BookSizeY / 2;
        context.blit(RenderPipelines.GUI_TEXTURED, StartBook_TexID, BookPosX, BookPosY, 0, 0, BookSizeX, BookSizeY, BookSizeX, BookSizeY, BookSizeX, BookSizeY, -1);
    }

    @Override
    public void renderBackground(@NonNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        // No blur — book texture serves as the background
    }

    @Override
    public void render(@NonNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.RenderBook(context);
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