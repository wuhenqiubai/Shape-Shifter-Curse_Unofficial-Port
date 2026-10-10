package net.onixary.shapeShifterCurseFabric.custom_ui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.custom_ui.ui_part.ScaleScrollTextWidget;
import net.onixary.shapeShifterCurseFabric.custom_ui.ui_part.WidgetEXUtils;
import net.onixary.shapeShifterCurseFabric.data.CodexData;
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsS2C;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.ISubForm;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

public class SubFormSelectScreen extends Screen implements WidgetEXUtils.IWidgetEX, FormTextureUtils.TempFormModelProcessor {
    private static final int MENU_WIDTH = 420;
    private static final int MENU_HEIGHT = 227;
    private static final ResourceLocation MENU_TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/sub_form_menu.png");

    private static final int BG_WIDTH = 420;
    private static final int BG_HEIGHT = 227;
    private static final ResourceLocation BG_TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/sub_form_menu_bg.png");

    private List<ResourceLocation> availableForms;
    private int nowFormIndex = 0;
    private boolean isLockTempModelSystem = false;

    private StringWidget formNameText;
    private ScaleScrollTextWidget formDescText;
    private Button selectFormButton;
    private Button prevFormButton;
    private Button nextFormButton;

    public static void openSubFormSelectScreen() {
        Minecraft client = Minecraft.getInstance();
        if (getAvailableForms(client).isEmpty()) {
            client.setScreen(new SubFormSelectScreen_NoForm(Component.empty()));
        } else {
            client.setScreen(new SubFormSelectScreen(Component.empty()));
        }
    }

    public SubFormSelectScreen(Component title) {
        super(title);
        if (!FormTextureUtils.useTempFormModel) {
            FormTextureUtils.useTempFormModel = true;
            FormTextureUtils.tempFormModelProcessor = this;
            isLockTempModelSystem = true;
        } else {
            ShapeShifterCurseFabric.LOGGER.warn("Temp Form Model System is already in use, dynamic form rendering will not work");
        }
    }

    public static List<ResourceLocation> getAvailableForms(Minecraft client) {
        List<ResourceLocation> availableForms = new ArrayList<>();
        IForm playerForm = FormUtils.getPlayerForm(client.player);
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
        subForms.removeIf(form -> !(FormUtils.isFormCanUse(client.player, form)));
        availableForms.addAll(subForms.stream().map(IForm::getFormID).toList());
        return availableForms;
    }

    private void SendSetForm(ResourceLocation formID) {
        ModPacketsS2C.sendSetSubForm(formID);
    }

    @Override
    public void init() {
        availableForms = getAvailableForms(minecraft);
        int baseX = (this.width - MENU_WIDTH) / 2;
        int baseY = (this.height - MENU_HEIGHT) / 2;
        // 152 32 116 14 - Label
        this.addRenderableWidget(new StringWidget(baseX + 152, baseY + 32, 116, 14, Component.literal("Sub Form Select Menu"), this.font));
        // 393 7 20 20 - Close
        this.addRenderableWidget(Button.builder(Component.literal("X"), button -> this.onClose()).pos(baseX + 393, baseY + 7).size(20, 20).build());

        // 223 58 77 14 - FormName
        formNameText = new StringWidget(baseX + 223, baseY + 58, 77, 14, Component.literal(""), this.font);
        // 223 75 77 88 - Form Desc
        formDescText = new ScaleScrollTextWidget(baseX + 223, baseY + 75, 77, 88, 1.0f, Component.literal(""), this.font);
        // 223 166 77 14 - Select Form Button
        selectFormButton = Button.builder(Component.literal("SELECT"), button -> {
            ResourceLocation formID = availableForms.get(nowFormIndex);
            if (formID != null) {
                SendSetForm(formID);
                this.onClose();
            }
        }).pos(baseX + 223, baseY + 166).size(77, 14).build();
        // 93 116 16 16 - Prev Form Button
        prevFormButton = Button.builder(Component.literal("<"), button -> {
            nowFormIndex--;
            if (nowFormIndex < 0) {
                nowFormIndex = availableForms.size() - 1;
            }
            updateInfo();
        }).pos(baseX + 93, baseY + 116).size(16, 16).build();
        // 311 116 16 16 - Next Form Button
        nextFormButton = Button.builder(Component.literal(">"), button -> {
            nowFormIndex++;
            if (nowFormIndex >= availableForms.size()) {
                nowFormIndex = 0;
            }
            updateInfo();
        }).pos(baseX + 311, baseY + 116).size(16, 16).build();

        selectFormButton.active = availableForms.size() > 1;
        prevFormButton.active = false;
        nextFormButton.active = false;

        this.addRenderableWidget(formNameText);
        this.addRenderableWidget(formDescText);
        this.addRenderableWidget(selectFormButton);
        this.addRenderableWidget(prevFormButton);
        this.addRenderableWidget(nextFormButton);

        this.updatePageButtons();
        this.updateInfo();
        super.init();
    }

    @Override
    public void onClose() {
        if (this.isLockTempModelSystem) {
            FormTextureUtils.useTempFormModel = false;
            FormTextureUtils.tempFormModelProcessor = null;
            isLockTempModelSystem = false;
        }
        super.onClose();
    }

    public void renderBackgroundTexture(GuiGraphics context) {
        // 计算居中位置，保持固定尺寸
        context.blit(BG_TEXTURE, 0, 0, this.width, this.height, 0, 0, BG_WIDTH, BG_HEIGHT, BG_WIDTH, BG_HEIGHT);
        int bgX = (this.width - MENU_WIDTH) / 2;
        int bgY = (this.height - MENU_HEIGHT) / 2;
        context.blit(MENU_TEXTURE, bgX, bgY, 0, 0, MENU_WIDTH, MENU_HEIGHT, MENU_WIDTH, MENU_HEIGHT);
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
        InventoryScreen.renderEntityInInventory(context, x, y, size, new Vector3f(), quaternionf, quaternionf2, entity);
        entity.yBodyRot = h;
        entity.yBodyRotO = m;
        entity.setYRot(i);
        entity.setXRot(j);
        entity.yHeadRotO = k;
        entity.yHeadRot = l;
    }

    private void RenderEntityInViewport(GuiGraphics context, int viewportX, int viewportY, int viewportWidth, int viewportHeight, int x, int y, int size, int mouseX, int mouseY, LivingEntity entity) {
        context.enableScissor(viewportX, viewportY, viewportX + viewportWidth, viewportY + viewportHeight);
        try {
            RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
            RenderEntity(context, x, y, size, mouseX, mouseY, entity);
        } finally {
            context.disableScissor();
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(context);
        int baseX = (this.width - MENU_WIDTH) / 2;
        int baseY = (this.height - MENU_HEIGHT) / 2;
        // 120 58 100 133
        if (minecraft.player != null) {
            int viewportX = baseX + 120;
            int viewportY = baseY + 50;
            int entityX = viewportX + 100 / 2;
            int entityY = viewportY + 133 - 15;
            int entitySize = 50;
            RenderEntityInViewport(
                    context,
                    viewportX, viewportY,
                    100, 133,
                    entityX, entityY,
                    entitySize,
                    entityX - mouseX, entityY - mouseY - entitySize,
                    minecraft.player
            );
        }
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

    @Override
    public WidgetEXUtils.WidgetRect getRect() {
        return null;
    }

    public List<WidgetEXUtils.IWidgetEX> WidgetList = new ArrayList<>();

    @Override
    public List<WidgetEXUtils.IWidgetEX> getWidgetList() {
        return this.WidgetList;
    }

    public void updatePageButtons() {
        prevFormButton.active = nowFormIndex > 0;
        nextFormButton.active = nowFormIndex < availableForms.size() - 1;
    }

    public void clearInfo() {
        formNameText.setMessage(Component.literal(""));
        formDescText.setMessage(Component.literal(""));
        selectFormButton.active = false;
        updatePageButtons();
    }

    public void updateInfo() {
        if (availableForms.isEmpty()) {
            clearInfo();
            return;
        }
        ResourceLocation formID = availableForms.get(nowFormIndex);
        if (formID == null) {
            clearInfo();
            return;
        }
        IForm form = RegPlayerForms.getPlayerForm(formID);
        if (form == null) {
            clearInfo();
            return;
        }
        formNameText.setMessage(form.getContentText(CodexData.ContentType.NAME));
        formDescText.setMessage(form.getContentText(CodexData.ContentType.DESC));
        selectFormButton.active = true;
        updatePageButtons();
    }

    @Override
    public IForm getForm() {
        if (availableForms.isEmpty()) {
            return FormUtils.getPlayerForm(minecraft.player);
        }
        IForm form = RegPlayerForms.getPlayerForm(availableForms.get(nowFormIndex));
        if (form == null) {
            return FormUtils.getPlayerForm(minecraft.player);
        }
        return form;
    }

    @Override
    public ResourceLocation getLayerID() {
        IForm playerForm = this.getForm();
        return playerForm.getRenderLayerOverride() == null ? playerForm.getFormLayer().getB() : playerForm.getRenderLayerOverride().getB();
    }
}
