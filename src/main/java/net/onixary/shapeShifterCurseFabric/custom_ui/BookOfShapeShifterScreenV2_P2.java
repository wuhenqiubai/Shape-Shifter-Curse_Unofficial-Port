package net.onixary.shapeShifterCurseFabric.custom_ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.custom_ui.ui_part.ScaleMultilineTextWidget;
import net.onixary.shapeShifterCurseFabric.custom_ui.ui_part.ScaleScrollTextWidget;
import net.onixary.shapeShifterCurseFabric.custom_ui.ui_part.ScaleTextRenderer;
import net.onixary.shapeShifterCurseFabric.custom_ui.ui_part.WidgetEXUtils;
import net.onixary.shapeShifterCurseFabric.data.CodexData;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

public class BookOfShapeShifterScreenV2_P2 extends Screen implements WidgetEXUtils.IWidgetEX {
    private static final Identifier page_texID = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/codex_page_2.png");
    public Player currentPlayer;
    public static final int BookSizeX = 350;
    public static final int BookSizeY = 220;

    public BookOfShapeShifterScreenV2_P2() {
        super(Component.nullToEmpty("ShapeShifterCurse_Book_Screen_V2"));
    }

    @Override
    public void init() {
        float Scale = 0.5f;
        int BookScale = 1;
        if (ShapeShifterCurseFabric.clientConfig.newStartBookForBiggerScreen) {
            BookScale = 2;
            Scale *= BookScale;
        }
        int BookPosX = width / 2 - (BookSizeX * BookScale) / 2;
        int BookPosY = height / 2 - (BookSizeY * BookScale) / 2;
        int DefaultTextColor = 0x222222;   // 这里的颜色属于乘法模式 (float)(R1*R2,G1*G2,B1*B2) 需要在lang中修改
        int HeaderTextColor = 0xDDDDDD;
        ScaleTextRenderer scaleTextRenderer = new ScaleTextRenderer(font);
        scaleTextRenderer.Scale = Scale;
        // Pros
        // D -> (9, 9), (80, 12)
        // Size -> (83, 181) Pos -> (13, 26)
        this.addRenderableWidget(BuildDetailScreenButton(80, 12, 9, 9, CodexData.getContentText(CodexData.ContentType.PROS, currentPlayer)));
        this.addRenderableWidget(new StringWidget(BookPosX + 26 * BookScale, BookPosY + 10 * BookScale, 53 * BookScale, 11 * BookScale, CodexData.headerPros, font));
        ScaleScrollTextWidget Pros = (ScaleScrollTextWidget) new ScaleScrollTextWidget(BookPosX + 13 * BookScale, BookPosY + 26 * BookScale, 83 * BookScale, 18 * BookScale, Scale, CodexData.getContentText(CodexData.ContentType.PROS, currentPlayer), scaleTextRenderer).shadow(false).setColor(DefaultTextColor);
        // ScaleScrollTextWidget Pros = (ScaleScrollTextWidget) new ScaleScrollTextWidget(BookPosX + 13 * BookScale, BookPosY + 26 * BookScale, 83 * BookScale, 4 * BookScale, Scale, CodexData.getContentText(CodexData.ContentType.PROS, currentPlayer), scaleTextRenderer).shadow(false).setTextColor(DefaultTextColor);
        Pros.setEnableScrollableIconRender(true);
        this.addWidget((WidgetEXUtils.IWidgetEX) Pros);
        this.addRenderableWidget(Pros);
        // Cons
        // D -> (9, 9), (185, 12)
        // Size -> (82, 182) Pos -> (110, 26)
        this.addRenderableWidget(BuildDetailScreenButton(185, 12, 9, 9, CodexData.getContentText(CodexData.ContentType.CONS, currentPlayer)));
        this.addRenderableWidget(new StringWidget(BookPosX + 120 * BookScale, BookPosY + 10 * BookScale, 63 * BookScale, 11 * BookScale, CodexData.headerCons, font));
        ScaleScrollTextWidget Cons = (ScaleScrollTextWidget) new ScaleScrollTextWidget(BookPosX + 110 * BookScale, BookPosY + 26 * BookScale, 82 * BookScale, 18 * BookScale, Scale, CodexData.getContentText(CodexData.ContentType.CONS, currentPlayer), scaleTextRenderer).shadow(false).setColor(DefaultTextColor);
        // ScaleScrollTextWidget Cons = (ScaleScrollTextWidget) new ScaleScrollTextWidget(BookPosX + 110 * BookScale, BookPosY + 26 * BookScale, 82 * BookScale, 4 * BookScale, Scale, CodexData.getContentText(CodexData.ContentType.CONS, currentPlayer), scaleTextRenderer).shadow(false).setTextColor(DefaultTextColor);
        Cons.setEnableScrollableIconRender(true);
        this.addWidget((WidgetEXUtils.IWidgetEX) Cons);
        this.addRenderableWidget(Cons);
        // Instincts
        // D -> (9, 9), (308, 13)
        // Size -> (106, 136) Pos -> (220, 24)
        this.addRenderableWidget(BuildDetailScreenButton(308, 13, 9, 9, CodexData.getContentText(CodexData.ContentType.INSTINCTS, currentPlayer)));
        this.addRenderableWidget(new StringWidget(BookPosX + 242 * BookScale, BookPosY + 10 * BookScale, 63 * BookScale, 12 * BookScale, CodexData.headerInstincts, font));
        // 在 BookOfShapeShifterScreen 未上色
        MultiLineTextWidget InstinctsDesc = new ScaleMultilineTextWidget(BookPosX + 220 * BookScale, BookPosY + 24 * BookScale, CodexData.getDescText(CodexData.ContentType.INSTINCTS, currentPlayer), scaleTextRenderer, Scale).shadow(false).setMaxWidth(106 * BookScale);
        this.addRenderableWidget(InstinctsDesc);
        int InstinctsDescHeight = InstinctsDesc.getHeight();
        ScaleScrollTextWidget Instincts = (ScaleScrollTextWidget) new ScaleScrollTextWidget(BookPosX + 220 * BookScale, BookPosY + 24 * BookScale + InstinctsDescHeight + Math.round(9 * Scale), 106 * BookScale, ((112 - InstinctsDescHeight) / 9 + 1) * BookScale, Scale, CodexData.getContentText(CodexData.ContentType.INSTINCTS, currentPlayer), scaleTextRenderer).shadow(false).setColor(DefaultTextColor);
        Instincts.setEnableScrollableIconRender(true);
        this.addWidget((WidgetEXUtils.IWidgetEX) Instincts);
        this.addRenderableWidget(Instincts);
        // 下一页按钮
        int NextPage_ButtonSizeX = 15 * BookScale;
        int NextPage_ButtonSizeY = 30 * BookScale;
        int NextPage_ButtonPosX = width / 2 + (BookSizeX * BookScale) / 2 - 18 * BookScale;
        int NextPage_ButtonPosY = height / 2 - NextPage_ButtonSizeY / 2;
        this.addRenderableWidget(
                Button.builder(Component.nullToEmpty(">"), button -> NextPage()).size(NextPage_ButtonSizeX, NextPage_ButtonSizeY).pos(NextPage_ButtonPosX, NextPage_ButtonPosY).build()
        );
    }

    private void RenderBook(GuiGraphics context) {
        int FinalBookSizeX = BookSizeX;
        int FinalBookSizeY = BookSizeY;
        if (ShapeShifterCurseFabric.clientConfig.newStartBookForBiggerScreen) {
            FinalBookSizeX = (BookSizeX * 2);
            FinalBookSizeY = (BookSizeY * 2);
        }
        int BookPosX = width / 2 - FinalBookSizeX / 2;
        int BookPosY = height / 2 - FinalBookSizeY / 2;
        context.blit(RenderPipelines.GUI_TEXTURED, page_texID, BookPosX, BookPosY, 0, 0, FinalBookSizeX, FinalBookSizeY, FinalBookSizeX, FinalBookSizeY, FinalBookSizeX, FinalBookSizeY, -1);
    }

    private void NextPage() {
        BookOfShapeShifterScreenV2_P1 NextPage = new BookOfShapeShifterScreenV2_P1();
        NextPage.currentPlayer = currentPlayer;
        Minecraft.getInstance().setScreen(NextPage);
    }

    private Button BuildDetailScreenButton(int InBookPosX, int InBookPosY, int SizeX, int SizeY, Component DetailText) {
        int BookScale = 1;
        if (ShapeShifterCurseFabric.clientConfig.newStartBookForBiggerScreen) {
            BookScale = 2;
        }
        int BookPosX = width / 2 - (BookSizeX * BookScale) / 2;
        int BookPosY = height / 2 - (BookSizeY * BookScale) / 2;
        int FixedPosX = BookPosX + InBookPosX * BookScale;
        int FixedPosY = BookPosY + InBookPosY * BookScale;
        int FixedSizeX = SizeX * BookScale;
        int FixedSizeY = SizeY * BookScale;
	    return Button.builder(Component.nullToEmpty("+"), button -> Minecraft.getInstance().setScreen(new DetailScreen(this, DetailText))).size(FixedSizeX, FixedSizeY).pos(FixedPosX, FixedPosY).build();
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // No blur — book texture serves as the background
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
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