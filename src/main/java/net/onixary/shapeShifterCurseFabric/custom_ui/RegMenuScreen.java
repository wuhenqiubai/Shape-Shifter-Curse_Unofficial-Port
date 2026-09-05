package net.onixary.shapeShifterCurseFabric.custom_ui;


import net.minecraft.client.gui.screens.MenuScreens;

public class RegMenuScreen {
    public static void init() {
        MenuScreens.register(RegMenuType.AlterCraftUI, AlterCraftUI::new);
    }
}
