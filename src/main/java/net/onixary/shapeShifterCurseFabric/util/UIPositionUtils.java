package net.onixary.shapeShifterCurseFabric.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Tuple;

@Environment(EnvType.CLIENT)
public class UIPositionUtils {

    // 矫正点
    // 1 2 3
    // 4 5 6
    // 7 8 9
    // 额外XY偏移量

    public static Tuple<Integer, Integer> getCorrectPosition(int positionType, int extraX, int extraY) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getWindow() == null) {
            return new Tuple<>(0, 0);
        }

        int windowWidth = client.getWindow().getGuiScaledWidth();
        int windowHeight = client.getWindow().getGuiScaledHeight();

        int centerX = windowWidth / 2;
        int centerY = windowHeight / 2;

        int posX = 0;
        int posY = 0;
        
        switch (positionType) {
            case 1 -> {
                posX = 0;
                posY = 0;
            }
            case 2 -> {
                posX = centerX;
                posY = 0;
            }
            case 3 -> {
                posX = windowWidth;
                posY = 0;
            }
            case 4 -> {
                posX = 0;
                posY = centerY;
            }
            case 5 -> {
                posX = centerX;
                posY = centerY;
            }
            case 6 -> {
                posX = windowWidth;
                posY = centerY;
            }
            case 7 -> {
                posX = 0;
                posY = windowHeight;
            }
            case 8 -> {
                posX = centerX;
                posY = windowHeight;
            }
            case 9 -> {
                posX = windowWidth;
                posY = windowHeight;
            }
            default -> {
                posX = centerX;
                posY = centerY;
            }
        }

        return new Tuple<>(posX + extraX, posY + extraY);
    }
}
