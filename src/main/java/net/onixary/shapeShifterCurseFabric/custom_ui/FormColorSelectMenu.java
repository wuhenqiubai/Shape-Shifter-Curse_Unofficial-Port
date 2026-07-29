package net.onixary.shapeShifterCurseFabric.custom_ui;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.client.ShapeShifterCurseFabricClient;
import net.onixary.shapeShifterCurseFabric.config.PlayerCustomConfig;
import net.onixary.shapeShifterCurseFabric.custom_ui.ui_part.FCS_ButtonWidget;
import net.onixary.shapeShifterCurseFabric.custom_ui.ui_part.SimpleIntSliderWidget;
import net.onixary.shapeShifterCurseFabric.data.CodexData;
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsS2C;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.skin.PlayerSkinComponent;
import net.onixary.shapeShifterCurseFabric.player_form.skin.RegPlayerSkinComponent;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;
import net.onixary.shapeShifterCurseFabric.util.FormColorData;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.*;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

// 条 更新链(防止StackOverflow)
// 条 -> 输入框 -> 全局数据
// 输入框 -> 条 -> 输入框 -> 全局数据
// 输入框在flag下才能更新全局数据 否则只会修改条的数据
// 条修改输入框的数据时会挂上flag
// 当有flag时 框无法修改条的数据
// flag为int 只有挂上flag的函数才能移除flag(boolean有点难看 所以用int 效果一样)

// HSV RGB 更新链
// HSV 条 -> 更新RGB条函数(updateSliderHSV) -> RGB输入框 -> 全局数据
// HSV 框 -> HSV 条 -> ...
// RGB 条 -> 更新HSV条函数(updateSliderRGB) -> HSV输入框
//       -> 全局数据
// RGB 框 -> RGB 条 -> ...

public class FormColorSelectMenu extends Screen implements FormTextureUtils.TempFormTextureProcessor, FormTextureUtils.TempCustomSkinConfigOverrider, FormTextureUtils.TempFormModelProcessor {
    private static final Identifier texture = Identifier.fromNamespaceAndPath(MOD_ID,"textures/gui/v1_form_color_select_menu.png");
    private static final int BG_WIDTH = 420;
    private static final int BG_HEIGHT = 227;
    private static final int BG_IMAGE_WIDTH = 420;
    private static final int BG_IMAGE_HEIGHT = 428;
    private static final int EXTRA_PART_START_X = 0;
    private static final int EXTRA_PART_START_Y = 228;

    public static FormColorSelectMenu instance = null;

    static final Component EmptyText = Component.empty();
    private static final Component BoolBTN_ON = Component.translatable("text.cloth-config.boolean.value.true");
    private static final Component BoolBTN_OFF = Component.translatable("text.cloth-config.boolean.value.false");

    // Label
    private static final Component FormSlotTitle = Component.translatable("gui.shape_shifter_curse_fabric.fcs.form_slot_title");
    static final Component GlobalSlotTitle = Component.translatable("gui.shape_shifter_curse_fabric.fcs.global_slot_title");
    private static final Component FormDefaultSlotTitle = Component.translatable("gui.shape_shifter_curse_fabric.fcs.form_default_slot_title");
    private static final Component Title = Component.translatable("gui.shape_shifter_curse_fabric.fcs.title");

    static final Component ColorChannel_R = Component.translatable("gui.shape_shifter_curse_fabric.fcs.color_channel_r");
    static final Component ColorChannel_G = Component.translatable("gui.shape_shifter_curse_fabric.fcs.color_channel_g");
    static final Component ColorChannel_B = Component.translatable("gui.shape_shifter_curse_fabric.fcs.color_channel_b");
    static final Component ColorChannel_H = Component.translatable("gui.shape_shifter_curse_fabric.fcs.color_channel_h");
    static final Component ColorChannel_S = Component.translatable("gui.shape_shifter_curse_fabric.fcs.color_channel_s");
    static final Component ColorChannel_V = Component.translatable("gui.shape_shifter_curse_fabric.fcs.color_channel_v");

    static final Component IsEnableLayerLabel = Component.translatable("gui.shape_shifter_curse_fabric.fcs.is_enable_layer");
    static final Component ExitSliderButtonLabel = Component.translatable("gui.shape_shifter_curse_fabric.fcs.exit_slider_button");
    static final MutableComponent NoneFromNameLabel = Component.translatable("gui.shape_shifter_curse_fabric.fcs.none_from_name");

    // Button
    private static final Component DownloadFromServer = Component.translatable("gui.shape_shifter_curse_fabric.fcs.from_server");
    private static final Component UploadToServer = Component.translatable("gui.shape_shifter_curse_fabric.fcs.to_server");
    private static final Component DownloadFromClient = Component.translatable("gui.shape_shifter_curse_fabric.fcs.from_client");
    private static final Component UploadToClient = Component.translatable("gui.shape_shifter_curse_fabric.fcs.to_client");
    static final Component DownloadFromClipboard = Component.translatable("gui.shape_shifter_curse_fabric.fcs.from_clipboard");
    static final Component UploadToClipboard = Component.translatable("gui.shape_shifter_curse_fabric.fcs.to_clipboard");

    // Config Entry
    static final Component PrimaryColorLabel = Component.translatable("gui.shape_shifter_curse_fabric.fcs.primaryColor");
    static final Component AccentColor1Label = Component.translatable("gui.shape_shifter_curse_fabric.fcs.accentColor1Color");
    static final Component AccentColor2Label = Component.translatable("gui.shape_shifter_curse_fabric.fcs.accentColor2Color");
    static final Component EyeColorALabel = Component.translatable("gui.shape_shifter_curse_fabric.fcs.eyeColorA");
    static final Component EyeColorBLabel = Component.translatable("gui.shape_shifter_curse_fabric.fcs.eyeColorB");
    static final Component PrimaryGreyReverseLabel = Component.translatable("gui.shape_shifter_curse_fabric.fcs.primaryGreyReverse");
    static final Component Accent1GreyReverseLabel = Component.translatable("gui.shape_shifter_curse_fabric.fcs.accent1GreyReverse");
    static final Component Accent2GreyReverseLabel = Component.translatable("gui.shape_shifter_curse_fabric.fcs.accent2GreyReverse");
    static final Component KeepOriginalSkinLabel = Component.translatable("text.autoconfig.shape-shifter-curse-custom.option.keep_original_skin");
    static final Component IsEnableFormColorSystemLabel = Component.translatable("text.autoconfig.shape-shifter-curse-custom.option.enable_form_color");

    private boolean isScreenInit = false;
    private EditBox primaryColorEditBox = null;
    private EditBox accentColor1EditBox = null;
    private EditBox accentColor2EditBox = null;
    private EditBox eyeColorAEditBox = null;
    private EditBox eyeColorBEditBox = null;
    private Button primaryGreyReverseButton = null;
    private Button accent1GreyReverseButton = null;
    private Button accent2GreyReverseButton = null;
    private Button keepOriginalSkinButton = null;
    private Button isEnableFormColorSystemButton = null;

    private SimpleIntSliderWidget sliderR = null;
    private SimpleIntSliderWidget sliderG = null;
    private SimpleIntSliderWidget sliderB = null;
    private EditBox sliderREditBox = null;
    private EditBox sliderGEditBox = null;
    private EditBox sliderBEditBox = null;
    private SimpleIntSliderWidget sliderH = null;
    private SimpleIntSliderWidget sliderS = null;
    private SimpleIntSliderWidget sliderV = null;
    private EditBox sliderHEditBox = null;
    private EditBox sliderSEditBox = null;
    private EditBox sliderVEditBox = null;

    private Button formNameLabel = null;

    private Button isEnableLayerButton = null;

    private static final Minecraft minecraftClient = Minecraft.getInstance();

    private final HashMap<String, HashMap<FormTextureUtils.ColorSetting, Identifier>> colorSettingCacheMap = new HashMap<>();  // 防止内存泄漏
    private int modelID = -1;
    private static final String IdentifierNameSpace = MOD_ID;
    private static final String IdentifierPrefix = "dynamic_fcs_v1_";
    private static long nowColorSettingIndex = 0;  // 自增ID

    private int formIDIndex = -1;

    public void scrollFormID(int offset, boolean loop) {
        if (formIDIndex < 0) {
            formIDIndex = 0;
        }
        formIDIndex += offset;
        if (formIDIndex < 0) {
            formIDIndex = loop ? RegPlayerForms.playerForms.size() - 1 : 0;
        } else if (formIDIndex >= RegPlayerForms.playerForms.size()) {
            formIDIndex = loop ? 0 : RegPlayerForms.playerForms.size() - 1;
        }
        this.onFormChange(false, false);
    }

    public void reloadFormIDName() {
        IForm form = this.getFormNoCheckUnlock();
        boolean isUnlocked = ShapeShifterCurseFabricClient.formColorData.isUnlock(form.getFormID());
        if (ShapeShifterCurseFabric.clientConfig.disableUnlockCheckInFormColorSelectMenu) {
            isUnlocked = true;
        }
        Component message = NoneFromNameLabel;
        if (!RegPlayerForms.ORIGINAL_BEFORE_ENABLE.equals(form)) {
            message = form.getContentText(CodexData.ContentType.NAME);
        }
        if (!isUnlocked) {
            if (message instanceof MutableComponent text) {
                text.setStyle(message.getStyle().withColor(TextColor.fromRgb(0xFF0000)));
            } else {
                message = message.copy().setStyle(message.getStyle().withColor(TextColor.fromRgb(0xFF0000)));
            }
        }
        this.formNameLabel.setMessage(message);
    }

    public void reloadFormIDIndex() {
        if (minecraftClient.player != null) {
            boolean isFind = false;
            IForm form = FormUtils.getPlayerForm(minecraftClient.player);
            if (form != null) {
                int Index = 0;
                for (IForm playerFormBase : RegPlayerForms.playerForms.values()) {
                    if (Objects.equals(playerFormBase.getFormID(), form.getFormID())) {
                        formIDIndex = Index;
                        isFind = true;
                        break;
                    }
                    Index++;
                }
            }
            if (!isFind) {
                formIDIndex = -1;
            }
            return;
        }
        formIDIndex = -1;
    }


    private Identifier getNextDynamicFormID() {
        return Identifier.fromNamespaceAndPath(IdentifierNameSpace, IdentifierPrefix + nowColorSettingIndex++);
    }

    private void CleanColorSettingCache() {
        TextureManager textureManager = minecraftClient.getTextureManager();
        for (Identifier id : colorSettingCacheMap.values().stream().flatMap(map -> map.values().stream()).toList()) {
            textureManager.release(id);
        }
        colorSettingCacheMap.clear();
    }

    // 顺序是 ARGB
    private int primaryColor = 0x00FFFFFF;
    private int accentColor1Color = 0x00FFFFFF;
    private int accentColor2Color = 0x00FFFFFF;
    private int eyeColorA = 0x00FFFFFF;
    private int eyeColorB = 0x00FFFFFF;
    private boolean primaryGreyReverse = false;
    private boolean accent1GreyReverse = false;
    private boolean accent2GreyReverse = false;
    private boolean keepOriginalSkin = false;
    private boolean enableFormColorSystem = true;
    // 同步配置是真只会操作一次 就不加了

    private boolean isColorSettingDirty = true;
    private FormTextureUtils.ColorSetting colorSetting_ARGB = null;
    private FormTextureUtils.ColorSetting colorSetting_ABGR = null;

    private int tempSliderConfigIndex = -1;
    private int tempSliderR = 0;
    private int tempSliderG = 0;
    private int tempSliderB = 0;
    private int tempSliderAlpha = 0;

    private int tempSliderH = 0;
    private int tempSliderS = 0;
    private int tempSliderV = 0;

    private boolean isOpenSlider = false;
    private List<AbstractWidget> config_panel_01 = new ArrayList<>();  // 保存config输入框 label之类的 用于切换
    private List<AbstractWidget> config_panel_02 = new ArrayList<>();  // 保存 RGB条 一些按钮

    // 修改 tempSliderX 后调用
    public void updateSlider() {
        int Color = tempSliderAlpha << 24 | tempSliderR << 16 | tempSliderG << 8 | tempSliderB;
        switch (tempSliderConfigIndex) {
            case 0 -> primaryColor = Color;
            case 1 -> accentColor1Color = Color;
            case 2 -> accentColor2Color = Color;
            case 3 -> eyeColorA = Color;
            case 4 -> eyeColorB = Color;
        }
        isColorSettingDirty = true;
    }

    // HSV 更新时调用此函数
    public void updateSliderHSV() {
        this.isUpdateRGBFromHSV = true;
        int[] color = FormTextureUtils.hsvToRgb(tempSliderH, tempSliderS, tempSliderV);
        tempSliderR = color[0];
        tempSliderG = color[1];
        tempSliderB = color[2];
        sliderR.setIntValue(tempSliderR);
        sliderG.setIntValue(tempSliderG);
        sliderB.setIntValue(tempSliderB);
        this.isUpdateRGBFromHSV = false;
    }

    // RGB 更新时调用此函数
    public void updateSliderRGB() {
        this.isUpdateHSVFromRGB = true;
        int[] color = FormTextureUtils.rgbToHsv(tempSliderR, tempSliderG, tempSliderB);
        tempSliderH = color[0];
        tempSliderS = color[1];
        tempSliderV = color[2];
        sliderH.setIntValue(tempSliderH);
        sliderS.setIntValue(tempSliderS);
        sliderV.setIntValue(tempSliderV);
        this.isUpdateHSVFromRGB = false;
    }

    // 非条修改颜色后调用
    public void reloadSlider() {
        int sliderColor = 0;
        switch (tempSliderConfigIndex) {
            case 0 -> sliderColor = primaryColor;
            case 1 -> sliderColor = accentColor1Color;
            case 2 -> sliderColor = accentColor2Color;
            case 3 -> sliderColor = eyeColorA;
            case 4 -> sliderColor = eyeColorB;
        }
        tempSliderR = (sliderColor >>> 16) & 0xFF;
        tempSliderG = (sliderColor >>> 8) & 0xFF;
        tempSliderB = sliderColor & 0xFF;
        tempSliderAlpha = (sliderColor >>> 24) & 0xFF;
        sliderR.setIntValue(tempSliderR);
        sliderG.setIntValue(tempSliderG);
        sliderB.setIntValue(tempSliderB);
        isEnableLayerButton.setMessage(tempSliderAlpha != 0 ? BoolBTN_ON : BoolBTN_OFF);
        this.updateSliderRGB();
    }

    public void updatePanel() {
        if (isOpenSlider) {
            config_panel_01.forEach(element -> element.visible = false);
            config_panel_02.forEach(element -> element.visible = true);
        } else {
            config_panel_01.forEach(element -> element.visible = true);
            config_panel_02.forEach(element -> element.visible = false);
        }
        this.updateUI();
    }

    public void loadData(FormTextureUtils.ColorSetting colorSetting) {
        primaryColor = colorSetting.getPrimaryColor();
        accentColor1Color = colorSetting.getAccentColor1();
        accentColor2Color = colorSetting.getAccentColor2();
        eyeColorA = colorSetting.getEyeColorA();
        eyeColorB = colorSetting.getEyeColorB();
        primaryGreyReverse = colorSetting.getPrimaryGreyReverse();
        accent1GreyReverse = colorSetting.getAccent1GreyReverse();
        accent2GreyReverse = colorSetting.getAccent2GreyReverse();
        isColorSettingDirty = true;
        this.updateUI();
    }

    public void loadServerData(FormTextureUtils.ColorSetting colorSetting) {
        // 服务器上的是 ABGR
        primaryColor = FormTextureUtils.ABGR2ARGB(colorSetting.getPrimaryColor());
        accentColor1Color = FormTextureUtils.ABGR2ARGB(colorSetting.getAccentColor1());
        accentColor2Color = FormTextureUtils.ABGR2ARGB(colorSetting.getAccentColor2());
        eyeColorA = FormTextureUtils.ABGR2ARGB(colorSetting.getEyeColorA());
        eyeColorB = FormTextureUtils.ABGR2ARGB(colorSetting.getEyeColorB());
        primaryGreyReverse = colorSetting.getPrimaryGreyReverse();
        accent1GreyReverse = colorSetting.getAccent1GreyReverse();
        accent2GreyReverse = colorSetting.getAccent2GreyReverse();
        isColorSettingDirty = true;
        this.updateUI();
    }

    // 仅用于最后保存 所以只会挂载到自动读取上 其他的可能由那6个按钮触发 loadData()只会在重载时触发
    private boolean lastLoadDataIsServerSide = false;

    public void loadData() {
        if (minecraftClient.player != null) {
            loadData(true);
            lastLoadDataIsServerSide = true;
        } else {
            loadData(false);
            lastLoadDataIsServerSide = false;
        }
    }

    public void loadData(boolean serverSide) {
        if (serverSide) {
            if (minecraftClient.player != null) {
                PlayerSkinComponent component = RegPlayerSkinComponent.SKIN_SETTINGS.get(minecraftClient.player);
                FormTextureUtils.ColorSetting colorSetting = component.getFormColor();
                this.keepOriginalSkin = component.shouldKeepOriginalSkin();
                this.enableFormColorSystem = component.isEnableFormColor();
                this.loadServerData(colorSetting);
            }
        } else {
            primaryColor = ShapeShifterCurseFabric.playerCustomConfig.primaryColor;
            accentColor1Color = ShapeShifterCurseFabric.playerCustomConfig.accentColor1Color;
            accentColor2Color = ShapeShifterCurseFabric.playerCustomConfig.accentColor2Color;
            eyeColorA = ShapeShifterCurseFabric.playerCustomConfig.eyeColorA;
            eyeColorB = ShapeShifterCurseFabric.playerCustomConfig.eyeColorB;
            primaryGreyReverse = ShapeShifterCurseFabric.playerCustomConfig.primaryGreyReverse;
            accent1GreyReverse = ShapeShifterCurseFabric.playerCustomConfig.accent1GreyReverse;
            accent2GreyReverse = ShapeShifterCurseFabric.playerCustomConfig.accent2GreyReverse;
            this.keepOriginalSkin = ShapeShifterCurseFabric.playerCustomConfig.keep_original_skin;
            this.enableFormColorSystem = ShapeShifterCurseFabric.playerCustomConfig.enable_form_color;
            this.updateUI();
        }
        isColorSettingDirty = true;
    }

    public void saveDataToClient(boolean savaColorData, boolean saveExtraData) {
        if (savaColorData) {
            ShapeShifterCurseFabric.playerCustomConfig.primaryColor = primaryColor;
            ShapeShifterCurseFabric.playerCustomConfig.accentColor1Color = accentColor1Color;
            ShapeShifterCurseFabric.playerCustomConfig.accentColor2Color = accentColor2Color;
            ShapeShifterCurseFabric.playerCustomConfig.eyeColorA = eyeColorA;
            ShapeShifterCurseFabric.playerCustomConfig.eyeColorB = eyeColorB;
            ShapeShifterCurseFabric.playerCustomConfig.primaryGreyReverse = primaryGreyReverse;
            ShapeShifterCurseFabric.playerCustomConfig.accent1GreyReverse = accent1GreyReverse;
            ShapeShifterCurseFabric.playerCustomConfig.accent2GreyReverse = accent2GreyReverse;
        }
        if (saveExtraData) {
            ShapeShifterCurseFabric.playerCustomConfig.keep_original_skin = keepOriginalSkin;
            ShapeShifterCurseFabric.playerCustomConfig.enable_form_color = enableFormColorSystem;
        }
        AutoConfig.getConfigHolder(PlayerCustomConfig.class).save();
    }

    public @NotNull FormTextureUtils.ColorSetting getColorSetting(boolean ABGR) {
        if (isColorSettingDirty) {
            colorSetting_ARGB = new FormTextureUtils.ColorSetting(
                    primaryColor,
                    accentColor1Color,
                    accentColor2Color,
                    eyeColorA,
                    eyeColorB,
                    primaryGreyReverse,
                    accent1GreyReverse,
                    accent2GreyReverse
            );
            colorSetting_ABGR = new FormTextureUtils.ColorSetting(
                    FormTextureUtils.ARGB2ABGR(primaryColor),
                    FormTextureUtils.ARGB2ABGR(accentColor1Color),
                    FormTextureUtils.ARGB2ABGR(accentColor2Color),
                    FormTextureUtils.ARGB2ABGR(eyeColorA),
                    FormTextureUtils.ARGB2ABGR(eyeColorB),
                    primaryGreyReverse,
                    accent1GreyReverse,
                    accent2GreyReverse
            );
            isColorSettingDirty = false;
        }
        return ABGR ? colorSetting_ABGR : colorSetting_ARGB;
    }

    private boolean isUsingTempTexture = true;
    private boolean isUsingCustomSkinConfigOverrider = true;
    private boolean isUsingTempModel = true;

    public FormColorSelectMenu(Component title) {
        super(title);
        this.reloadFormIDIndex();
        loadData();
        if (!FormTextureUtils.useTempFormTexture) {
            FormTextureUtils.useTempFormTexture = true;
            FormTextureUtils.tempFormTextureProcessor = this;
        } else {
            ShapeShifterCurseFabric.LOGGER.warn("Temp Texture System is already in use, dynamic texture rendering will not work");
            isUsingTempTexture = false;
        }
        if (!FormTextureUtils.useTempCustomSkinConfig) {
            FormTextureUtils.useTempCustomSkinConfig = true;
            FormTextureUtils.tempCustomSkinConfigOverrider = this;
        } else {
            ShapeShifterCurseFabric.LOGGER.warn("Temp Custom Skin Config System is already in use, dynamic custom skin config will not work");
            isUsingCustomSkinConfigOverrider = false;
        }
        if (!FormTextureUtils.useTempFormModel) {
            FormTextureUtils.useTempFormModel = true;
            FormTextureUtils.tempFormModelProcessor = this;
        } else {
            ShapeShifterCurseFabric.LOGGER.warn("Temp Form Model System is already in use, dynamic form rendering will not work");
            isUsingTempModel = false;
        }
        if (instance != null) {
            ShapeShifterCurseFabric.LOGGER.error("FormColorSelectMenu is already in use, only one instance is allowed");
        }
        instance = this;
    }

    private Screen parsetScreen = null;

    public FormColorSelectMenu(Component title, Screen parsetScreen) {
        this(title);
        this.parsetScreen = parsetScreen;
    }

    public void renderTextureBackground(GuiGraphics context) {
        int BG_X = width / 2 - BG_WIDTH / 2;
        int BG_Y = height / 2 - BG_HEIGHT / 2;
        context.blit(texture, BG_X, BG_Y, 0, 0, BG_WIDTH, BG_HEIGHT, BG_IMAGE_WIDTH, BG_IMAGE_HEIGHT);
        if (!isOpenSlider) {
            // 133,20,184,181,0,0
            this.drawExtraPart(context, BG_X + 133, BG_Y + 20, 0, 0, 184, 181);
        } else {
            // 133,20,184,181,184,0
            this.drawExtraPart(context, BG_X + 133, BG_Y + 20, 184, 0, 184, 181);
        }
    }

    public int colorChannel2Int(String channel) {
        return colorChannel2Int(channel, 0, 255);
    }

    public int colorChannel2Int(String channel, int min, int max) {
        try {
            int value = Integer.parseInt(channel);
            return Math.min(Math.max(value, min), max);
        } catch (Exception ignored) {
            return min;
        }
    }

    public int decodeColor(String Color) {
        Integer color = null;
        try {
            if (Color.startsWith("#")) {
                color = Integer.parseUnsignedInt(Color.substring(1), 16);
            } else {
                color = Integer.parseUnsignedInt(Color, 10);
            }
        } catch (Exception ignored) {
        }
        if (color == null) {
            return 0x00FFFFFF;
        }
        return color;
    }

    public String encodeColor(int Color) {
        return String.format(Locale.ROOT, "#%08X", Color);
    }

    private boolean isUpdateUI = false;
    private int isUpdateSlider = 0;  // 用于防止 EditBox修改Slider
    private boolean isUpdateHSVFromRGB = false;
    private boolean isUpdateRGBFromHSV = false;

    public void onConfigChanged() {
        if (!this.isScreenInit || isUpdateUI) {
            return;
        }
        this.primaryColor = decodeColor(this.primaryColorEditBox.getValue());
        this.accentColor1Color = decodeColor(this.accentColor1EditBox.getValue());
        this.accentColor2Color = decodeColor(this.accentColor2EditBox.getValue());
        this.eyeColorA = decodeColor(this.eyeColorAEditBox.getValue());
        this.eyeColorB = decodeColor(this.eyeColorBEditBox.getValue());
        this.primaryGreyReverse = primaryGreyReverseButton.getMessage().equals(BoolBTN_ON);
        this.accent1GreyReverse = accent1GreyReverseButton.getMessage().equals(BoolBTN_ON);
        this.accent2GreyReverse = accent2GreyReverseButton.getMessage().equals(BoolBTN_ON);
        this.keepOriginalSkin = keepOriginalSkinButton.getMessage().equals(BoolBTN_ON);
        this.enableFormColorSystem = isEnableFormColorSystemButton.getMessage().equals(BoolBTN_ON);
        this.isColorSettingDirty = true;
    }

    public void updateUI() {
        if (!this.isScreenInit) {
            return;
        }
        this.isUpdateUI = true;
        this.primaryColorEditBox.setValue(encodeColor(this.primaryColor));
        this.accentColor1EditBox.setValue(encodeColor(this.accentColor1Color));
        this.accentColor2EditBox.setValue(encodeColor(this.accentColor2Color));
        this.eyeColorAEditBox.setValue(encodeColor(this.eyeColorA));
        this.eyeColorBEditBox.setValue(encodeColor(this.eyeColorB));
        this.primaryGreyReverseButton.setMessage(this.primaryGreyReverse ? BoolBTN_ON : BoolBTN_OFF);
        this.accent1GreyReverseButton.setMessage(this.accent1GreyReverse ? BoolBTN_ON : BoolBTN_OFF);
        this.accent2GreyReverseButton.setMessage(this.accent2GreyReverse ? BoolBTN_ON : BoolBTN_OFF);
        this.keepOriginalSkinButton.setMessage(this.keepOriginalSkin ? BoolBTN_ON : BoolBTN_OFF);
        this.isEnableFormColorSystemButton.setMessage(this.enableFormColorSystem ? BoolBTN_ON : BoolBTN_OFF);
        this.reloadSlider();
        this.reloadAllSlotName();
        this.isUpdateUI = false;
    }

    public void onFormChange(boolean force, boolean reloadColorData) {
        if (!this.isScreenInit) {
            return;
        }
        if (force) {
            this.reloadFormIDIndex();
        }
        if (reloadColorData) {
            this.loadData();
        } else {
            this.updateUI();
        }
        this.reloadFormIDName();
    }

    public static void onFormChange_STATIC(boolean force, boolean reloadColorData) {
        if (instance != null) {
            instance.onFormChange(force, reloadColorData);
        }
    }

    @Override
    public void init() {
        // 格式:
        // X,Y,Width,Height - WidgetDesc
        // 推荐使用AI自动补全修改 先注释掉原来的代码 重新写那个位置大小注释 AI大概率能正确填充

        super.init();
        int BPosX = width / 2 - BG_WIDTH / 2;  // 图片左上角 X
        int BPosY = height / 2 - BG_HEIGHT / 2;  // 图片左上角 Y
        // Label
        // 20,146,80,9 - 形态3槽
        this.addRenderableWidget(new StringWidget(BPosX + 20, BPosY + 146, 80, 9, FormSlotTitle, font).setColor(0xDDDDDD));
        // 135,5,180,9 - Title
        this.addRenderableWidget(new StringWidget(BPosX + 135, BPosY + 5, 180, 9, Title, font).setColor(0xDDDDDD));
        // 320,5,80,9 - 全局9槽
        this.addRenderableWidget(new StringWidget(BPosX + 320, BPosY + 5, 80, 9, GlobalSlotTitle, font).setColor(0xDDDDDD));
        // 320,182,80,9 - 形态默认槽
        this.addRenderableWidget(new StringWidget(BPosX + 320, BPosY + 182, 80, 9, FormDefaultSlotTitle, font).setColor(0xDDDDDD));
        // Normal Button
        // 85,5,45,15 - 获取服务器数据
        this.addRenderableWidget(Button.builder(DownloadFromServer, button -> {
            loadData(true);
        }).pos(BPosX + 85, BPosY + 5).size(45, 15).build()
        );
        // 85,23,45,15 - 发送到服务器
        this.addRenderableWidget(Button.builder(UploadToServer, button -> {
            ModPacketsS2C.sendUpdateCustomColor(this.getColorSetting(false), false, true, this.keepOriginalSkin, this.enableFormColorSystem);
        }).pos(BPosX + 85, BPosY + 23).size(45, 15).build()
        );
        // 85,41,45,15 - 获取客户端数据(配置)
        this.addRenderableWidget(Button.builder(DownloadFromClient, button -> {
            loadData(false);
        }).pos(BPosX + 85, BPosY + 41).size(45, 15).build()
        );
        // 85,59,45,15 - 发送到客户端(配置)
        this.addRenderableWidget(Button.builder(UploadToClient, button -> {
            this.saveDataToClient(true, true);
        }).pos(BPosX + 85, BPosY + 59).size(45, 15).build()
        );
        // 85,77,45,15 - 从剪切板获取
        this.addRenderableWidget(Button.builder(DownloadFromClipboard, button -> {
            String keyBoardData = minecraftClient.keyboardHandler.getClipboard();
            FormTextureUtils.ColorSetting cs = FormColorData.ColorSettingFormString(keyBoardData);
            if (cs != null) {
                this.loadData(cs);
            }
        }).pos(BPosX + 85, BPosY + 77).size(45, 15).build()
        );
        // 85,95,45,15 - 发送到剪切板
        this.addRenderableWidget(Button.builder(UploadToClipboard, button -> {
            String keyBoardData = FormColorData.ColorSettingtoString(this.getColorSetting(false), true);
            if (keyBoardData == null) {
                return;
            }
            minecraftClient.keyboardHandler.setClipboard(keyBoardData);
        }).pos(BPosX + 85, BPosY + 95).size(45, 15).build()
        );
        // Player Form Model Switch
        // 35,128,80,15 Form Name Button
        Button formScrollButton = Button.builder(NoneFromNameLabel, button -> {
            this.reloadFormIDIndex();
            this.onFormChange(false, false);
        }).pos(BPosX + 35, BPosY + 128).size(80, 15).build();
        this.addRenderableWidget(formScrollButton);
        this.formNameLabel = formScrollButton;
        // 20,128,15,15 Form Scroll Left Button
        this.addRenderableWidget(Button.builder(Component.literal("<"), button -> {
            this.scrollFormID(-1, true);
        }).pos(BPosX + 20, BPosY + 128).size(15, 15).build());
        // 115,128,15,15 Form Scroll Right Button
        this.addRenderableWidget(Button.builder(Component.literal(">"), button -> {
            this.scrollFormID(1, true);
        }).pos(BPosX + 115, BPosY + 128).size(15, 15).build());
        this.reloadFormIDName();
        // Config Pair
        // 139,27,75,11 - PrimaryColor Label
        StringWidget primaryColorLabel = new StringWidget(BPosX + 139, BPosY + 27, 75, 11, PrimaryColorLabel, font).setColor(0xDDDDDD);
        this.addRenderableWidget(primaryColorLabel);
        this.config_panel_01.add(primaryColorLabel);
        // 241,27,70,11 - PrimaryColor Input
        EditBox primaryColorInput = new EditBox(this.font, BPosX + 241, BPosY + 27, 70, 11, null, EmptyText);
        primaryColorInput.setMaxLength(9);
        primaryColorInput.setResponder((text) -> {
            this.onConfigChanged();
        });
        this.addRenderableWidget(primaryColorInput);
        this.config_panel_01.add(primaryColorInput);
        this.primaryColorEditBox = primaryColorInput;
        // 139,41,75,11 - AccentColor1 Label
        StringWidget accentColor1Label = new StringWidget(BPosX + 139, BPosY + 41, 75, 11, AccentColor1Label, font).setColor(0xDDDDDD);
        this.addRenderableWidget(accentColor1Label);
        this.config_panel_01.add(accentColor1Label);
        // 241,41,70,11 - AccentColor1 Input
        EditBox accentColor1Input = new EditBox(this.font, BPosX + 241, BPosY + 41, 70, 11, null, EmptyText);
        accentColor1Input.setMaxLength(9);
        accentColor1Input.setResponder((text) -> {
            this.onConfigChanged();
        });
        this.addRenderableWidget(accentColor1Input);
        this.config_panel_01.add(accentColor1Input);
        this.accentColor1EditBox = accentColor1Input;
        // 139,55,75,11 - AccentColor2 Label
        StringWidget accentColor2Label = new StringWidget(BPosX + 139, BPosY + 55, 75, 11, AccentColor2Label, font).setColor(0xDDDDDD);
        this.addRenderableWidget(accentColor2Label);
        this.config_panel_01.add(accentColor2Label);
        // 241,55,70,11 - AccentColor2 Input
        EditBox accentColor2Input = new EditBox(this.font, BPosX + 241, BPosY + 55, 70, 11, null, EmptyText);
        accentColor2Input.setMaxLength(9);
        accentColor2Input.setResponder((text) -> {
            this.onConfigChanged();
        });
        this.addRenderableWidget(accentColor2Input);
        this.config_panel_01.add(accentColor2Input);
        this.accentColor2EditBox = accentColor2Input;
        // 139,69,75,11 - EyeColorA Label
        StringWidget eyeColorALabel = new StringWidget(BPosX + 139, BPosY + 69, 75, 11, EyeColorALabel, font).setColor(0xDDDDDD);
        this.addRenderableWidget(eyeColorALabel);
        this.config_panel_01.add(eyeColorALabel);
        // 241,69,70,11 - EyeColorA Input
        EditBox eyeColorAInput = new EditBox(this.font, BPosX + 241, BPosY + 69, 70, 11, null, EmptyText);
        eyeColorAInput.setMaxLength(9);
        eyeColorAInput.setResponder((text) -> {
            this.onConfigChanged();
        });
        this.addRenderableWidget(eyeColorAInput);
        this.config_panel_01.add(eyeColorAInput);
        this.eyeColorAEditBox = eyeColorAInput;
        // 139,83,75,11 - EyeColorB Label
        StringWidget eyeColorBLabel = new StringWidget(BPosX + 139, BPosY + 83, 75, 11, EyeColorBLabel, font).setColor(0xDDDDDD);
        this.addRenderableWidget(eyeColorBLabel);
        this.config_panel_01.add(eyeColorBLabel);
        // 241,83,70,11 - EyeColorB Input
        EditBox eyeColorBInput = new EditBox(this.font, BPosX + 241, BPosY + 83, 70, 11, null, EmptyText);
        eyeColorBInput.setMaxLength(9);
        eyeColorBInput.setResponder((text) -> {
            this.onConfigChanged();
        });
        this.addRenderableWidget(eyeColorBInput);
        this.config_panel_01.add(eyeColorBInput);
        this.eyeColorBEditBox = eyeColorBInput;
        // 139,97,139,11 - PrimaryGreyReverse Label
        StringWidget primaryGreyReverseLabel = new StringWidget(BPosX + 139, BPosY + 97, 139, 11, PrimaryGreyReverseLabel, font).setColor(0xDDDDDD);
        this.addRenderableWidget(primaryGreyReverseLabel);
        this.config_panel_01.add(primaryGreyReverseLabel);
        // 281,97,30,11 - PrimaryGreyReverse Button
        Button primaryGreyReverseButton = Button.builder(this.primaryGreyReverse ? BoolBTN_ON :BoolBTN_OFF, (button) -> {
            this.primaryGreyReverse = !this.primaryGreyReverse;
            if (this.primaryGreyReverse) {
                button.setMessage(BoolBTN_ON);
            } else {
                button.setMessage(BoolBTN_OFF);
            }
            this.isColorSettingDirty = true;
        }).pos(BPosX + 281, BPosY + 97).size(30, 11).build();
        this.addRenderableWidget(primaryGreyReverseButton);
        this.config_panel_01.add(primaryGreyReverseButton);
        this.primaryGreyReverseButton = primaryGreyReverseButton;
        // 139,111,139,11 - Accent1GreyReverse Label
        StringWidget accent1GreyReverseLabel = new StringWidget(BPosX + 139, BPosY + 111, 139, 11, Accent1GreyReverseLabel, font).setColor(0xDDDDDD);
        this.addRenderableWidget(accent1GreyReverseLabel);
        this.config_panel_01.add(accent1GreyReverseLabel);
        // 281,111,30,11 - Accent1GreyReverse Button
        Button accent1GreyReverseButton = Button.builder(this.accent1GreyReverse ? BoolBTN_ON :BoolBTN_OFF, (button) -> {
            this.accent1GreyReverse = !this.accent1GreyReverse;
            if (this.accent1GreyReverse) {
                button.setMessage(BoolBTN_ON);
            } else {
                button.setMessage(BoolBTN_OFF);
            }
            this.isColorSettingDirty = true;
        }).pos(BPosX + 281, BPosY + 111).size(30, 11).build();
        this.addRenderableWidget(accent1GreyReverseButton);
        this.config_panel_01.add(accent1GreyReverseButton);
        this.accent1GreyReverseButton = accent1GreyReverseButton;
        // 139,125,139,11 - Accent2GreyReverse Label
        StringWidget accent2GreyReverseLabel = new StringWidget(BPosX + 139, BPosY + 125, 139, 11, Accent2GreyReverseLabel, font).setColor(0xDDDDDD);
        this.addRenderableWidget(accent2GreyReverseLabel);
        this.config_panel_01.add(accent2GreyReverseLabel);
        // 281,125,30,11 - Accent2GreyReverse Button
        Button accent2GreyReverseButton = Button.builder(this.accent2GreyReverse ? BoolBTN_ON :BoolBTN_OFF, (button) -> {
            this.accent2GreyReverse = !this.accent2GreyReverse;
            if (this.accent2GreyReverse) {
                button.setMessage(BoolBTN_ON);
            } else {
                button.setMessage(BoolBTN_OFF);
            }
            this.isColorSettingDirty = true;
        }).pos(BPosX + 281, BPosY + 125).size(30, 11).build();
        this.addRenderableWidget(accent2GreyReverseButton);
        this.config_panel_01.add(accent2GreyReverseButton);
        this.accent2GreyReverseButton = accent2GreyReverseButton;
        // 139,153,139,11 - Keep Original Skin Label
        StringWidget keepOriginalSkinLabel = new StringWidget(BPosX + 139, BPosY + 153, 139, 11, KeepOriginalSkinLabel, font).setColor(0xDDDDDD);
        this.addRenderableWidget(keepOriginalSkinLabel);
        this.config_panel_01.add(keepOriginalSkinLabel);
        // 281,153,30,11 - Keep Original Skin Button
        Button keepOriginalSkinButton = Button.builder(this.keepOriginalSkin ? BoolBTN_ON :BoolBTN_OFF, (button) -> {
            this.keepOriginalSkin = !this.keepOriginalSkin;
            if (this.keepOriginalSkin) {
                button.setMessage(BoolBTN_ON);
            } else {
                button.setMessage(BoolBTN_OFF);
            }
        }).pos(BPosX + 281, BPosY + 153).size(30, 11).build();
        this.addRenderableWidget(keepOriginalSkinButton);
        this.config_panel_01.add(keepOriginalSkinButton);
        this.keepOriginalSkinButton = keepOriginalSkinButton;
        // 139,167,139,11 - Is Enable Form Color System Label
        StringWidget isEnableFormColorSystemLabel = new StringWidget(BPosX + 139, BPosY + 167, 139, 11, IsEnableFormColorSystemLabel, font).setColor(0xDDDDDD);
        this.addRenderableWidget(isEnableFormColorSystemLabel);
        this.config_panel_01.add(isEnableFormColorSystemLabel);
        // 281,167,30,11 - Is Enable Form Color System Button
        Button isEnableFormColorSystemButton = Button.builder(this.enableFormColorSystem ? BoolBTN_ON :BoolBTN_OFF, (button) -> {
            this.enableFormColorSystem = !this.enableFormColorSystem;
            if (this.enableFormColorSystem) {
                button.setMessage(BoolBTN_ON);
            } else {
                button.setMessage(BoolBTN_OFF);
            }
        }).pos(BPosX + 281, BPosY + 167).size(30, 11).build();
        this.addRenderableWidget(isEnableFormColorSystemButton);
        this.config_panel_01.add(isEnableFormColorSystemButton);
        this.isEnableFormColorSystemButton = isEnableFormColorSystemButton;
        // 139,27,25,11 - R Label
        StringWidget rLabel = new StringWidget(BPosX + 139, BPosY + 27, 25, 11, ColorChannel_R, font);
        this.addRenderableWidget(rLabel);
        this.config_panel_02.add(rLabel);
        // 139,41,25,11 - G Label
        StringWidget gLabel = new StringWidget(BPosX + 139, BPosY + 41, 25, 11, ColorChannel_G, font);
        this.addRenderableWidget(gLabel);
        this.config_panel_02.add(gLabel);
        // 139,55,25,11 - B Label
        StringWidget bLabel = new StringWidget(BPosX + 139, BPosY + 55, 25, 11, ColorChannel_B, font);
        this.addRenderableWidget(bLabel);
        this.config_panel_02.add(bLabel);
        // 177,27,30,11 - R Input
        EditBox sliderREditBox = new EditBox(font, BPosX + 177, BPosY + 27, 30, 11, EmptyText);
        sliderREditBox.setMaxLength(3);
        sliderREditBox.setResponder((text) -> {
            this.tempSliderR = this.colorChannel2Int(text);
            if (isUpdateSlider == 0) {
                this.sliderR.setIntValue(this.tempSliderR);
            } else {
                this.updateSlider();
                if (!isUpdateRGBFromHSV) {
                    this.updateSliderRGB();
                }
            }
        });
        this.addRenderableWidget(sliderREditBox);
        this.config_panel_02.add(sliderREditBox);
        this.sliderREditBox = sliderREditBox;
        // 177,41,30,11 - G Input
        EditBox sliderGEditBox = new EditBox(font, BPosX + 177, BPosY + 41, 30, 11, EmptyText);
        sliderGEditBox.setMaxLength(3);
        sliderGEditBox.setResponder((text) -> {
            this.tempSliderG = this.colorChannel2Int(text);
            if (isUpdateSlider == 0) {
                this.sliderG.setIntValue(this.tempSliderG);
            } else {
                this.updateSlider();
                if (!isUpdateRGBFromHSV) {
                    this.updateSliderRGB();
                }
            }
        });
        this.addRenderableWidget(sliderGEditBox);
        this.config_panel_02.add(sliderGEditBox);
        this.sliderGEditBox = sliderGEditBox;
        // 177,55,30,11 - B Input
        EditBox sliderBEditBox = new EditBox(font, BPosX + 177, BPosY + 55, 30, 11, EmptyText);
        sliderBEditBox.setMaxLength(3);
        sliderBEditBox.setResponder((text) -> {
            this.tempSliderB = this.colorChannel2Int(text);
            if (isUpdateSlider == 0) {
                this.sliderB.setIntValue(this.tempSliderB);
            } else {
                this.updateSlider();
                if (!isUpdateRGBFromHSV) {
                    this.updateSliderRGB();
                }
            }
        });
        this.addRenderableWidget(sliderBEditBox);
        this.config_panel_02.add(sliderBEditBox);
        this.sliderBEditBox = sliderBEditBox;
        // Slider的改动直接改sliderXEditBox就行 不用updateSlider
        // 211,27,100,11 - R Slider
        SimpleIntSliderWidget sliderR = new SimpleIntSliderWidget(BPosX + 211, BPosY + 27, 100, 11, EmptyText, 0d, 0, 255);
        sliderR.onChanged = (widget) -> {
            this.isUpdateSlider++;
            this.sliderREditBox.setValue(String.valueOf(widget.getIntValue()));
            this.isUpdateSlider--;
        };
        this.addRenderableWidget(sliderR);
        this.config_panel_02.add(sliderR);
        this.sliderR = sliderR;
        // 211,41,100,11 - G Slider
        SimpleIntSliderWidget sliderG = new SimpleIntSliderWidget(BPosX + 211, BPosY + 41, 100, 11, EmptyText, 0d, 0, 255);
        sliderG.onChanged = (widget) -> {
            this.isUpdateSlider++;
            this.sliderGEditBox.setValue(String.valueOf(widget.getIntValue()));
            this.isUpdateSlider--;
        };
        this.addRenderableWidget(sliderG);
        this.config_panel_02.add(sliderG);
        this.sliderG = sliderG;
        // 211,55,100,11 - B Slider
        SimpleIntSliderWidget sliderB = new SimpleIntSliderWidget(BPosX + 211, BPosY + 55, 100, 11, EmptyText, 0d, 0, 255);
        sliderB.onChanged = (widget) -> {
            this.isUpdateSlider++;
            this.sliderBEditBox.setValue(String.valueOf(widget.getIntValue()));
            this.isUpdateSlider--;
        };
        this.addRenderableWidget(sliderB);
        this.config_panel_02.add(sliderB);
        this.sliderB = sliderB;
        // 139,69,25,11 - H label
        StringWidget hLabel = new StringWidget(BPosX + 139, BPosY + 69, 25, 11, ColorChannel_H, font);
        this.addRenderableWidget(hLabel);
        this.config_panel_02.add(hLabel);
        // 139,83,25,11 - S label
        StringWidget sLabel = new StringWidget(BPosX + 139, BPosY + 83, 25, 11, ColorChannel_S, font);
        this.addRenderableWidget(sLabel);
        this.config_panel_02.add(sLabel);
        // 139,97,25,11 - V label
        StringWidget vLabel = new StringWidget(BPosX + 139, BPosY + 97, 25, 11, ColorChannel_V, font);
        this.addRenderableWidget(vLabel);
        this.config_panel_02.add(vLabel);
        // 177,69,30,11 - H Input
        EditBox sliderHEditBox = new EditBox(font, BPosX + 177, BPosY + 69, 30, 11, EmptyText);
        sliderHEditBox.setMaxLength(3);
        sliderHEditBox.setResponder((text) -> {
            this.tempSliderH = this.colorChannel2Int(text, 0, 359);
            if (isUpdateSlider == 0) {
                this.sliderH.setIntValue(this.tempSliderH);
            } else {
                if (!isUpdateHSVFromRGB) {
                    this.updateSliderHSV();
                }
            }
        });
        this.addRenderableWidget(sliderHEditBox);
        this.config_panel_02.add(sliderHEditBox);
        this.sliderHEditBox = sliderHEditBox;
        // 177,83,30,11 - S Input
        EditBox sliderSEditBox = new EditBox(font, BPosX + 177, BPosY + 83, 30, 11, EmptyText);
        sliderSEditBox.setMaxLength(3);
        sliderSEditBox.setResponder((text) -> {
            this.tempSliderS = this.colorChannel2Int(text, 0, 100);
            if (isUpdateSlider == 0) {
                this.sliderS.setIntValue(this.tempSliderS);
            } else {
                if (!isUpdateHSVFromRGB) {
                    this.updateSliderHSV();
                }
            }
        });
        this.addRenderableWidget(sliderSEditBox);
        this.config_panel_02.add(sliderSEditBox);
        this.sliderSEditBox = sliderSEditBox;
        // 177,97,30,11 - V Input
        EditBox sliderVEditBox = new EditBox(font, BPosX + 177, BPosY + 97, 30, 11, EmptyText);
        sliderVEditBox.setMaxLength(3);
        sliderVEditBox.setResponder((text) -> {
            this.tempSliderV = this.colorChannel2Int(text, 0, 100);
            if (isUpdateSlider == 0) {
                this.sliderV.setIntValue(this.tempSliderV);
            } else {
                if (!isUpdateHSVFromRGB) {
                    this.updateSliderHSV();
                }
            }
        });
        this.addRenderableWidget(sliderVEditBox);
        this.config_panel_02.add(sliderVEditBox);
        this.sliderVEditBox = sliderVEditBox;
        // 211,69,100,11 - H Slider
        SimpleIntSliderWidget sliderH = new SimpleIntSliderWidget(BPosX + 211, BPosY + 69, 100, 11, EmptyText, 0d, 0, 359);
        sliderH.onChanged = (widget) -> {
            this.isUpdateSlider++;
            this.sliderHEditBox.setValue(String.valueOf(widget.getIntValue()));
            this.isUpdateSlider--;
        };
        this.addRenderableWidget(sliderH);
        this.config_panel_02.add(sliderH);
        this.sliderH = sliderH;
        // 211,83,100,11 - S Slider
        SimpleIntSliderWidget sliderS = new SimpleIntSliderWidget(BPosX + 211, BPosY + 83, 100, 11, EmptyText, 0d, 0, 100);
        sliderS.onChanged = (widget) -> {
            this.isUpdateSlider++;
            this.sliderSEditBox.setValue(String.valueOf(widget.getIntValue()));
            this.isUpdateSlider--;
        };
        this.addRenderableWidget(sliderS);
        this.config_panel_02.add(sliderS);
        this.sliderS = sliderS;
        // 211,97,100,11 - V Slider
        SimpleIntSliderWidget sliderV = new SimpleIntSliderWidget(BPosX + 211, BPosY + 97, 100, 11, EmptyText, 0d, 0, 100);
        sliderV.onChanged = (widget) -> {
            this.isUpdateSlider++;
            this.sliderVEditBox.setValue(String.valueOf(widget.getIntValue()));
            this.isUpdateSlider--;
        };
        this.addRenderableWidget(sliderV);
        this.config_panel_02.add(sliderV);
        this.sliderV = sliderV;
        // 139,111,75,11 - Is Enable Layer Label
        StringWidget isEnableLayerLabel = new StringWidget(BPosX + 139, BPosY + 111, 75, 11, IsEnableLayerLabel, font).setColor(0xDDDDDD);
        this.addRenderableWidget(isEnableLayerLabel);
        this.config_panel_02.add(isEnableLayerLabel);
        // 228,111,36,11 - Is Enable Layer Button
        Button isEnableLayerButton = Button.builder(this.tempSliderAlpha != 0 ? BoolBTN_ON : BoolBTN_OFF, (button) -> {
            this.tempSliderAlpha = this.tempSliderAlpha == 0 ? 255 : 0;
            if (this.tempSliderAlpha != 0) {
                button.setMessage(BoolBTN_ON);
            }
            else {
                button.setMessage(BoolBTN_OFF);
            }
            this.updateSlider();
        }).pos(BPosX + 228, BPosY + 111).size(36, 11).build();
        this.addRenderableWidget(isEnableLayerButton);
        this.config_panel_02.add(isEnableLayerButton);
        this.isEnableLayerButton = isEnableLayerButton;
        // 281,111,30,11 - Exit Slider Button
        Button exitSliderButton = Button.builder(ExitSliderButtonLabel, (button) -> {
            this.updateSlider();
            this.isOpenSlider = false;
            this.updatePanel();
        }).pos(BPosX + 281, BPosY + 111).size(30, 11).build();
        this.addRenderableWidget(exitSliderButton);
        this.config_panel_02.add(exitSliderButton);

        this.formLocalSettingButtons.clear();
        this.formLocalSettingTextFields.clear();
        this.formDefaultSettingButton = null;
        this.formDefaultSettingTextField = null;
        this.globalSettingButtons.clear();
        this.globalSettingTextFields.clear();

        // 20,158,80,15 local_form_slot_1
        this.createSaveDataButtons(0, 0, BPosX + 20, BPosY + 158);
        // 20,176,80,15 local_form_slot_2
        this.createSaveDataButtons(0, 1, BPosX + 20, BPosY + 176);
        // 20,194,80,15 local_form_slot_3
        this.createSaveDataButtons(0, 2, BPosX + 20, BPosY + 194);

        // 320,17,80,15 global_form_slot_1
        this.createSaveDataButtons(1, 0, BPosX + 320, BPosY + 17);
        // 320,35,80,15 global_form_slot_2
        this.createSaveDataButtons(1, 1, BPosX + 320, BPosY + 35);
        // 320,53,80,15 global_form_slot_3
        this.createSaveDataButtons(1, 2, BPosX + 320, BPosY + 53);
        // 320,71,80,15 global_form_slot_4
        this.createSaveDataButtons(1, 3, BPosX + 320, BPosY + 71);
        // 320,89,80,15 global_form_slot_5
        this.createSaveDataButtons(1, 4, BPosX + 320, BPosY + 89);
        // 320,107,80,15 global_form_slot_6
        this.createSaveDataButtons(1, 5, BPosX + 320, BPosY + 107);
        // 320,125,80,15 global_form_slot_7
        this.createSaveDataButtons(1, 6, BPosX + 320, BPosY + 125);
        // 320,143,80,15 global_form_slot_8
        this.createSaveDataButtons(1, 7, BPosX + 320, BPosY + 143);
        // 320,161,80,15 global_form_slot_9
        this.createSaveDataButtons(1, 8, BPosX + 320, BPosY + 161);

        // 320,194,80,15 form_default_slot
        this.createSaveDataButtons(2, 0, BPosX + 320, BPosY + 194);

        this.isScreenInit = true;
        this.updatePanel();
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
        InventoryScreen.renderEntityInInventory(context, x, y, size, new org.joml.Vector3f(), quaternionf, quaternionf2, entity);
        entity.yBodyRot = h;
        entity.yBodyRotO = m;
        entity.setYRot(i);
        entity.setXRot(j);
        entity.yHeadRotO = k;
        entity.yHeadRot = l;
    }

    private static int timer = 0;

    private void drawExtraPart(GuiGraphics context, int x, int y, int PartX, int PartY, int Width, int Height) {
        int realX = PartX + EXTRA_PART_START_X;
        int realY = PartY + EXTRA_PART_START_Y;
        context.blit(texture, x, y, realX, realY, Width, Height, BG_IMAGE_WIDTH, BG_IMAGE_HEIGHT);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        int BPosX = width / 2 - BG_WIDTH / 2;
        int BPosY = height / 2 - BG_HEIGHT / 2;
        this.renderBackground(context, mouseX, mouseY, 1.0f);
        this.renderTextureBackground(context);
        if (!isOpenSlider) {
            // 228,27,11,11
            // context.fill(BPosX + 228, BPosY + 27, BPosX + 239, BPosY + 38, 0xFF000000);
            context.fill(BPosX + 228, BPosY + 27, BPosX + 239, BPosY + 38, this.primaryColor);
            // 228,41,11,11
            // context.fill(BPosX + 228, BPosY + 41, BPosX + 239, BPosY + 52, 0xFF000000);
            context.fill(BPosX + 228, BPosY + 41, BPosX + 239, BPosY + 52, this.accentColor1Color);
            // 228,55,11,11
            // context.fill(BPosX + 228, BPosY + 55, BPosX + 239, BPosY + 66, 0xFF000000);
            context.fill(BPosX + 228, BPosY + 55, BPosX + 239, BPosY + 66, this.accentColor2Color);
            // 228,69,11,11
            // context.fill(BPosX + 228, BPosY + 69, BPosX + 239, BPosY + 80, 0xFF000000);
            context.fill(BPosX + 228, BPosY + 69, BPosX + 239, BPosY + 80, this.eyeColorA);
            // 228,83,11,11
            // context.fill(BPosX + 228, BPosY + 83, BPosX + 239, BPosY + 94, 0xFF000000);
            context.fill(BPosX + 228, BPosY + 83, BPosX + 239, BPosY + 94, this.eyeColorB);
        } else {
            // 267,111,11,11
            // context.fill(BPosX + 267, BPosY + 111, BPosX + 278, BPosY + 122, 0xFF000000);
            context.fill(BPosX + 267, BPosY + 111, BPosX + 278, BPosY + 122, (this.tempSliderAlpha << 24) | (this.tempSliderR << 16) | (this.tempSliderG << 8) | (this.tempSliderB));
        }
        if (timer > 60) {
            this.updateSavaButtonActive();
        } else {
            timer++;
        }
        // 20,5,60,120
        if (minecraftClient.player != null) {
            RenderEntity(context, BPosX + 50, BPosY + 100, 30, BPosX + 50 - mouseX, BPosY + 100 - mouseY, minecraftClient.player);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public Identifier getTexture(int modelID, String category, Identifier texture, Identifier mask, boolean OnlyMultiply) {
        if (this.modelID != modelID) {
            this.modelID = modelID;
            CleanColorSettingCache();
        }
        // 可以关闭这个功能
        if (!this.enableFormColorSystem) {
            return texture;
        }
        return colorSettingCacheMap.computeIfAbsent(category, k -> new HashMap<>()).computeIfAbsent(this.getColorSetting(true), k -> {
            // 这种方法不会内存泄漏 但是得自己管理临时材质
            DynamicTexture nativeImageBackedTexture = FormTextureUtils.BakeTextureNoMemLeak(texture, mask, this.getColorSetting(true), OnlyMultiply);
            Identifier id = getNextDynamicFormID();
            minecraftClient.getTextureManager().register(id, nativeImageBackedTexture);
            return id;
        });
    }

    public void saveData() {
        ShapeShifterCurseFabricClient.formColorData.writeToConfig();
    }

    @Override
    public void onClose() {
        CleanColorSettingCache();
        if (isUsingTempTexture) {
            FormTextureUtils.useTempFormTexture = false;
            FormTextureUtils.tempFormTextureProcessor = null;
            isUsingTempTexture = false;
        }
        if (isUsingCustomSkinConfigOverrider) {
            FormTextureUtils.useTempCustomSkinConfig = false;
            FormTextureUtils.tempCustomSkinConfigOverrider = null;
            isUsingCustomSkinConfigOverrider = false;
        }
        if (isUsingTempModel) {
            FormTextureUtils.useTempFormModel = false;
            FormTextureUtils.tempFormModelProcessor = null;
            isUsingTempModel = false;
        }
        instance = null;
        try {
            this.saveDataToClient(ShapeShifterCurseFabric.playerCustomConfig.auto_sync_config && ShapeShifterCurseFabric.playerCustomConfig.auto_sync_color_config, ShapeShifterCurseFabric.playerCustomConfig.auto_sync_config);
            ModPacketsS2C.sendUpdateCustomColor(this.getColorSetting(false), false, true, this.keepOriginalSkin, this.enableFormColorSystem); // 如果没进游戏时会发送失败 懒得做判断了 加一个Try
        } catch (Exception ignored) {
        }
        if (!this.lastLoadDataIsServerSide) {
            this.saveDataToClient(true, true);
        }
        this.saveData();
        if (this.parsetScreen != null && this.minecraft != null) {
            this.minecraft.setScreen(this.parsetScreen);
        } else {
            super.onClose();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean result = super.mouseClicked(mouseX, mouseY, button);
        int BPosX = width / 2 - BG_WIDTH / 2;
        int BPosY = height / 2 - BG_HEIGHT / 2;
        if (!this.isOpenSlider && this.isScreenInit) {
            // 228,27,11,11
            if (mouseX > BPosX + 228 && mouseX < BPosX + 239 && mouseY > BPosY + 27 && mouseY < BPosY + 38) {
                this.tempSliderConfigIndex = 0;
                this.isOpenSlider = true;
                result = true;
                this.updatePanel();
            } else
            // 228,41,11,11
            if (mouseX > BPosX + 228 && mouseX < BPosX + 239 && mouseY > BPosY + 41 && mouseY < BPosY + 52) {
                this.tempSliderConfigIndex = 1;
                this.isOpenSlider = true;
                result = true;
                this.updatePanel();
            } else
            // 228,55,11,11
            if (mouseX > BPosX + 228 && mouseX < BPosX + 239 && mouseY > BPosY + 55 && mouseY < BPosY + 66) {
                this.tempSliderConfigIndex = 2;
                this.isOpenSlider = true;
                result = true;
                this.updatePanel();
            } else
            // 228,69,11,11
            if (mouseX > BPosX + 228 && mouseX < BPosX + 239 && mouseY > BPosY + 69 && mouseY < BPosY + 80) {
                this.tempSliderConfigIndex = 3;
                this.isOpenSlider = true;
                result = true;
                this.updatePanel();
            } else
            // 228,83,11,11
            if (mouseX > BPosX + 228 && mouseX < BPosX + 239 && mouseY > BPosY + 83 && mouseY < BPosY + 94) {
                this.tempSliderConfigIndex = 4;
                this.isOpenSlider = true;
                result = true;
                this.updatePanel();
            }
        }
        return result;
    }

    private @Nullable Identifier getPlayerForm() {
        IForm form = this.getForm();
        if (RegPlayerForms.ORIGINAL_BEFORE_ENABLE.isEquals(form)) {
            return null;
        }
        return form.getFormID();
    }

    private boolean isFormLocalSettingExists(int index) {
        String id = String.format("fcs_%s", index);
        Identifier formID = this.getPlayerForm();
        if (formID != null) {
            return ShapeShifterCurseFabricClient.formColorData.customSettingByForm.getOrDefault(formID, new HashMap<>()).containsKey(id);
        }
        return false;
    }

    private @Nullable FormTextureUtils.ColorSetting getFormLocalSetting(int index) {
        String id = String.format("fcs_%s", index);
        Identifier formID = this.getPlayerForm();
        if (formID != null) {
            return ShapeShifterCurseFabricClient.formColorData.customSettingByForm.getOrDefault(formID, new HashMap<>()).get(id);
        }
        return null;
    }

    private void setFormLocalSetting(int index) {
        String id = String.format("fcs_%s", index);
        Identifier formID = this.getPlayerForm();
        if (formID != null) {
            FormTextureUtils.ColorSetting colorSettingRGBA = this.getColorSetting(false);
            ShapeShifterCurseFabricClient.formColorData.customSettingByForm.computeIfAbsent(formID, k -> new HashMap<>()).put(id, colorSettingRGBA);
        }
        this.updateSavaButtonActive();
    }

    private void removeFormLocalSetting(int index) {
        String id = String.format("fcs_%s", index);
        Identifier formID = this.getPlayerForm();
        if (formID != null) {
            ShapeShifterCurseFabricClient.formColorData.customSettingByForm.computeIfAbsent(formID, k -> new HashMap<>()).remove(id);
        }
        this.updateSavaButtonActive();
    }

    private boolean isGlobalSettingExists(int index) {
        String id = String.format("fcs_%s", index);
        return ShapeShifterCurseFabricClient.formColorData.customSetting.containsKey(id);
    }

    private @Nullable FormTextureUtils.ColorSetting getGlobalSetting(int index) {
        String id = String.format("fcs_%s", index);
        return ShapeShifterCurseFabricClient.formColorData.customSetting.get(id);
    }

    private void setGlobalSetting(int index) {
        String id = String.format("fcs_%s", index);
        FormTextureUtils.ColorSetting colorSettingRGBA = this.getColorSetting(false);
        ShapeShifterCurseFabricClient.formColorData.customSetting.put(id, colorSettingRGBA);
        this.updateSavaButtonActive();
    }

    private void removeGlobalSetting(int index) {
        String id = String.format("fcs_%s", index);
        ShapeShifterCurseFabricClient.formColorData.customSetting.remove(id);
        this.updateSavaButtonActive();
    }

    private boolean isFormDefaultSettingExists() {
        Identifier formID = this.getPlayerForm();
        if (formID != null) {
            return ShapeShifterCurseFabricClient.formColorData.formDefaultSetting.containsKey(formID);
        }
        return false;
    }

    private @Nullable FormTextureUtils.ColorSetting getFormDefaultSetting() {
        Identifier formID = this.getPlayerForm();
        if (formID != null) {
            return ShapeShifterCurseFabricClient.formColorData.formDefaultSetting.get(formID);
        }
        return null;
    }

    private void setFormDefaultSetting() {
        Identifier formID = this.getPlayerForm();
        if (formID != null) {
            FormTextureUtils.ColorSetting colorSettingRGBA = this.getColorSetting(false);
            ShapeShifterCurseFabricClient.formColorData.formDefaultSetting.put(formID, colorSettingRGBA);
        }
        this.updateSavaButtonActive();
    }

    private void removeFormDefaultSetting() {
        Identifier formID = this.getPlayerForm();
        if (formID != null) {
            ShapeShifterCurseFabricClient.formColorData.formDefaultSetting.remove(formID);
        }
        this.updateSavaButtonActive();
    }

    private final List<Tuple<FCS_ButtonWidget, FCS_ButtonWidget>> formLocalSettingButtons = new ArrayList<>();
    private final List<EditBox> formLocalSettingTextFields = new ArrayList<>();
    private Tuple<FCS_ButtonWidget, FCS_ButtonWidget> formDefaultSettingButton = null;
    private EditBox formDefaultSettingTextField = null;
    private final List<Tuple<FCS_ButtonWidget, FCS_ButtonWidget>> globalSettingButtons = new ArrayList<>();
    private final List<EditBox> globalSettingTextFields = new ArrayList<>();

    private void updateSavaButtonActive() {
        if (this.isScreenInit) {
            if (minecraftClient.player == null) {
                for (Tuple<FCS_ButtonWidget, FCS_ButtonWidget> buttonWidget : this.formLocalSettingButtons) {
                    FCS_ButtonWidget deleteButtonWidget = buttonWidget.getB();
                    deleteButtonWidget.active = false;
                    FCS_ButtonWidget updButtonWidget = buttonWidget.getA();
                    updButtonWidget.active = false;
                    updButtonWidget.TEXTURE_X = 0;
                }
                this.formDefaultSettingButton.getA().active = false;
                this.formDefaultSettingButton.getB().active = false;
                this.formDefaultSettingButton.getA().TEXTURE_X = 0;
            } else {
                for (int index = 0; index < formLocalSettingButtons.size(); index++) {
                    boolean dataExist = this.isFormLocalSettingExists(index);
                    Tuple<FCS_ButtonWidget, FCS_ButtonWidget> buttonWidget = formLocalSettingButtons.get(index);
                    FCS_ButtonWidget deleteButtonWidget = buttonWidget.getB();
                    deleteButtonWidget.active = dataExist;
                    FCS_ButtonWidget updButtonWidget = buttonWidget.getA();
                    updButtonWidget.active = true;
                    updButtonWidget.TEXTURE_X = dataExist ? 15 : 0;
                }
                boolean dataExist = this.isFormDefaultSettingExists();
                this.formDefaultSettingButton.getA().active = true;
                this.formDefaultSettingButton.getB().active = dataExist;
                this.formDefaultSettingButton.getA().TEXTURE_X = dataExist ? 15 : 0;
            }
            for (int index = 0; index < globalSettingButtons.size(); index++) {
                boolean dataExist = this.isGlobalSettingExists(index);
                Tuple<FCS_ButtonWidget, FCS_ButtonWidget> buttonWidget = globalSettingButtons.get(index);
                FCS_ButtonWidget deleteButtonWidget = buttonWidget.getB();
                deleteButtonWidget.active = dataExist;
                FCS_ButtonWidget updButtonWidget = buttonWidget.getA();
                updButtonWidget.active = true;
                updButtonWidget.TEXTURE_X = dataExist ? 15 : 0;
            }
        }
    }

    public void saveCustomColorData(int ButtonType, int Index) {
        if (ButtonType == 0) {
            this.setFormLocalSetting(Index);
        } else if (ButtonType == 1) {
            this.setGlobalSetting(Index);
        } else if (ButtonType == 2) {
            this.setFormDefaultSetting();
        }
        return;
    }

    private void deleteSaveData(int ButtonType, int Index) {
        if (ButtonType == 0) {
            this.removeFormLocalSetting(Index);
        } else if (ButtonType == 1) {
            this.removeGlobalSetting(Index);
        } else if (ButtonType == 2) {
            this.removeFormDefaultSetting();
        }
    }

    private void loadSaveData(int ButtonType, int Index) {
        @Nullable FormTextureUtils.ColorSetting colorSetting = null;
        if (ButtonType == 0) {
            colorSetting = getFormLocalSetting(Index);
        } else if (ButtonType == 1) {
            colorSetting = getGlobalSetting(Index);
        } else if (ButtonType == 2) {
            colorSetting = getFormDefaultSetting();
        }
        if (colorSetting != null) {
            this.loadData(colorSetting);
        }
    }

    private void saveSlotName(int ButtonType, int Index) {
        if (ButtonType == 0) {
            Identifier FormID = this.getPlayerForm();
            if (FormID == null) {
                return;
            }
            ShapeShifterCurseFabricClient.formColorData.setName_LocalFormSlot(FormID, Index, this.formLocalSettingTextFields.get(Index).getValue());
        } else if (ButtonType == 1) {
            ShapeShifterCurseFabricClient.formColorData.setName_GlobalSlot(Index, this.globalSettingTextFields.get(Index).getValue());
        } else if (ButtonType == 2) {
            Identifier FormID = this.getPlayerForm();
            if (FormID == null) {
                return;
            }
            ShapeShifterCurseFabricClient.formColorData.setName_DefaultSlot(FormID, this.formDefaultSettingTextField.getValue());
        }
    }

    private String getSlotName(int ButtonType, int Index) {
        if (ButtonType == 0) {
            Identifier FormID = this.getPlayerForm();
            if (FormID == null) {
                return "";
            }
            return ShapeShifterCurseFabricClient.formColorData.getName_LocalFormSlot(FormID, Index);
        } else if (ButtonType == 1) {
            return ShapeShifterCurseFabricClient.formColorData.getName_GlobalSlot(Index);
        } else if (ButtonType == 2) {
            Identifier FormID = this.getPlayerForm();
            if (FormID == null)
                return "";

            return ShapeShifterCurseFabricClient.formColorData.getName_DefaultSlot(FormID);
        }
        return "";
    }

    private void reloadAllSlotName() {
        for (int index = 0; index < globalSettingButtons.size(); index++) {
            this.globalSettingTextFields.get(index).setValue(this.getSlotName(1, index));
        }
        Identifier FormID = this.getPlayerForm();
        if (FormID != null) {
            this.formDefaultSettingTextField.setValue(this.getSlotName(2, 0));
            for (int index = 0; index < formLocalSettingButtons.size(); index++) {
                this.formLocalSettingTextFields.get(index).setValue(this.getSlotName(0, index));
            }
        }
    }

    private void createSaveDataButtons(int ButtonType, int Index, int X, int Y) {
        // X,Y,80,15
        // X+0,Y+0,15,15 upload/download Button
        FCS_ButtonWidget updButtonWidget = new FCS_ButtonWidget(X, Y, EmptyText, (button -> {
            if (button instanceof FCS_ButtonWidget fcsButtonWidget) {
                // 靠UI判断 省的写一个变量了
                if (fcsButtonWidget.TEXTURE_X == 15) {
                    this.loadSaveData(ButtonType, Index);
                } else if (fcsButtonWidget.TEXTURE_X == 0) {
                    this.saveCustomColorData(ButtonType, Index);
                }
            }
        }), (textSupplier) -> (MutableComponent)textSupplier.get(), 0);

        // X+15,Y+0,50,15 slot name input
        EditBox textFieldWidget = new EditBox(this.font, X + 15, Y, 50, 15, EmptyText);
        textFieldWidget.setResponder((text) -> {
            this.saveSlotName(ButtonType, Index);
        });

        // X+65,Y+0,15,15 delete Button
        FCS_ButtonWidget deleteButtonWidget = new FCS_ButtonWidget(X + 65, Y, EmptyText, (button -> {
            if (button instanceof FCS_ButtonWidget fcsButtonWidget) {
                if (fcsButtonWidget.TEXTURE_X == 30) {
                    this.deleteSaveData(ButtonType, Index);
                }
            }
        }), (textSupplier) -> (MutableComponent)textSupplier.get(), 30);
        switch (ButtonType) {
            case 0:
                formLocalSettingButtons.add(new Tuple<>(updButtonWidget, deleteButtonWidget));
                formLocalSettingTextFields.add(textFieldWidget);
                break;
            case 1:
                globalSettingButtons.add(new Tuple<>(updButtonWidget, deleteButtonWidget));
                globalSettingTextFields.add(textFieldWidget);
                break;
            case 2:
                formDefaultSettingButton = new Tuple<>(updButtonWidget, deleteButtonWidget);
                formDefaultSettingTextField = textFieldWidget;
                break;
        }
        this.addRenderableWidget(updButtonWidget);
        this.addRenderableWidget(textFieldWidget);
        this.addRenderableWidget(deleteButtonWidget);
    }

    public IForm getFormNoCheckUnlock() {
        if (this.formIDIndex < 0) {
            return RegPlayerForms.ORIGINAL_BEFORE_ENABLE;
        }
        Collection<IForm> playerFormsCollection = RegPlayerForms.playerForms.values();
        if (this.formIDIndex >= playerFormsCollection.size()) {
            return RegPlayerForms.ORIGINAL_BEFORE_ENABLE;
        }
        return playerFormsCollection.toArray(new IForm[0])[this.formIDIndex];
    }

    @Override
    public IForm getForm() {
        if (ShapeShifterCurseFabric.clientConfig.disableUnlockCheckInFormColorSelectMenu) {
            return this.getFormNoCheckUnlock();
        }
        IForm finalForm = null;
        if (this.formIDIndex < 0) {
            finalForm = RegPlayerForms.ORIGINAL_BEFORE_ENABLE;
        } else {
            Collection<IForm> playerFormsCollection = RegPlayerForms.playerForms.values();
            if (this.formIDIndex >= playerFormsCollection.size()) {
                finalForm = RegPlayerForms.ORIGINAL_BEFORE_ENABLE;
            } else {
                finalForm = playerFormsCollection.toArray(new IForm[0])[this.formIDIndex];
            }
        }
        if (finalForm == null || !ShapeShifterCurseFabricClient.formColorData.isUnlock(finalForm.getFormID())) {
            if (minecraftClient.player != null) {
                return FormUtils.getPlayerForm(minecraftClient.player);
            } else {
                return RegPlayerForms.ORIGINAL_BEFORE_ENABLE;
            }
        }
        return finalForm;
    }

    @Override
    public Identifier getLayerID() {
        return this.getForm().getFormLayer().getB();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keepOriginalSkin() {
        return this.keepOriginalSkin;
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // 自定义背景纹理完全遮挡，不需要暗色渐变
    }
}