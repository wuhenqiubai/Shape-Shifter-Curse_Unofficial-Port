package net.onixary.shapeShifterCurseFabric.custom_ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsS2C;
import net.onixary.shapeShifterCurseFabric.perk.IDependent;
import net.onixary.shapeShifterCurseFabric.perk.PerkTree;
import net.onixary.shapeShifterCurseFabric.perk.PerkUtils;
import net.onixary.shapeShifterCurseFabric.perk.RegPerks;
import net.onixary.shapeShifterCurseFabric.util.util.BaseSprite;
import net.onixary.shapeShifterCurseFabric.util.util.ISprite;
import net.onixary.shapeShifterCurseFabric.util.util.cost.BaseCost;
import net.onixary.shapeShifterCurseFabric.util.util.cost.ICost;
import net.onixary.shapeShifterCurseFabric.util.util.cost.CostTypeIcons;
import net.onixary.shapeShifterCurseFabric.util.util.cost.IFUSDrawableCostType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

// 标记 UNTESTED 代表这个函数没测试 测试完了就删(估计最后得有一堆没测试函数 还是标一下大概率炸的函数吧)

public class FormUpgradeScreen extends Screen implements WidgetEXUtils.IWidgetEX {
    public static final ResourceLocation TEXTURE = ShapeShifterCurseFabric.identifier("textures/gui/shape_shifter_tuner_ui.png");
    public static final HashMap<Integer, ISprite> levelSprites = new HashMap<>();
    public static final int TEXTURE_WIDTH = 454;
    public static final int TEXTURE_HEIGHT = 190;

    static {
        levelSprites.put(1, new BaseSprite(TEXTURE, TEXTURE_WIDTH, TEXTURE_HEIGHT, 420, 0, 11, 11));
        levelSprites.put(2, new BaseSprite(TEXTURE, TEXTURE_WIDTH, TEXTURE_HEIGHT, 420, 11, 11, 11));
        levelSprites.put(3, new BaseSprite(TEXTURE, TEXTURE_WIDTH, TEXTURE_HEIGHT, 420, 22, 11, 11));
        levelSprites.put(4, new BaseSprite(TEXTURE, TEXTURE_WIDTH, TEXTURE_HEIGHT, 420, 33, 11, 11));
        levelSprites.put(5, new BaseSprite(TEXTURE, TEXTURE_WIDTH, TEXTURE_HEIGHT, 420, 44, 11, 11));
        levelSprites.put(6, new BaseSprite(TEXTURE, TEXTURE_WIDTH, TEXTURE_HEIGHT, 420, 55, 11, 11));
        levelSprites.put(7, new BaseSprite(TEXTURE, TEXTURE_WIDTH, TEXTURE_HEIGHT, 420, 66, 11, 11));
        levelSprites.put(8, new BaseSprite(TEXTURE, TEXTURE_WIDTH, TEXTURE_HEIGHT, 420, 77, 11, 11));
        levelSprites.put(9, new BaseSprite(TEXTURE, TEXTURE_WIDTH, TEXTURE_HEIGHT, 420, 88, 11, 11));
        levelSprites.put(10, new BaseSprite(TEXTURE, TEXTURE_WIDTH, TEXTURE_HEIGHT, 420, 99, 11, 11));
    }

    public static final int BACKGROUND_WIDTH = 420;
    public static final int BACKGROUND_HEIGHT = 190;

    public static final int FORM_MODEL_REVIEW_X = 8;
    public static final int FORM_MODEL_REVIEW_Y = 30;
    public static final int FORM_MODEL_REVIEW_WIDTH = 100;
    public static final int FORM_MODEL_REVIEW_HEIGHT = 130;

    public static final int PERK_UI_X = 110;
    public static final int PERK_UI_Y = 8;
    public static final int PERK_UI_VIEW_Y = 20;
    public static final int PERK_UI_WIDTH = 200;
    public static final int PERK_UI_HEIGHT = 174;

    public static final int PERK_UI_ICON_X = 123;
    public static final int PERK_UI_ICON_Y = 10;
    public static final int PERK_UI_ICON_WIDTH = 17;
    public static final int PERK_UI_ICON_HEIGHT = 17;

    public static final int LEVEL_ICON_Y = 5;  // 以摄像机中心计算
    public static final int LEVEL_ICON_DRAW_X = -5;
    public static final int LEVEL_ICON_WIDTH = 11;
    public static final int LEVEL_ICON_HEIGHT = 11;

    public static final int PERK_INFO_NAME_X = 316;
    public static final int PERK_INFO_NAME_Y = 12;
    public static final int PERK_INFO_NAME_WIDTH = 91;
    public static final int PERK_INFO_NAME_HEIGHT = 14;

    public static final int PERK_INFO_DESC_X = 316;
    public static final int PERK_INFO_DESC_Y = 28;
    public static final int PERK_INFO_DESC_WIDTH = 91;
    public static final int PERK_INFO_DESC_HEIGHT = 115;

    public static final int PERK_INFO_COST_ICON_X = 315;
    public static final int PERK_INFO_COST_ICON_Y = 144;
    public static final int PERK_INFO_COST_ICON_WIDTH = 18;
    public static final int PERK_INFO_COST_ICON_HEIGHT = 18;

    public static final int PERK_INFO_COST_AMOUNT_X = 334;
    public static final int PERK_INFO_COST_AMOUNT_Y = 145;
    public static final int PERK_INFO_COST_AMOUNT_WIDTH = 73;
    public static final int PERK_INFO_COST_AMOUNT_HEIGHT = 17;

    public static final int PERK_INFO_GAIN_BUTTON_X = 316;
    public static final int PERK_INFO_GAIN_BUTTON_Y = 164;
    public static final int PERK_INFO_GAIN_BUTTON_WIDTH = 91;
    public static final int PERK_INFO_GAIN_BUTTON_HEIGHT = 14;

    public int baseX = 0;
    public int baseY = 0;

    public static final ISprite GAINED_SPRITE = new BaseSprite(TEXTURE, TEXTURE_WIDTH, TEXTURE_HEIGHT, 434, 112, 20, 20);
    public static final ISprite SELECTED_SPRITE = new BaseSprite(TEXTURE, TEXTURE_WIDTH, TEXTURE_HEIGHT, 434, 72, 20, 20);
    public static final ISprite CAN_NOT_GAIN_SPRITE = new BaseSprite(TEXTURE, TEXTURE_WIDTH, TEXTURE_HEIGHT, 434, 132, 20, 20);
    public static final ISprite DEPEND_SPRITE = new BaseSprite(TEXTURE, TEXTURE_WIDTH, TEXTURE_HEIGHT, 434, 92, 20, 20);

    public static final HashMap<ResourceLocation, Boolean> perkAvailableMap = new HashMap<>();  // 仅客户端数据 仅影响渲染 仅代表服务器获取这个表时无法获取这个Perk
    public static final HashMap<ResourceLocation, ICost> perkCostMap = new HashMap<>();  // 仅客户端数据 实际消耗由服务器决定
    public static final ICost EMPTY_COST = new BaseCost();

    public int tier = -1;
    public @NotNull PerkTree perkTree;

    public @Nullable PerkTree.PerkNode nowSelectNode;
    // 中心点:
    // Camera 中心
    // Node 左中
    public int cameraPosX = 0;
    public int cameraPosY = 0;
    public float cameraScale = 1.0f;  // 不一定实现 得看手动鼠标计算位置好不好算

    public int nodeWindowX = 0;
    public int nodeWindowY = 0;

    // 基础渲染原点(左上) -> cameraCenter(中心) -> nodeCenter(左中)
    public Vector2i cameraCenter = new Vector2i(0, 0);
    public Vector2i nodeCenter = new Vector2i( 0, 0);

    public static final int nodeBaseX = 25;
    public static final int posXPerTier = 50;
    public static final int nodeLineRootXOffset = 11;
    public static final int nodeLineDependXOffset = -10;
    public static final int LineColor = 0xFF9F9F9F;

    public static final int NodeDrawStartX = -7;
    public static final int NodeDrawStartY = -7;
    public static final int NodeTextureWidth = 16;
    public static final int NodeTextureHeight = 16;

    public static final int NodeSelectStartX = -8;
    public static final int NodeSelectStartY = -8;
    public static final int NodeSelectRectWidth = 18;
    public static final int NodeSelectRectHeight = 18;

    // Widgets
    public StringWidget PerkNameWidget;
    public ScaleScrollTextWidget PerkDescWidget;
    public Button AcquirePerkButton;
    public StringWidget PerkCostAmountWidget;

    public int MaxPerkLevel = 0;

    @Override
    public WidgetEXUtils.WidgetRect getRect() {
        return null;
    }

    public List<WidgetEXUtils.IWidgetEX> WidgetList = new ArrayList<>();

    @Override
    public List<WidgetEXUtils.IWidgetEX> getWidgetList() {
        return this.WidgetList;
    }

    public FormUpgradeScreen(int tier, Component title, @Nullable PerkTree perkTree) {
        super(title);
        this.tier = tier;
        this.perkTree = perkTree != null ? perkTree : Objects.requireNonNull(RegPerks.getPerkTree(RegPerks.EMPTY_PERK_TREE));
        ModPacketsS2C.sendRequestPerkAvailability();
        ModPacketsS2C.sendRequestPerkData();
        for (PerkTree.PerkNode node : this.perkTree.getAllNodes()) {
            if (node.tier > MaxPerkLevel) {
                MaxPerkLevel = node.tier;
            }
        }
    }

    @Override
    public void init() {
        baseX = this.width / 2 - BACKGROUND_WIDTH / 2;
        baseY = this.height / 2 - BACKGROUND_HEIGHT / 2;
        this.PerkNameWidget = new StringWidget(baseX + PERK_INFO_NAME_X, baseY + PERK_INFO_NAME_Y, PERK_INFO_NAME_WIDTH, PERK_INFO_NAME_HEIGHT, Component.literal(""), this.font);
        this.PerkDescWidget = new ScaleScrollTextWidget(baseX + PERK_INFO_DESC_X, baseY + PERK_INFO_DESC_Y, PERK_INFO_DESC_WIDTH, PERK_INFO_DESC_HEIGHT, 1.0f, Component.literal(""), this.font);
        this.PerkDescWidget.setEnableScrollableIconRender(true);
        this.WidgetList.add(this.PerkDescWidget);
        this.AcquirePerkButton = Button.builder(Component.literal("GET"), button -> {
            if (this.nowSelectNode != null) {
                PerkUtils.addPerk(Minecraft.getInstance().player, this.perkTree.getID(), this.nowSelectNode.perkID);
                ModPacketsS2C.sendRequestPerkAvailability();
            }
        }).pos(baseX + PERK_INFO_GAIN_BUTTON_X, baseY + PERK_INFO_GAIN_BUTTON_Y).size(PERK_INFO_GAIN_BUTTON_WIDTH, PERK_INFO_GAIN_BUTTON_HEIGHT).build();
        this.PerkCostAmountWidget = new StringWidget(baseX + PERK_INFO_COST_AMOUNT_X, baseY + PERK_INFO_COST_AMOUNT_Y, PERK_INFO_COST_AMOUNT_WIDTH, PERK_INFO_COST_AMOUNT_HEIGHT, Component.literal(""), this.font);
        this.PerkCostAmountWidget.alignRight();
        this.addRenderableWidget(this.PerkNameWidget);
        this.addRenderableWidget(this.PerkDescWidget);
        this.addRenderableWidget(this.AcquirePerkButton);
        this.addRenderableWidget(this.PerkCostAmountWidget);
        super.init();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.onClickWidget(mouseX, mouseY, button);
        this.NodeScreenMouseClickHandler((int)mouseX, (int)mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.onReleaseWidget(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        this.onDragWidget(mouseX, mouseY, button, deltaX, deltaY);
        this.NodeScreenMouseDragHandler((int)mouseX, (int)mouseY, button, deltaX, deltaY);
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double mouseZ, double g) {
        this.onScrollWidget(mouseX, mouseY, mouseZ);
        this.NodeScreenMouseScrollHandler((int)mouseX, (int)mouseY, mouseZ);
        return super.mouseScrolled(mouseX, mouseY, mouseZ, g);
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
        baseX = this.width / 2 - BACKGROUND_WIDTH / 2;
        baseY = this.height / 2 - BACKGROUND_HEIGHT / 2;
        context.blit(TEXTURE, baseX, baseY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        nodeWindowX = baseX + PERK_UI_X;
        nodeWindowY = baseY + PERK_UI_Y;
        cameraCenter = new Vector2i(nodeWindowX + PERK_UI_WIDTH / 2, nodeWindowY + PERK_UI_HEIGHT / 2);
        nodeCenter = new Vector2i( -PERK_UI_WIDTH / 2, 0);
        super.render(context, mouseX, mouseY, delta);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        context.blit(TEXTURE, baseX + PERK_UI_ICON_X, baseY + PERK_UI_ICON_Y, 434, 0, PERK_UI_ICON_WIDTH, PERK_UI_ICON_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        RenderSystem.disableBlend();
        this.drawAllNode(context, mouseX, mouseY, delta);

        if (minecraft.player != null) {
            int viewportX = baseX + FORM_MODEL_REVIEW_X;
            int viewportY = baseY + FORM_MODEL_REVIEW_Y;
            int entityX = viewportX + FORM_MODEL_REVIEW_WIDTH / 2;
            int entityY = viewportY + FORM_MODEL_REVIEW_HEIGHT - 15;
            int entitySize = 50;
            RenderEntityInViewport(
                    context,
                    viewportX, viewportY,
                    FORM_MODEL_REVIEW_WIDTH, FORM_MODEL_REVIEW_HEIGHT,
                    entityX, entityY,
                    entitySize,
                    entityX - mouseX, entityY - mouseY - entitySize,
                    minecraft.player
            );
        }

        if (this.nowSelectNode != null) {
            ICost nowCost = perkCostMap.getOrDefault(this.nowSelectNode.perkID, EMPTY_COST);
            if (nowCost.getType() instanceof IFUSDrawableCostType<?>) {
                int rx = baseX + PERK_INFO_COST_ICON_X;
                int ry = baseY + PERK_INFO_COST_ICON_Y;
                // 图标绘制走纯客户端的 CostTypeIcons —— IFUSDrawableCostType 不能再带
                // GuiGraphics 签名的方法，否则实现类在专用服务端加载时就会拉起客户端类。
                CostTypeIcons.drawIcon(nowCost.getType(), context, nowCost, minecraft.player, rx, ry, 0);
                CostTypeIcons.drawOnHover(nowCost.getType(), context, nowCost, minecraft.player, rx, ry, 0, mouseX - rx, mouseY - ry);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
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

    // Utils

    public void drawConnectLine(GuiGraphics context, PerkTree.PerkNode perkNode) {
        List<IDependent> depends = perkNode.dependents;
        if (depends.isEmpty()) return;
        for (IDependent depend : depends) {
            depend.drawDependentLine(context, nodeCenter, perkTree, perkNode);
        }
    }

    // playerGainedPerk 由调用方获取 毕竟drawNode调用频繁
    public void drawNode(GuiGraphics context, PerkTree.PerkNode perkNode, @Nullable List<ResourceLocation> playerGainedPerk, int mouseX, int mouseY, float delta) {
        // this.drawConnectLine(context, perkNode);
        ISprite icon = RegPerks.getPerkIcon(perkNode.perkID);
        if (icon == null) {
            icon = RegPerks.FALLBACK_PERK_ICON;
        }
        int virtualNodeX = nodeBaseX + posXPerTier * perkNode.tier;
        int virtualNodeY = perkNode.y;
        int NodePosX = nodeCenter.x + virtualNodeX;
        int NodePosY = nodeCenter.y + virtualNodeY;
        // int left = virtualNodeX + NodeSelectStartX;
        // int top = virtualNodeY + NodeSelectStartY;
        if (playerGainedPerk != null && playerGainedPerk.contains(perkNode.perkID)) {
            GAINED_SPRITE.draw(context, NodePosX - 9, NodePosY - 9);
        } else if (!perkAvailableMap.getOrDefault(perkNode.perkID, true)) {
            CAN_NOT_GAIN_SPRITE.draw(context, NodePosX - 9, NodePosY - 9);
        }

        if (this.nowSelectNode != null) {
            if (perkNode == this.nowSelectNode) {
                SELECTED_SPRITE.draw(context, NodePosX - 9, NodePosY - 9);
            } else if (this.nowSelectNode.dependents != null && this.nowSelectNode.dependents.stream().anyMatch(d -> d.isDependentPerk(perkNode.perkID))) {
                DEPEND_SPRITE.draw(context, NodePosX - 9, NodePosY - 9);
            }
        }
        // if (mouseX >= left && mouseX < left + NodeSelectRectWidth && mouseY >= top && mouseY < top + NodeSelectRectHeight) {
        //     context.drawTexture(LABEL_SELECT, NodePosX - 9, NodePosY - 9, 0, 0, 20, 20, 20, 20);
        // }
        icon.draw(context, NodePosX + NodeDrawStartX, NodePosY + NodeDrawStartY);
        // context.drawTexture(icon, NodePosX + NodeDrawStartX, NodePosY + NodeDrawStartY, 0, 0, NodeTextureWidth, NodeTextureHeight, NodeTextureWidth, NodeTextureHeight);
        // final int PerkNameBoxWidth = 33;
        // Text perkNameText = RegPerks.getPerkName(perkNode.perkID);
        // int perkNameTextWidth = this.textRenderer.getWidth(perkNameText);
        // int iconCenterX = NodePosX + NodeDrawStartX + NodeTextureWidth / 2;
        // int perkNameY = NodePosY + NodeDrawStartY + NodeTextureHeight + 2;
        // int perkNameBoxLeftX = iconCenterX - PerkNameBoxWidth / 2;
        // int perkNameX = perkNameBoxLeftX + (PerkNameBoxWidth - perkNameTextWidth) / 2;
        // context.drawText(
        //                 this.textRenderer,
        //         perkNameText,
        //         perkNameX,
        //         perkNameY,
        //         0xFFFFFFFF,
        //         false
        //         );
    }

    public void drawVirtualNode(GuiGraphics context, PerkTree.PerkNode perkNode, @Nullable List<ResourceLocation> playerGainedPerk, int mouseX, int mouseY, float delta) {
        ISprite icon = RegPerks.getPerkIcon(perkNode.perkID);
        if (icon == null) {
            icon = RegPerks.FALLBACK_PERK_ICON;
        }
        int virtualNodeX = nodeBaseX + posXPerTier * perkNode.tier;
        int virtualNodeY = perkNode.y;
        int NodePosX = nodeCenter.x + virtualNodeX;
        int NodePosY = nodeCenter.y + virtualNodeY;
        icon.draw(context, NodePosX + NodeDrawStartX, NodePosY + NodeDrawStartY);
    }

    public void drawAllNode(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (this.minecraft == null) return;
        context.enableScissor(nodeWindowX, nodeWindowY, nodeWindowX + PERK_UI_WIDTH, nodeWindowY + PERK_UI_HEIGHT);
        PoseStack matrixStack = context.pose();
        int firstX = nodeBaseX + nodeCenter.x;
        int firstY = nodeWindowY + LEVEL_ICON_Y;
        for (int tierIndex = 1; tierIndex <= this.MaxPerkLevel; tierIndex++) {
            int localLineX = firstX + tierIndex * posXPerTier;
            float iconCenterScreenX =
                    cameraCenter.x + cameraPosX + cameraScale * (localLineX + 1.0f);
            int lineLeftX = Math.round(iconCenterScreenX - 0.5f);
            // context.fill(
            //         lineLeftX, nodeWindowY,
            //         lineLeftX + 1, nodeWindowY + PERK_UI_HEIGHT,
            //         LineColor
            // );
            context.blit(TEXTURE, lineLeftX - 1, nodeWindowY, 431, 0, 3, PERK_UI_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            int screenIconX = lineLeftX - (LEVEL_ICON_WIDTH - 1) / 2;
            ISprite icon = levelSprites.get(tierIndex);
            if (icon != null) {
                icon.draw(context, screenIconX, firstY);
            } else {
                context.fill(
                        screenIconX, firstY,
                        screenIconX + LEVEL_ICON_WIDTH, firstY + LEVEL_ICON_HEIGHT,
                        LineColor
                );
            }
        }
        RenderSystem.disableBlend();
        context.disableScissor();
        context.enableScissor(nodeWindowX, nodeWindowY + PERK_UI_VIEW_Y, nodeWindowX + PERK_UI_WIDTH, nodeWindowY + PERK_UI_HEIGHT);
        matrixStack.pushPose();
        matrixStack.translate(cameraCenter.x + cameraPosX, cameraCenter.y + cameraPosY, 0);
        matrixStack.scale(cameraScale, cameraScale, 1.0f);
        PerkTree tree = this.perkTree;
        List<ResourceLocation> playerGainedPerk = PerkUtils.getPlayerPerks(this.minecraft.player, tree.getID());
        Vector2i vMousePos = getVirtualMousePos(mouseX, mouseY);
        for (PerkTree.PerkNode perkNode : tree.getAllNodes()) {
            this.drawConnectLine(context, perkNode);
        }
        for (PerkTree.PerkNode perkNode : tree.getAllNodes()) {
            this.drawNode(context, perkNode, playerGainedPerk, vMousePos.x, vMousePos.y, delta);
        }
        for (PerkTree.PerkNode perkNode : tree.getAllVirtualNodes()) {
            this.drawVirtualNode(context, perkNode, playerGainedPerk, vMousePos.x, vMousePos.y, delta);
        }
        matrixStack.popPose();
        context.disableScissor();
    }

    public Vector2i getVirtualMousePos(int mouseX, int mouseY) {
        float relX = (mouseX - cameraCenter.x - cameraPosX) / cameraScale - nodeCenter.x;
        float relY = (mouseY - cameraCenter.y - cameraPosY) / cameraScale - nodeCenter.y;
        return new Vector2i((int) relX, (int) relY);
    }

    public @Nullable PerkTree.PerkNode getMouseNode(int mouseX, int mouseY) {
        for (PerkTree.PerkNode perkNode : this.perkTree.getAllNodes()) {
            int centerX = nodeBaseX + posXPerTier * perkNode.tier;
            int centerY = perkNode.y;
            int left = centerX + NodeSelectStartX;
            int top = centerY + NodeSelectStartY;
            if (mouseX >= left && mouseX < left + NodeSelectRectWidth && mouseY >= top && mouseY < top + NodeSelectRectHeight) {
                return perkNode;
            }
        }
        return null;
    }

    public void NodeScreenMouseClickHandler(int mouseX, int mouseY, int mode) {
        if (mouseX < nodeWindowX || mouseX >= nodeWindowX + PERK_UI_WIDTH || mouseY < nodeWindowY || mouseY >= nodeWindowY + PERK_UI_HEIGHT) {
            return;
        }
        Vector2i trueMousePos = getVirtualMousePos(mouseX, mouseY);
        @Nullable PerkTree.PerkNode node = getMouseNode(trueMousePos.x, trueMousePos.y);
        this.nowSelectNode = node;
        this.onNodeSelect();
    }

    public double totalDragX = 0;
    public double totalDragY = 0;

    public void NodeScreenMouseDragHandler(int mouseX, int mouseY, int mode, double deltaX, double deltaY) {
        if (mouseX < nodeWindowX || mouseX >= nodeWindowX + PERK_UI_WIDTH || mouseY < nodeWindowY || mouseY >= nodeWindowY + PERK_UI_HEIGHT) {
            return;
        }
        if (mode == 0) {
            totalDragX += deltaX;
            totalDragY += deltaY;
            int dragX = (int) totalDragX;
            int dragY = (int) totalDragY;
            if (dragX != 0 || dragY != 0) {
                cameraPosX += dragX;
                cameraPosY += dragY;
                totalDragX -= dragX;
                totalDragY -= dragY;
            }
        }
    }

    public void NodeScreenMouseScrollHandler(int mouseX, int mouseY, double scroll) {
        if (mouseX < nodeWindowX || mouseX >= nodeWindowX + PERK_UI_WIDTH
                || mouseY < nodeWindowY || mouseY >= nodeWindowY + PERK_UI_HEIGHT) {
            return;
        }
        if (scroll == 0) return;
        float oldScale = cameraScale;
        float newScale = oldScale * (float) Math.pow(1.1, scroll);
        newScale = Math.max(0.25f, Math.min(4.0f, newScale));
        if (newScale == oldScale) return;
        float worldX = (mouseX - cameraCenter.x - cameraPosX) / oldScale;
        float worldY = (mouseY - cameraCenter.y - cameraPosY) / oldScale;
        cameraPosX = (int) (mouseX - cameraCenter.x - worldX * newScale);
        cameraPosY = (int) (mouseY - cameraCenter.y - worldY * newScale);
        cameraScale = newScale;
    }

    public boolean isNowPerkCanGain() {
        if (this.nowSelectNode == null) {
            return false;
        }
        if (this.nowSelectNode.tier > this.tier) {
            return false;
        }
        List<ResourceLocation> playerGainedPerk = PerkUtils.getPlayerPerks(this.minecraft.player, this.perkTree.getID());
        if (playerGainedPerk != null && playerGainedPerk.contains(this.nowSelectNode.perkID)) {
            return false;
        }
        if (this.nowSelectNode.dependents != null && !this.nowSelectNode.dependents.isEmpty()) {
            for (IDependent dependent : this.nowSelectNode.dependents) {
                if (!dependent.isAllDependentGained(this.minecraft.player, playerGainedPerk)) return false;
            }
        }
        ICost cost = perkCostMap.get(this.nowSelectNode.perkID);
        if (!PerkUtils.isFreeUnlock(minecraft.player) && cost != null && !cost.getType().canPay_CLIENT(cost, minecraft.player)) {
            return false;
        }
        return true;
    }

    public void onNodeSelect() {
        try {
            Minecraft.getInstance().player.displayClientMessage(Component.literal("Node Selected: " + this.nowSelectNode.perkID.toString()), false);
            Minecraft.getInstance().player.displayClientMessage(Component.literal("Can Gained (Cache): " + this.perkAvailableMap.getOrDefault(this.nowSelectNode.perkID, true)), false);
        } catch (Exception e) {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("No Node Selected"));
        }
        if (this.nowSelectNode != null) {
            this.PerkNameWidget.setMessage(RegPerks.getPerkName(this.nowSelectNode.perkID));
            this.PerkDescWidget.reloadText(RegPerks.getPerkDescription(this.nowSelectNode.perkID));
            ICost cost = perkCostMap.get(this.nowSelectNode.perkID);
            if (cost != null && cost.getType() instanceof IFUSDrawableCostType<?> ifusDrawableCostType) {
                this.PerkCostAmountWidget.setMessage(ifusDrawableCostType.getAmountText(cost, minecraft.player));
            } else {
                this.PerkCostAmountWidget.setMessage(Component.literal(""));
            }
            this.AcquirePerkButton.active = this.isNowPerkCanGain();
        } else {
            this.PerkNameWidget.setMessage(Component.literal(""));
            this.PerkDescWidget.reloadText(Component.literal(""));
            this.PerkCostAmountWidget.setMessage(Component.literal(""));
            this.AcquirePerkButton.active = false;
        }
        ModPacketsS2C.sendRequestPerkAvailability();
    }
}
