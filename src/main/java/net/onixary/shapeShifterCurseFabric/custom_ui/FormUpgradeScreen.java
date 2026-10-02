package net.onixary.shapeShifterCurseFabric.custom_ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.custom_ui.ui_part.ScaleScrollTextWidget;
import net.onixary.shapeShifterCurseFabric.custom_ui.ui_part.WidgetEXUtils;
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsS2C;
import net.onixary.shapeShifterCurseFabric.perk.PerkTree;
import net.onixary.shapeShifterCurseFabric.perk.PerkUtils;
import net.onixary.shapeShifterCurseFabric.perk.RegPerks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
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
    public static final Identifier TEXTURE = ShapeShifterCurseFabric.identifier("textures/gui/shape_shifter_tuner_ui.png");
    public static final int TEXTURE_WIDTH = 420;
    public static final int TEXTURE_HEIGHT = 190;

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

    public static final int PERK_INFO_XP_ICON_X = 316;
    public static final int PERK_INFO_XP_ICON_Y = 145;
    public static final int PERK_INFO_XP_ICON_WIDTH = 16;
    public static final int PERK_INFO_XP_ICON_HEIGHT = 17;

    public static final int PERK_INFO_XP_COST_X = 334;
    public static final int PERK_INFO_XP_COST_Y = 145;
    public static final int PERK_INFO_XP_COST_WIDTH = 73;
    public static final int PERK_INFO_XP_COST_HEIGHT = 17;

    public static final int PERK_INFO_GAIN_BUTTON_X = 316;
    public static final int PERK_INFO_GAIN_BUTTON_Y = 164;
    public static final int PERK_INFO_GAIN_BUTTON_WIDTH = 91;
    public static final int PERK_INFO_GAIN_BUTTON_HEIGHT = 14;

    public int baseX = 0;
    public int baseY = 0;

    public static final Identifier LABEL_GAINED = ShapeShifterCurseFabric.identifier("textures/perk/system/gained.png");
    public static final Identifier LABEL_SELECT = ShapeShifterCurseFabric.identifier("textures/perk/system/select.png");
    public static final Identifier LABEL_SELECTED = ShapeShifterCurseFabric.identifier("textures/perk/system/selected.png");
    public static final Identifier LABEL_CAN_NOT_GAIN = ShapeShifterCurseFabric.identifier("textures/perk/system/can_not_gain.png");
    public static final Identifier LABEL_DEPEND = ShapeShifterCurseFabric.identifier("textures/perk/system/depend.png");

    public static final HashMap<Identifier, Boolean> perkAvailableMap = new HashMap<>();  // 仅客户端数据 仅影响渲染 仅代表服务器获取这个表时无法获取这个Perk
    public static final HashMap<Identifier, Integer> perkXpCostMap = new HashMap<>();  // 仅客户端数据 实际消耗由服务器决定

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
    public StringWidget PerkXpCostWidget;

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
        this.PerkXpCostWidget = new StringWidget(baseX + PERK_INFO_XP_COST_X, baseY + PERK_INFO_XP_COST_Y, PERK_INFO_XP_COST_WIDTH, PERK_INFO_XP_COST_HEIGHT, Component.literal(""), this.font);
        // 1.21.11: StringWidget 移除了 alignRight()/horizontalAlignment（1.21.1 是靠私有 alignX 字段
        // 在 renderWidget 内做 x 偏移）。改为在 setPerkXpCostText() 里直接调整 widget 的 x 实现右对齐。
        this.setPerkXpCostText("");
        this.addRenderableWidget(this.PerkNameWidget);
        this.addRenderableWidget(this.PerkDescWidget);
        this.addRenderableWidget(this.AcquirePerkButton);
        this.addRenderableWidget(this.PerkXpCostWidget);
        super.init();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        // 1.21.11: mouseClicked 的形参改成了 MouseButtonEvent，旧代码里的 mouseX/mouseY/button 要从事件里取
        double mouseX = mouseButtonEvent.x();
        double mouseY = mouseButtonEvent.y();
        int button = mouseButtonEvent.button();
        this.onClickWidget(mouseX, mouseY, button);
        this.NodeScreenMouseClickHandler((int)mouseX, (int)mouseY, button);
        return super.mouseClicked(mouseButtonEvent, bl);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        // 1.21.11: mouseReleased/mouseDragged 的形参同样改成了 MouseButtonEvent
        double mouseX = mouseButtonEvent.x();
        double mouseY = mouseButtonEvent.y();
        int button = mouseButtonEvent.button();
        this.onReleaseWidget(mouseX, mouseY, button);
        return super.mouseReleased(mouseButtonEvent);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double deltaX, double deltaY) {
        double mouseX = mouseButtonEvent.x();
        double mouseY = mouseButtonEvent.y();
        int button = mouseButtonEvent.button();
        this.onDragWidget(mouseX, mouseY, button, deltaX, deltaY);
        this.NodeScreenMouseDragHandler((int)mouseX, (int)mouseY, button, deltaX, deltaY);
        return super.mouseDragged(mouseButtonEvent, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double mouseZ, double g) {
        this.onScrollWidget(mouseX, mouseY, mouseZ);
        this.NodeScreenMouseScrollHandler((int)mouseX, (int)mouseY, mouseZ);
        return super.mouseScrolled(mouseX, mouseY, mouseZ, g);
    }

    private void RenderEntity(GuiGraphics context, int x, int y, int size, int mouseX, int mouseY, LivingEntity entity) {
        // 1.21.11: renderEntityInInventoryFollowsMouse 改为「矩形区域 + 鼠标绝对坐标」签名，
        // 内部自行完成 bodyRot/yRot 的鼠标跟随计算，并改用 render state（不再改写实体自身状态）。
        // 原先手写的 atan / Quaternionf / 实体朝向保存恢复整段随之删除。
        // 注意 mouseX/mouseY 现在要传【绝对屏幕坐标】（旧签名收的是「中心-鼠标」差值）。
        // 1.21.11: 矩形是实体的渲染视口（画中画裁剪区），size×size 会裁掉模型。
        // 按原版 InventoryScreen 比例（49x70 配 size=30）换算：半宽 ≈ size*0.817、半高 ≈ size*1.167。
        // 1.21.1 的 (x,y) 是绘制原点（实体向上画），此处矩形中心是显示基准 → 上移约半个身高。
        InventoryScreen.renderEntityInInventoryFollowsMouse(
                context,
                x - size * 4 / 5, y - size * 7 / 6 - size / 2,
                x + size * 4 / 5, y + size * 7 / 6 - size / 2,
                size, 0.0625F,
                mouseX, mouseY, entity);
    }

    // 1.21.11: StringWidget 不再支持内部对齐（alignRight/horizontalAlignment 已移除），
    // 改为按「基准 x + 区域宽 - 文本宽」手动设置 widget 位置来实现右对齐。
    private void setPerkXpCostText(String text) {
        Component component = Component.literal(text);
        this.PerkXpCostWidget.setMessage(component);
        this.PerkXpCostWidget.setX(baseX + PERK_INFO_XP_COST_X + PERK_INFO_XP_COST_WIDTH - this.font.width(component));
    }

    private void RenderEntityInViewport(GuiGraphics context, int viewportX, int viewportY, int viewportWidth, int viewportHeight, int x, int y, int size, int mouseX, int mouseY, LivingEntity entity) {
        context.enableScissor(viewportX, viewportY, viewportX + viewportWidth, viewportY + viewportHeight);
        try {
            // 1.21.11: RenderSystem.clear 已随 GL5 管线移除；原版 InventoryScreen 在 GUI 内渲染实体时
            // 也不再手动清深度（帧级清屏由 GuiRenderer / GameRenderer 负责），故此处直接去掉。
            RenderEntity(context, x, y, size, mouseX, mouseY, entity);
        } finally {
            context.disableScissor();
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        baseX = this.width / 2 - BACKGROUND_WIDTH / 2;
        baseY = this.height / 2 - BACKGROUND_HEIGHT / 2;
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, baseX, baseY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT, -1);
        nodeWindowX = baseX + PERK_UI_X;
        nodeWindowY = baseY + PERK_UI_Y;
        cameraCenter = new Vector2i(nodeWindowX + PERK_UI_WIDTH / 2, nodeWindowY + PERK_UI_HEIGHT / 2);
        nodeCenter = new Vector2i( -PERK_UI_WIDTH / 2, 0);
        context.fill(baseX + PERK_UI_ICON_X, baseY + PERK_UI_ICON_Y, baseX + PERK_UI_ICON_X + PERK_UI_ICON_WIDTH, baseY + PERK_UI_ICON_Y + PERK_UI_ICON_HEIGHT, 0xFFFFFFFF);
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
                    mouseX, mouseY,
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
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (super.keyPressed(keyEvent)) {
            return true;
        } else if (this.minecraft.options.keyInventory.matches(keyEvent)) {
            this.onClose();
            return true;
        }
        return false;
    }

    // Utils

    public void drawConnectLine(GuiGraphics context, PerkTree.PerkNode perkNode) {
        List<Identifier> depends = perkNode.dependentPerkIDs;
        if (depends.isEmpty()) return;
        for (Identifier depend : depends) {
            PerkTree.PerkNode dependNodeMetaData = perkTree.getNode(depend);
            if (dependNodeMetaData == null) return;
            int ox = nodeCenter.x;
            int oy = nodeCenter.y;
            int x1 = nodeBaseX + posXPerTier * perkNode.tier + nodeLineDependXOffset;
            int x2 = nodeBaseX + posXPerTier * dependNodeMetaData.tier + nodeLineRootXOffset;
            int y1 = perkNode.y;
            int y2 = dependNodeMetaData.y;
            if (perkNode.tier - 1 == dependNodeMetaData.tier) {
                int halfX = (x1 + x2) / 2;
                context.fill(
                        ox + Math.min(x1, halfX), oy + y1,
                        ox + Math.max(x1, halfX) + 1, oy + y1 + 1,
                        LineColor);
                context.fill(
                        ox + halfX, oy + Math.min(y1, y2),
                        ox + halfX + 1, oy + Math.max(y1, y2) + 1,
                        LineColor);
                context.fill(
                        ox + Math.min(x2, halfX), oy + y2,
                        ox + Math.max(x2, halfX) + 1, oy + y2 + 1,
                        LineColor);
            } else {
                // AI整的虚线 看起来应该没有对应的API了 所以尽量别整需要虚线的Perk 这种比较费性能 除非使用贴图 但是这种不太好改
                int dashLen = posXPerTier / 2 - 10;
                int dashSize = 2;
                int gapSize = 1;
                int lastPixelX = x1;
                for (int i = 0; i < dashLen; i += dashSize + gapSize) {
                    int to = Math.min(i + dashSize, dashLen);
                    if (i >= to) break;
                    context.fill(
                            ox + x1 - to, oy + y1,
                            ox + x1 - i, oy + y1 + 1,
                            LineColor);
                    lastPixelX = x1 - to;
                }
                context.fill(ox + lastPixelX - 2, oy + y1 - 1, ox + lastPixelX - 1, oy + y1 + 2, LineColor);
            }
        }
    }

    // playerGainedPerk 由调用方获取 毕竟drawNode调用频繁
    public void drawNode(GuiGraphics context, PerkTree.PerkNode perkNode, @Nullable List<Identifier> playerGainedPerk, int mouseX, int mouseY, float delta) {
        // this.drawConnectLine(context, perkNode);
        Identifier icon = RegPerks.getPerkIcon(perkNode.perkID);
        if (icon == null) {
            icon = RegPerks.FALLBACK_PERK_ICON;
        }
        int virtualNodeX = nodeBaseX + posXPerTier * perkNode.tier;
        int virtualNodeY = perkNode.y;
        int NodePosX = nodeCenter.x + virtualNodeX;
        int NodePosY = nodeCenter.y + virtualNodeY;
        int left = virtualNodeX + NodeSelectStartX;
        int top = virtualNodeY + NodeSelectStartY;
        if (playerGainedPerk != null && playerGainedPerk.contains(perkNode.perkID)) {
            context.blit(RenderPipelines.GUI_TEXTURED, LABEL_GAINED, NodePosX - 9, NodePosY - 9, 0, 0, 20, 20, 20, 20, 20, 20, -1);
        } else if (!perkAvailableMap.getOrDefault(perkNode.perkID, true)) {
            context.blit(RenderPipelines.GUI_TEXTURED, LABEL_CAN_NOT_GAIN, NodePosX - 9, NodePosY - 9, 0, 0, 20, 20, 20, 20, 20, 20, -1);
        }

        if (this.nowSelectNode != null) {
            if (perkNode == this.nowSelectNode) {
                context.blit(RenderPipelines.GUI_TEXTURED, LABEL_SELECTED, NodePosX - 9, NodePosY - 9, 0, 0, 20, 20, 20, 20, 20, 20, -1);
            } else if (this.nowSelectNode.dependentPerkIDs.contains(perkNode.perkID)) {
                context.blit(RenderPipelines.GUI_TEXTURED, LABEL_DEPEND, NodePosX - 9, NodePosY - 9, 0, 0, 20, 20, 20, 20, 20, 20, -1);
            }
        }
        if (mouseX >= left && mouseX < left + NodeSelectRectWidth && mouseY >= top && mouseY < top + NodeSelectRectHeight) {
            context.blit(RenderPipelines.GUI_TEXTURED, LABEL_SELECT, NodePosX - 9, NodePosY - 9, 0, 0, 20, 20, 20, 20, 20, 20, -1);
        }
        context.blit(RenderPipelines.GUI_TEXTURED, icon, NodePosX + NodeDrawStartX, NodePosY + NodeDrawStartY, 0, 0, NodeTextureWidth, NodeTextureHeight, NodeTextureWidth, NodeTextureHeight, NodeTextureWidth, NodeTextureHeight, -1);
        final int PerkNameBoxWidth = 33;
        Component perkNameText = RegPerks.getPerkName(perkNode.perkID);
        int perkNameTextWidth = this.font.width(perkNameText);
        int iconCenterX = NodePosX + NodeDrawStartX + NodeTextureWidth / 2;
        int perkNameY = NodePosY + NodeDrawStartY + NodeTextureHeight + 2;
        int perkNameBoxLeftX = iconCenterX - PerkNameBoxWidth / 2;
        int perkNameX = perkNameBoxLeftX + (PerkNameBoxWidth - perkNameTextWidth) / 2;
        context.drawString(
                this.font,
                perkNameText,
                perkNameX,
                perkNameY,
                0xFFFFFFFF,
                false
        );
    }

    public void drawAllNode(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (this.minecraft == null) return;
        context.enableScissor(nodeWindowX, nodeWindowY, nodeWindowX + PERK_UI_WIDTH, nodeWindowY + PERK_UI_HEIGHT);
        // 1.21.11: GuiGraphics.pose() 返回 org.joml.Matrix3x2fStack（原为 PoseStack）
        Matrix3x2fStack matrixStack = context.pose();
        int firstX = nodeBaseX + nodeCenter.x;
        int firstY = nodeWindowY + LEVEL_ICON_Y;
        for (int tierIndex = 1; tierIndex <= this.MaxPerkLevel; tierIndex++) {
            int localLineX = firstX + tierIndex * posXPerTier;
            float iconCenterScreenX =
                    cameraCenter.x + cameraPosX + cameraScale * (localLineX + 1.0f);
            int lineLeftX = Math.round(iconCenterScreenX - 0.5f);
            context.fill(
                    lineLeftX, nodeWindowY,
                    lineLeftX + 1, nodeWindowY + PERK_UI_HEIGHT,
                    LineColor
            );
            int screenIconX = lineLeftX - (LEVEL_ICON_WIDTH - 1) / 2;
            context.fill(
                    screenIconX, firstY,
                    screenIconX + LEVEL_ICON_WIDTH, firstY + LEVEL_ICON_HEIGHT,
                    LineColor
            );
        }
        context.disableScissor();
        context.enableScissor(nodeWindowX, nodeWindowY + PERK_UI_VIEW_Y, nodeWindowX + PERK_UI_WIDTH, nodeWindowY + PERK_UI_HEIGHT);
        // 1.21.11: Matrix3x2fStack 是 JOML 的 2D 矩阵栈 —— pushPose → pushMatrix，
        // translate / scale 只收 2 个分量（无 z）。
        matrixStack.pushMatrix();
        matrixStack.translate(cameraCenter.x + cameraPosX, cameraCenter.y + cameraPosY);
        matrixStack.scale(cameraScale, cameraScale);
        PerkTree tree = this.perkTree;
        List<Identifier> playerGainedPerk = PerkUtils.getPlayerPerks(this.minecraft.player, tree.getID());
        Vector2i vMousePos = getVirtualMousePos(mouseX, mouseY);
        for (PerkTree.PerkNode perkNode : tree.getAllNodes()) {
            this.drawConnectLine(context, perkNode);
        }
        for (PerkTree.PerkNode perkNode : tree.getAllNodes()) {
            this.drawNode(context, perkNode, playerGainedPerk, vMousePos.x, vMousePos.y, delta);
        }
        matrixStack.popMatrix();
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
        List<Identifier> playerGainedPerk = PerkUtils.getPlayerPerks(this.minecraft.player, this.perkTree.getID());
        if (playerGainedPerk != null && playerGainedPerk.contains(this.nowSelectNode.perkID)) {
            return false;
        }
        if (this.nowSelectNode.dependentPerkIDs != null && !this.nowSelectNode.dependentPerkIDs.isEmpty()) {
            if (playerGainedPerk == null) return false;
            for (Identifier dependentPerkID : this.nowSelectNode.dependentPerkIDs) if (!playerGainedPerk.contains(dependentPerkID)) return false;
        }
        int requireXp = this.minecraft.player.getAbilities().instabuild ? 0 : perkXpCostMap.getOrDefault(this.nowSelectNode.perkID, 0);
        if (this.minecraft.player.totalExperience < requireXp) {
            return false;
        }
        return true;
    }

    public void onNodeSelect() {
        // 1.21.11: Entity 不再实现 CommandSource，客户端 LocalPlayer 没有 sendSystemMessage，要用 displayClientMessage
        try {
            Minecraft.getInstance().player.displayClientMessage(Component.literal("Node Selected: " + this.nowSelectNode.perkID.toString()), false);
            Minecraft.getInstance().player.displayClientMessage(Component.literal("Can Gained (Cache): " + this.perkAvailableMap.getOrDefault(this.nowSelectNode.perkID, true)), false);
        } catch (Exception e) {
            Minecraft.getInstance().player.displayClientMessage(Component.literal("No Node Selected"), false);
        }
        if (this.nowSelectNode != null) {
            this.PerkNameWidget.setMessage(RegPerks.getPerkName(this.nowSelectNode.perkID));
            this.PerkDescWidget.reloadText(RegPerks.getPerkDescription(this.nowSelectNode.perkID));
            this.setPerkXpCostText(String.valueOf(perkXpCostMap.getOrDefault(this.nowSelectNode.perkID, 0)));
            this.AcquirePerkButton.active = this.isNowPerkCanGain();
        } else {
            this.PerkNameWidget.setMessage(Component.literal(""));
            this.PerkDescWidget.reloadText(Component.literal(""));
            this.setPerkXpCostText("");
            this.AcquirePerkButton.active = false;
        }
        ModPacketsS2C.sendRequestPerkAvailability();
    }
}
