package net.onixary.shapeShifterCurseFabric.custom_ui.ui_part;


import java.util.List;
import net.minecraft.util.Tuple;

public class WidgetEXUtils {
    public static class WidgetRect {
        public int x;
        public int y;
        public int width;
        public int height;

        public WidgetRect(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public boolean isMouseInside(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }

        public Tuple<Double, Double> getMousePos(double mouseX, double mouseY) {
            return new Tuple<>(mouseX - x, mouseY - y);
        }
    }

	public interface IWidgetEX {
		WidgetRect getRect();

		List<IWidgetEX> getWidgetList();

		default void addWidget(IWidgetEX widget) {
            getWidgetList().add(widget);
        }

		default void onClickWidget(double mouseX, double mouseY, int button) {
            for (IWidgetEX widget : getWidgetList()) {
                WidgetRect rect = widget.getRect();
                if (rect.isMouseInside(mouseX, mouseY)) {
                    Tuple<Double, Double> newMousePos = rect.getMousePos(mouseX, mouseY);
                    widget.onClickWidget(newMousePos.getA(), newMousePos.getB(), button);
                }
            }
		}

		default void onReleaseWidget(double mouseX, double mouseY, int button) {
            for (IWidgetEX widget : getWidgetList()) {
                WidgetRect rect = widget.getRect();
                if (rect.isMouseInside(mouseX, mouseY)) {
                    Tuple<Double, Double> newMousePos = rect.getMousePos(mouseX, mouseY);
                    widget.onReleaseWidget(newMousePos.getA(), newMousePos.getB(), button);
                }
            }
		}

		default void onDragWidget(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            for (IWidgetEX widget : getWidgetList()) {
                WidgetRect rect = widget.getRect();
                if (rect.isMouseInside(mouseX, mouseY)) {
                    Tuple<Double, Double> newMousePos = rect.getMousePos(mouseX, mouseY);
                    widget.onDragWidget(newMousePos.getA(), newMousePos.getB(), button, deltaX, deltaY);
                }
            }
		}

		default void onScrollWidget(double mouseX, double mouseY, double mouseZ) {
            for (IWidgetEX widget : getWidgetList()) {
                WidgetRect rect = widget.getRect();
                if (rect.isMouseInside(mouseX, mouseY)) {
                    Tuple<Double, Double> newMousePos = rect.getMousePos(mouseX, mouseY);
                    widget.onScrollWidget(newMousePos.getA(), newMousePos.getB(), mouseZ);
                }
            }
		}
    }
}
