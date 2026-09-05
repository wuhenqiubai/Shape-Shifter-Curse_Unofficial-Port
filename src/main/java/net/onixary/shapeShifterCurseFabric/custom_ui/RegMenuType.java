package net.onixary.shapeShifterCurseFabric.custom_ui;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

// CV高手忘了怎么写了 从我很久以前的Mod CV的 自己借鉴自己属于是
public class RegMenuType {

    public static Identifier ALTER_CRAFT_UI_ID = ShapeShifterCurseFabric.identifier("alter_craft_ui");
    public static MenuType<AlterCraftUIHandler> AlterCraftUI = register(ALTER_CRAFT_UI_ID, new MenuType<>(AlterCraftUIHandler::createMenu, FeatureFlags.VANILLA_SET));
    public static <T extends AbstractContainerMenu> MenuType<T> register(Identifier id, MenuType<T> factory) {
        Registry.register(BuiltInRegistries.MENU, id, factory);
        return factory;
    }
}
