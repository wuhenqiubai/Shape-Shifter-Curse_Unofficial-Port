package net.onixary.shapeShifterCurseFabric.player_form.skin;

public record FormColorSetting(int primaryColor, int accentColor1, int accentColor2, int eyeColorA, int eyeColorB
        , boolean primaryGreyReverse, boolean accent1GreyReverse, boolean accent2GreyReverse) {
    public int getPrimaryColor() {
        return this.primaryColor;
    }

    public int getAccentColor1() {
        return this.accentColor1;
    }

    public int getAccentColor2() {
        return this.accentColor2;
    }

    public int getEyeColorA() {
        return this.eyeColorA;
    }

    public int getEyeColorB() {
        return this.eyeColorB;
    }

    public boolean getPrimaryGreyReverse() {
        return this.primaryGreyReverse;
    }

    public boolean getAccent1GreyReverse() {
        return this.accent1GreyReverse;
    }

    public boolean getAccent2GreyReverse() {
        return this.accent2GreyReverse;
    }

    public static int RGBA2ABGR(int color) {
        return (color << 24) | ((color << 8) & 0x00FF0000) | ((color >> 8) & 0x0000FF00) | (color >>> 24);
    }

    public static int ABGR2RGBA(int color) {
        return (color << 24) | ((color << 8) & 0x00FF0000) | ((color >> 8) & 0x0000FF00) | (color >>> 24);
    }
}
