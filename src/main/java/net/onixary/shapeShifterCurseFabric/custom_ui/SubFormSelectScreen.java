package net.onixary.shapeShifterCurseFabric.custom_ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.data.CodexData;
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsS2C;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.ISubForm;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;

import java.util.ArrayList;
import java.util.List;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

public class SubFormSelectScreen extends Screen {
    private static final int BG_WIDTH = 470;
    private static final int BG_HEIGHT = 247;
    private static final ResourceLocation BG_TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/normal_form_select_menu.png");

    private List<ResourceLocation> availableForms;
    private int nowPage = 0;
    private static final int pageSize = 16;
    private final List<ResourceLocation> buttonForms = new ArrayList<>();
    private final List<Button> buttonWidgetList = new ArrayList<>();

    public SubFormSelectScreen(Component title) {
        super(title);
    }

    private List<ResourceLocation> getAvailableForms() {
        List<ResourceLocation> availableForms = new ArrayList<>();
        IForm playerForm = FormUtils.getPlayerForm(this.minecraft.player);
        IForm nowForm = playerForm;
        if (playerForm instanceof ISubForm subForm && subForm.isSubForm()) {
            playerForm = subForm.getMasterForm();
        }
        if (playerForm == null) {
            return availableForms;
        }
        List<IForm> subForms = RegPlayerForms.getSubForms(playerForm);
        subForms.add(playerForm);
        subForms.removeIf(form -> form.isEquals(nowForm));
        subForms.removeIf(form -> !(FormUtils.isFormCanUse(this.minecraft.player, form)));
        availableForms.addAll(subForms.stream().map(IForm::getFormID).toList());
        return availableForms;
    }

    private void SendSetForm(ResourceLocation formID) {
        ModPacketsS2C.sendSetSubForm(formID);
    }

    private void LoadPage() {
        buttonForms.clear();
        for (int i = nowPage * pageSize; i < (nowPage + 1) * pageSize; i++) {
            if (i < availableForms.size()) {
                buttonForms.add(availableForms.get(i));
            }
            else {
                buttonForms.add(null);
            }
        }
        RefreshButtons();
    }

    private void RefreshButtons() {
        if (buttonForms.size() != buttonWidgetList.size()) {
            ShapeShifterCurseFabric.LOGGER.warn("ButtonForms.size() != buttonWidgetList.size()");
            return;
        }
        for (int i = 0; i < buttonForms.size(); i++) {
            Button buttonWidget = buttonWidgetList.get(i);
            if (buttonForms.get(i) != null) {
                try {
                    buttonWidget.setMessage(RegPlayerForms.getPlayerForm(buttonForms.get(i)).getContentText(CodexData.ContentType.NAME));
                } catch (Exception e) {
                    buttonWidget.setMessage(Component.nullToEmpty(buttonForms.get(i).toString()));
                }
                buttonWidget.visible = true;
            }
            else {
                buttonWidget.visible = false;
            }
        }
    }

    @Override
    public void init() {
        availableForms = getAvailableForms();
        // 或许可以对按钮进行排版
        // 修改数量请同时修改pageSize
        // 一列显示8个 共2列
        int ButtonWidth = 180;
        int ButtonHeight = 20;
        int ButtonStartX = width / 2 - (ButtonWidth + 10);
        int ButtonStartY = height / 2 - 4 * (ButtonHeight + 5) - 12;
        int InfoStartY = height / 2 + 4 * (ButtonHeight + 5) + 5;
        int totalButtonWidth = 2 * ButtonWidth + 20;
        int textX = width / 2 - totalButtonWidth / 2;
        for (int Col = 0; Col < 2; Col++) {
            for (int Row = 0; Row < 8; Row++) {
                int ButtonX = ButtonStartX + Col * (ButtonWidth + 20);
                int ButtonY = ButtonStartY + Row * (ButtonHeight + 5);
                Button button = Button.builder(Component.nullToEmpty("<-------->"), (buttonWidget) -> {
                    int ID = buttonWidgetList.indexOf(buttonWidget);
                    if (ID >= 0 && ID < buttonForms.size()) {
                        if (buttonForms.get(ID) != null) {
                            SendSetForm(buttonForms.get(ID));
                        }
                    }
                    this.onClose();
                }).size(ButtonWidth, ButtonHeight).pos(ButtonX, ButtonY).build();
                button.visible = false;
                buttonWidgetList.add(button);
                addRenderableWidget(button);
            }
        }
        // 翻页
        Button PagePrevButton = Button.builder(Component.nullToEmpty("<"), (buttonWidget) -> PrevPage()).size(20, 20).pos(width / 2 - 100, height / 2 + 4 * (ButtonHeight + 5) - 5).build();
        this.addRenderableWidget(PagePrevButton);
        Button PageNextButton = Button.builder(Component.nullToEmpty(">"), (buttonWidget) -> NextPage()).size(20, 20).pos(width / 2 + 80, height / 2 + 4 * (ButtonHeight + 5) - 5).build();
        this.addRenderableWidget(PageNextButton);
        LoadPage();
        super.init();
    }

    public void NextPage() {
        int MaxPage = availableForms.size() /  pageSize;
        MaxPage += availableForms.size() % pageSize == 0 ? 0 : 1;
        this.nowPage++;
        if (this.nowPage >= MaxPage) {
            this.nowPage = 0;
        }
        LoadPage();
    }

    public void PrevPage() {
        int MaxPage = availableForms.size() / pageSize;
        MaxPage += availableForms.size() % pageSize == 0 ? 0 : 1;
        this.nowPage--;
        if (this.nowPage < 0) {
            this.nowPage = MaxPage - 1;
        }
        LoadPage();
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    public void renderBackgroundTexture(GuiGraphics context) {
        // 计算居中位置，保持固定尺寸
        int bgX = (this.width - BG_WIDTH) / 2;
        int bgY = (this.height - BG_HEIGHT) / 2;
        context.blit(BG_TEXTURE, bgX, bgY, 0, 0, BG_WIDTH, BG_HEIGHT, BG_WIDTH, BG_HEIGHT);
    }


    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(context);
        //this.renderTexture(context);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return false;
    }
}
