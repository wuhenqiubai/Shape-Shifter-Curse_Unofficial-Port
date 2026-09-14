package net.onixary.shapeShifterCurseFabric.custom_ui.ui_part;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;

public class ScaleScrollTextWidget extends MultiLineTextWidget implements WidgetEXUtils.IWidgetEX {
    private final float Scale;
    private final float realScale;
    private boolean shadow;
    private int textColor = 0xFFFFFFFF;


    private int realWidth;
    private int MaxWidth;
    private int MaxRows;

    private int boxHeight = 0;

    private final List<WidgetEXUtils.IWidgetEX> widgetList = List.of();
    private WidgetEXUtils.WidgetRect rect;

    public boolean enableScrollableIconRender = false;
    public int IconSize = 8;
    public Identifier IconTexID = ShapeShifterCurseFabric.identifier("textures/gui/scrollable_icon.png");

    public int scroll = 0;  // 单位改成像素


    public ScaleScrollTextWidget(int x, int y, int width, int height, float Scale, Component message, Font textRenderer) {
        super(x, y, message, textRenderer);
        this.Scale = Scale;
        int textHeight = Math.round(textRenderer.lineHeight * Scale);
        this.realScale = (float) textHeight / (float) textRenderer.lineHeight;
        this.rect = new WidgetEXUtils.WidgetRect(x, y, width, height);
        assert width > 0;
        assert height > 0;
        this.setMaxWidth(width);
        this.setMaxRows(1_000_000_000);
        this.boxHeight = height;
    }

    @Override
    public WidgetEXUtils.WidgetRect getRect() {
        return this.rect;
    }

    @Override
    public List<WidgetEXUtils.IWidgetEX> getWidgetList() {
        return this.widgetList;
    }


    private double deltaYTotal = 0;
    private double scrollZTotal = 0;

    @Override
    public void onClickWidget(double mouseX, double mouseY, int button) {
        if (this.enableScrollableIconRender) {
            if (mouseX >= this.realWidth - IconSize && mouseX <= this.realWidth && mouseY >= 0 && mouseY < IconSize) {
                this.scroll(-this.boxHeight);
            }
            if (mouseX >= this.realWidth - IconSize && mouseX <= this.realWidth && mouseY >= this.boxHeight - IconSize && mouseY < this.boxHeight) {
                this.scroll(this.boxHeight);
            }
        }
    }

    @Override
    public void onDragWidget(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.enableScrollableIconRender && mouseX >= this.realWidth) {
            return;
        }
        deltaYTotal += deltaY;
        if (deltaYTotal > 1 || deltaYTotal < -1) {
            int amount = (int) (deltaYTotal);
            deltaYTotal -= amount;
            this.scroll(-amount);
        }
    }

    @Override
    public void onScrollWidget(double mouseX, double mouseY, double mouseZ) {
        if (this.enableScrollableIconRender && mouseX >= this.realWidth) {
            return;
        }
        scrollZTotal += mouseZ;
        if (scrollZTotal > 0.0625f || scrollZTotal < -0.0625f) {
            int amount = (int) (scrollZTotal * 16);
            scrollZTotal -= amount * 0.0625f;
            this.scroll(-amount);
        }
    }

    public ScaleScrollTextWidget shadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public ScaleScrollTextWidget setColor(int color) {
        this.textColor = color;
        return this;
    }

    public int getColor() {
        return this.textColor;
    }

    public ScaleScrollTextWidget setEnableScrollableIconRender(boolean enableScrollableIconRender) {
        if (this.enableScrollableIconRender != enableScrollableIconRender) {
            if (enableScrollableIconRender) {
                this.modMaxWidth(-IconSize);
            } else {
                this.modMaxWidth(0);
            }
            this.enableScrollableIconRender = enableScrollableIconRender;
            this.scroll = 0;
        }
        return this;
    }

    public void reloadText(Component message) {
        this.setMessage(message);
        this.scroll = 0;
    }

    public void scroll(int amount) {
        this.scroll += amount;
        if (this.scroll > this.getHeight() - this.boxHeight) {
            this.scroll = this.getHeight() - this.boxHeight;
        }
        if (this.scroll < 0) {
            this.scroll = 0;
        }
    }

    public int modMaxWidth = 0;

    public void modMaxWidth(int value) {
        this.modMaxWidth = value;
        super.setMaxWidth(this.MaxWidth + this.modMaxWidth);
    }

    @Override
    public @NotNull MultiLineTextWidget setMaxWidth(int maxWidth) {
        this.realWidth = maxWidth;
        this.MaxWidth = Math.round(maxWidth * (1 / this.Scale));
        super.setMaxWidth(this.MaxWidth + this.modMaxWidth);
        return this;
    }

    @Override
    public @NotNull MultiLineTextWidget setMaxRows(int maxRows) {
        this.MaxRows = Math.round(maxRows * (1 / this.Scale));
        super.setMaxRows(this.MaxRows);
        return this;
    }

    @Override
    public int getWidth() {
        return (int) ((this.MaxWidth + this.modMaxWidth) * this.Scale);
    }

    public int getTextWidth() {
        return this.MaxWidth + this.modMaxWidth;
    }

    @Override
    public int getHeight() {
        return (int) (super.getHeight() * this.realScale);
    }

    @Override
    public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int i = this.getX();
        int j = this.getY();
        if (this.enableScrollableIconRender) {
            if (this.scroll > 0) {
                context.blit(RenderPipelines.GUI_TEXTURED, IconTexID, i + realWidth - IconSize, j, 0, 0, IconSize, IconSize, IconSize, IconSize, IconSize, IconSize * 2, -1);
            }
            if (this.scroll < this.getHeight() - this.boxHeight) {
                context.blit(RenderPipelines.GUI_TEXTURED, IconTexID, i + realWidth - IconSize, j + boxHeight - IconSize, 0, IconSize, IconSize, IconSize, IconSize, IconSize, IconSize, IconSize * 2, -1);
            }
        }
        Font font = Objects.requireNonNull(this.getFont());
        // 文字行距/居中用未缩放值，缩放由下面的 GUI pose 完成
        int k = 9;
        int l = this.getColor();
        List<FormattedCharSequence> lines = font.split(this.getMessage(), this.getTextWidth());
        // 这API真好用 比我硬算剔除好写不止一点
        context.enableScissor(i, j, i + this.getWidth(), j + this.boxHeight);
        // 1.21.11: MultiLineLabel 只剩 visitLines(...)，renderCentered/renderLeftAligned 已删除；
        // 且 AbstractStringWidget 不再提供 shadow 开关，文字渲染走 GuiTextRenderState（延迟提交）、
        // 字形缩放只能由保存的 2D pose 决定。故这里自己按行画：pose 缩放 Scale 倍 + scroll 走设备像素。
        Matrix3x2fStack pose = context.pose();
        pose.pushMatrix();
        pose.translate(i, j - this.scroll);
        pose.scale(this.Scale, this.Scale);
        pose.translate(-i, -j);
        int lineY = j;
        for (FormattedCharSequence line : lines) {
            int lineX = this.centered ? i + (this.MaxWidth + this.modMaxWidth - font.width(line)) / 2 : i;
            context.text(font, line, lineX, lineY, l, this.shadow);
            lineY += k;
        }
        pose.popMatrix();
        context.disableScissor();
    }
}