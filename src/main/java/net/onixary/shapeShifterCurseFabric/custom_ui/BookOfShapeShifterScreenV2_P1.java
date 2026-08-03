package net.onixary.shapeShifterCurseFabric.custom_ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.cursed_moon.CursedMoonClient;
import net.onixary.shapeShifterCurseFabric.custom_ui.ui_part.ColorStringWidget;
import net.onixary.shapeShifterCurseFabric.custom_ui.ui_part.ScaleScrollTextWidget;
import net.onixary.shapeShifterCurseFabric.custom_ui.ui_part.ScaleTextRenderer;
import net.onixary.shapeShifterCurseFabric.custom_ui.ui_part.WidgetEXUtils;
import net.onixary.shapeShifterCurseFabric.data.CodexData;
import org.joml.Quaternionf;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

public class BookOfShapeShifterScreenV2_P1 extends Screen implements WidgetEXUtils.IWidgetEX {
    private static final Identifier page_texID = Identifier.fromNamespaceAndPath(MOD_ID,"textures/gui/codex_page_1.png");
    private static final Identifier cursed_moon_icon_texID = Identifier.fromNamespaceAndPath(MOD_ID,"textures/gui/book_cursed_moon_icon.png");
    public Player currentPlayer;
    public static final int BookSizeX = 350;
    public static final int BookSizeY = 220;

    public static final Component openFCSMenuButtonLabel = Component.translatable("gui.shape_shifter_curse_fabric.book_2_1.open_fcs_menu");

    public BookOfShapeShifterScreenV2_P1() {
        super(Component.nullToEmpty("ShapeShifterCurse_Book_Screen_V2"));
    }

    @Override
    public void init() {
        int BookScale = 1;
        float Scale = 0.5f;
        if (ShapeShifterCurseFabric.clientConfig.newStartBookForBiggerScreen) {
            BookScale = 2;
            Scale *= BookScale;
        }
        int BookPosX = width / 2 - (BookSizeX * BookScale) / 2;
        int BookPosY = height / 2 - (BookSizeY * BookScale) / 2;
        // 1.21.11 文字渲染用标准 ARGB alpha 做透明度：0x222222 的 alpha=0x22(13%) 会让正文几乎透明不可见。
        // 保持 RGB 不变（上游乘法模式语义），补全 alpha=FF 恢复可见性。
        int DefaultTextColor = 0xFF222222;
        int HeaderTextColor = 0xFFDDDDDD;
        ScaleTextRenderer scaleTextRenderer = new ScaleTextRenderer(font);
        scaleTextRenderer.Scale = Scale;
        // Title
        // D -> (9, 9), (19, 95)
        // Size -> (108, 48) Pos -> (17, 92)
        this.addRenderableWidget(BuildDetailScreenButton(19, 95, 9, 9, CodexData.getContentText(CodexData.ContentType.TITLE, currentPlayer)));
        ScaleScrollTextWidget TitleLabel = (ScaleScrollTextWidget) new ScaleScrollTextWidget(BookPosX + 17 * BookScale, BookPosY + 105 * BookScale, 108 * BookScale, 5 * BookScale, Scale, CodexData.getContentText(CodexData.ContentType.TITLE, currentPlayer), scaleTextRenderer).shadow(false).setColor(DefaultTextColor);
        TitleLabel.setEnableScrollableIconRender(true);
        this.addWidget((WidgetEXUtils.IWidgetEX) TitleLabel);
        this.addRenderableWidget(TitleLabel);
        // Equip 190
        // D -> (9, 9), (116, 143)
        // Size -> (107, 56) Pos -> (17, 153)
        this.addRenderableWidget(BuildDetailScreenButton(116, 143, 9, 9, CodexData.getContentText(CodexData.ContentType.EQUIP, currentPlayer)));
        this.addRenderableWidget(new ColorStringWidget(BookPosX + 17 * BookScale, BookPosY + 143 * BookScale, 107 * BookScale, 6 * BookScale, CodexData.headerEquip, font).setColor(HeaderTextColor));
        ScaleScrollTextWidget StatusLabel = (ScaleScrollTextWidget) new ScaleScrollTextWidget(BookPosX + 17 * BookScale, BookPosY + 153 * BookScale, 107 * BookScale, 6 * BookScale, Scale, CodexData.getContentText(CodexData.ContentType.EQUIP, currentPlayer), scaleTextRenderer).shadow(false).setColor(DefaultTextColor);
        StatusLabel.setEnableScrollableIconRender(true);
        this.addWidget((WidgetEXUtils.IWidgetEX) StatusLabel);
        this.addRenderableWidget(StatusLabel);
        // Open FCS Menu Button
        // 21,194,98,11
        this.addRenderableWidget(Button.builder(openFCSMenuButtonLabel, button -> {
                    if (ShapeShifterCurseFabric.clientConfig.fcs_use_v1_menu) {
                        if (FormColorSelectMenu.instance == null) {
                            Screen screen = new FormColorSelectMenu(Component.literal("text.shape-shifter-curse.config.form_color_select_menu"), this);
	                        if (minecraft != null) {
		                        minecraft.setScreen(screen);
	                        }
                        }
                    } else {
                        if (FormColorSelectMenuV2.instance == null) {
                            Screen screen = new FormColorSelectMenuV2(Component.literal("text.shape-shifter-curse.config.form_color_select_menu_v2"), this);
	                        if (minecraft != null) {
		                        minecraft.setScreen(screen);
	                        }
                        }
                    }
        }).pos(BookPosX + 31 * BookScale, BookPosY + 194 * BookScale).size(78 * BookScale, 14 * BookScale).build());
        // Appearance
        // D -> (9, 9), (311, 13)
        // Size -> (176, 184) Pos -> (142, 23)
        this.addRenderableWidget(BuildDetailScreenButton(311, 13, 9, 9, CodexData.getContentText(CodexData.ContentType.APPEARANCE, currentPlayer)));
        this.addRenderableWidget(new ColorStringWidget(BookPosX + 142 * BookScale, BookPosY + 11 * BookScale, 176 * BookScale, 8 * BookScale, CodexData.headerAppearance, font).setColor(HeaderTextColor));
        ScaleScrollTextWidget AppearanceLabel = (ScaleScrollTextWidget) new ScaleScrollTextWidget(BookPosX + 142 * BookScale, BookPosY + 26 * BookScale, 176 * BookScale, 20 * BookScale, Scale, CodexData.getContentText(CodexData.ContentType.APPEARANCE, currentPlayer), scaleTextRenderer).shadow(false).setColor(DefaultTextColor);
        AppearanceLabel.setEnableScrollableIconRender(true);
        this.addWidget((WidgetEXUtils.IWidgetEX) AppearanceLabel);
        this.addRenderableWidget(AppearanceLabel);
        // 下一页按钮
        int NextPage_ButtonSizeX = 15 * BookScale;
        int NextPage_ButtonSizeY = 30 * BookScale;
        int NextPage_ButtonPosX = width / 2 + (BookSizeX * BookScale) / 2 - 18 * BookScale;
        int NextPage_ButtonPosY = height / 2 - NextPage_ButtonSizeY / 2;
        this.addRenderableWidget(
                Button.builder(Component.nullToEmpty(">"), button -> NextPage()).size(NextPage_ButtonSizeX, NextPage_ButtonSizeY).pos(NextPage_ButtonPosX, NextPage_ButtonPosY).build()
        );
    }

    private void RenderEntity(GuiGraphics context, int x, int y, int size, int mouseX, int mouseY, LivingEntity entity) {
        float f = (float)Math.atan((double)(mouseX / 40.0F));
        float g = (float)Math.atan((double)(mouseY / 40.0F));
        Quaternionf quaternionf = (new Quaternionf()).rotateZ(3.1415927F);
        Quaternionf quaternionf2 = (new Quaternionf()).rotateX(g * 20.0F * 0.017453292F);
        quaternionf.mul(quaternionf2);
        float h = entity.yBodyRot;
        float i = entity.getYRot();
        float j = entity.getXRot();
        float k = entity.yHeadRotO;
        float l = entity.yHeadRot;
        float m = entity.yBodyRotO;
        entity.yBodyRot = 180.0F + f * 20.0F;
        entity.yBodyRotO = entity.yBodyRot;
        entity.setYRot(180.0F + f * 40.0F);
        entity.setXRot(-g * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();

        int x1 = x - size;
        int y1 = y - size;
        int x2 = x + size;
        int y2 = y + size;
        InventoryScreen.renderEntityInInventoryFollowsMouse(context, x1, y1, x2, y2, size, 0.0625f, mouseX, mouseY, entity);

        entity.yBodyRot = h;
        entity.yBodyRotO = m;
        entity.setYRot(i);
        entity.setXRot(j);
        entity.yHeadRotO = k;
        entity.yHeadRot = l;
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
        BookOfShapeShifterScreenV2_P2 NextPage = new BookOfShapeShifterScreenV2_P2();
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
        int BookScale = 1;
        int FinalBookSizeX = BookSizeX;
        int FinalBookSizeY = BookSizeY;
        if (ShapeShifterCurseFabric.clientConfig.newStartBookForBiggerScreen) {
            BookScale *= 2;
            FinalBookSizeX = (BookSizeX * BookScale);
            FinalBookSizeY = (BookSizeY * BookScale);
        }
        int BookPosX = width / 2 - FinalBookSizeX / 2;
        int BookPosY = height / 2 - FinalBookSizeY / 2;
        this.RenderBook(context);
	    super.render(context, mouseX, mouseY, delta);
        // 实体渲染原点为实体中心脚下
        // Size -> (70, 66) Pos -> (35, 15)
        int PlayerX = BookPosX + 70 * BookScale;
	    int PlayerY = BookPosY + 45 * BookScale;
        this.RenderEntity(context, PlayerX, PlayerY, 30 * BookScale, PlayerX - mouseX, PlayerY - 37 * BookScale - mouseY, currentPlayer);
        // Cursed Moon Icon
        // Size -> (8, 8), Pos -> (115, 92)
        context.blit(RenderPipelines.GUI_TEXTURED, cursed_moon_icon_texID, BookPosX + 115 * BookScale, BookPosY + 92 * BookScale,
                CursedMoonClient.isCursedMoon ? 8 : 0, 0, 8 * BookScale, 8 * BookScale, 8, 8, 16, 8, -1);
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