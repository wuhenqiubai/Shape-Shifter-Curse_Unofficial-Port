package net.onixary.shapeShifterCurseFabric.config;

import io.github.apace100.apoli.util.ApoliConfigClient;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.client.ShapeShifterCurseFabricClient;
import net.onixary.shapeShifterCurseFabric.custom_ui.FormColorSelectMenu;
import net.onixary.shapeShifterCurseFabric.custom_ui.FormColorSelectMenuV2;
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsS2C;

import java.util.function.Supplier;

// 配置菜单Mod 只支持一个配置菜单 添加一个选择菜单
public class ConfigMenuScreen extends Screen {
    private Screen parent;
    public ConfigMenuScreen(Screen parent) {
        super(Component.translatable("text.shape-shifter-curse.config.title"));
    }

    public void init() {
        int Config_BTN_Size_X = 240;  // 按钮长 英文过长时请修改
        int Config_BTN_Size_Y = 20;  // 按钮宽
        int Config_BTN_Interval = 10;  // 按钮Y方向间隔 留空部分
        int Config_Count = 6;  // 配置数量  **** 添加配置时修改 ****
        int Additional_Button_Count = 1;  // 额外按钮数量 [关闭配置界面按钮]
        int Config_BTN_X_Pos = (width - Config_BTN_Size_X) / 2;  // 按钮X坐标
        int Config_BTN_Y_Start_Pos = (height - Config_BTN_Size_Y * (Config_Count + Additional_Button_Count) - Config_BTN_Interval * (Config_Count + Additional_Button_Count - 1)) / 2;  // 按钮Y坐标起始位置
        int Config_BTN_Y_Pos = Config_BTN_Y_Start_Pos;  // 按钮Y坐标

        // 添加按钮
        // 玩家自定义配置
        AddButton(Config_BTN_X_Pos, Config_BTN_Y_Pos, Config_BTN_Size_X, Config_BTN_Size_Y, Component.translatable("text.autoconfig.shape-shifter-curse-custom.title"), AutoConfig.getConfigScreen(PlayerCustomConfig.class, this));
        Config_BTN_Y_Pos += Config_BTN_Size_Y + Config_BTN_Interval;
        // 客户端配置
        AddButton(Config_BTN_X_Pos, Config_BTN_Y_Pos, Config_BTN_Size_X, Config_BTN_Size_Y, Component.translatable("text.autoconfig.shape-shifter-curse-client.title"), AutoConfig.getConfigScreen(ClientConfig.class, this));
        Config_BTN_Y_Pos += Config_BTN_Size_Y + Config_BTN_Interval;
        // 双端配置
        AddButton(Config_BTN_X_Pos, Config_BTN_Y_Pos, Config_BTN_Size_X, Config_BTN_Size_Y, Component.translatable("text.autoconfig.shape-shifter-curse-common.title"), AutoConfig.getConfigScreen(CommonConfig.class, this));
        Config_BTN_Y_Pos += Config_BTN_Size_Y + Config_BTN_Interval;

        // 自定义形态颜色新 UI V2
        AddButton(Config_BTN_X_Pos, Config_BTN_Y_Pos, Config_BTN_Size_X, Config_BTN_Size_Y, Component.translatable("text.shape-shifter-curse.config.form_color_select_menu_v2"), () -> new FormColorSelectMenuV2(Component.translatable("text.shape-shifter-curse.config.form_color_select_menu"), this));
        Config_BTN_Y_Pos += Config_BTN_Size_Y + Config_BTN_Interval;

        // 自定义形态颜色新 UI V1
        AddButton(Config_BTN_X_Pos, Config_BTN_Y_Pos, Config_BTN_Size_X, Config_BTN_Size_Y, Component.translatable("text.shape-shifter-curse.config.form_color_select_menu"), () -> new FormColorSelectMenu(Component.translatable("text.shape-shifter-curse.config.form_color_select_menu"), this));
        Config_BTN_Y_Pos += Config_BTN_Size_Y + Config_BTN_Interval;

        // 被吞进去的Apoli配置 **** 提取起源模组时记得删除这项 ****
        AddButton(Config_BTN_X_Pos, Config_BTN_Y_Pos, Config_BTN_Size_X, Config_BTN_Size_Y, Component.translatable("text.autoconfig.power_config.title"), AutoConfig.getConfigScreen(ApoliConfigClient.class, this));
        Config_BTN_Y_Pos += Config_BTN_Size_Y + Config_BTN_Interval;

        // **** 在这里添加配置 ****

        // 关闭配置界面按钮
        AddCloseButton(Config_BTN_X_Pos, Config_BTN_Y_Pos, Config_BTN_Size_X, Config_BTN_Size_Y, Component.translatable("text.shape-shifter-curse.config.close"));
    }

    public void AddButton(int PosX, int PosY, int SizeX, int SizeY, Component text, Supplier<Screen> ConfigScreenSupplier) {
        addRenderableWidget(Button.builder(text, button -> {
            Minecraft.getInstance().setScreen(ConfigScreenSupplier.get());
        }).size(SizeX, SizeY).pos(PosX, PosY).build());
    }

    public void AddCloseButton(int PosX, int PosY, int SizeX, int SizeY, Component text) {
        addRenderableWidget(Button.builder(text, button -> onClose()).size(SizeX, SizeY).pos(PosX, PosY).build());
    }

    @Override
    public void onClose() {
        ShapeShifterCurseFabric.LOGGER.info("Sending custom settings to server");
        if (ShapeShifterCurseFabric.clientConfig.unlockAllFormInFormColorSelectMenu) {
            ShapeShifterCurseFabricClient.formColorData.unlockAll();
            ShapeShifterCurseFabric.clientConfig.unlockAllFormInFormColorSelectMenu = false;
            AutoConfig.getConfigHolder(ClientConfig.class).save();
        }
        if (ShapeShifterCurseFabric.clientConfig.clearFormUnlockRecordInFormColorSelectMenu) {
            ShapeShifterCurseFabricClient.formColorData.clearFormUnlock();
            ShapeShifterCurseFabric.clientConfig.clearFormUnlockRecordInFormColorSelectMenu = false;
            AutoConfig.getConfigHolder(ClientConfig.class).save();
        }
        try {
            ModPacketsS2C.sendUpdateCustomSetting();  // 尝试发送自定义配置到服务器
        }
        catch (Exception ignored) {}  // 如果不在服务器上则忽略
        Minecraft.getInstance().setScreen(parent);
    }

    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // 1.21.11: Screen.render 已自动调用 renderBackground（内部 blur），
        // 这里再手动调会 "Can only blur once per frame" 崩溃，只需 super.render
        super.render(context, mouseX, mouseY, delta);
    }
}
