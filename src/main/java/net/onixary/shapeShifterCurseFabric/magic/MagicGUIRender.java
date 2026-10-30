package net.onixary.shapeShifterCurseFabric.magic;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.client.ShapeShifterCurseFabricClient;
import net.onixary.shapeShifterCurseFabric.util.UIPositionUtils;
import net.onixary.shapeShifterCurseFabric.util.util.ISprite;

@Environment(EnvType.CLIENT)
public class MagicGUIRender {
    private static final Minecraft mc = Minecraft.getInstance();

    private static final int CENTER_SIZE = 24;
    private static final int SIDE_SIZE = 16;

    private static final int CENTER_X = (CENTER_SIZE + SIDE_SIZE) / 2;
    private static final int CENTER_Y = 0;
    private static final int LEFT_X = 6;
    private static final int LEFT_Y = (CENTER_SIZE - SIDE_SIZE) / 2;
    private static final int RIGHT_X = 2 * CENTER_SIZE - 6;
    private static final int RIGHT_Y = LEFT_Y;

    private static final int COOLDOWN_COLOR = 0xA0000000;

    // XuHaoNan:
    // 这AI写UI的水平就是比我强(我扣的扣半天 AI写的我只用审查一下代码 根据架构修一下性能) 我还得扣半天MatrixStack 结果可以用draw画 不会出现拉伸问题 学到了
    public static void render(GuiGraphics context, float tickDelta) {
        Player player = mc.player;
        if (player == null) return;
        if (mc.options.hideGui) return;
        if (!MagicUtils.shouldShowMagicUI(player)) return;

        Tuple<Integer, Integer> pos = UIPositionUtils.getCorrectPosition(
                ShapeShifterCurseFabric.clientConfig.magicGUIPosType,
                ShapeShifterCurseFabric.clientConfig.magicGUIPosOffsetX,
                ShapeShifterCurseFabric.clientConfig.magicGUIPosOffsetY);
        int ax = pos.getA();
        int ay = pos.getB();

        IMagic[] magics = MagicUtils.getPlayerMagic(player);
        int n = MagicUtils.MAGIC_SLOT_COUNT;
        int centerIdx = MagicUtils.nowSelectMagicIndex;
        int leftIdx = (centerIdx - 1 + n) % n;
        int rightIdx = (centerIdx + 1) % n;

        renderSlot(context, magics, leftIdx, ax + LEFT_X, ay + LEFT_Y, SIDE_SIZE, tickDelta);
        renderSlot(context, magics, rightIdx, ax + RIGHT_X, ay + RIGHT_Y, SIDE_SIZE, tickDelta);
        renderSlot(context, magics, centerIdx, ax + CENTER_X, ay + CENTER_Y, CENTER_SIZE, tickDelta);
    }

    private static void renderSlot(GuiGraphics context, IMagic[] magics, int index, int x, int y, int size, float tickDelta) {
        if (index < 0 || index >= magics.length) return;
        IMagic magic = magics[index];
        if (magic == null) return;
        ISprite sprite = index == MagicUtils.nowSelectMagicIndex ? magic.getSprite() : magic.getUnselectedSprite();
        sprite.draw(context, x, x + size, y, y + size, 0, sprite.getTextureWidth(), sprite.getTextureHeight(), 0, 0);
        float cooldown;
        try {
            cooldown = getCooldownPercent(magic, tickDelta);
        } catch (Exception e) {
            cooldown = 1.0f;
        }
        if (cooldown < 1.0f) {
            if (cooldown < 0.0f) cooldown = 0.0f;
            int coverH = (int) Math.ceil((1.0f - cooldown) * size);
            context.fill(x, y + size - coverH, x + size, y + size, COOLDOWN_COLOR);
        }
    }

    // 一样 拓展后续实现
    public static float getCooldownPercent(IMagic magic, float tickDelta) {
        return 1.0f;
    }

    // 有点复杂 不太适合放ShapeShifterCurseFabricClient里面 但又不够但拉一个Class
    private static boolean lastPASPressed = false;
    private static boolean lastNASPressed = false;
    private static boolean lastASPressed = false;
    private static double scrollCounter = 0.0f;
    private static final double scrollStep = -1.0f;

    public static void onClientTick() {
        if (ShapeShifterCurseFabricClient.prevActiveSkillKeybind.isDown() && !lastPASPressed) {
            MagicUtils.nowSelectMagicIndex = (MagicUtils.nowSelectMagicIndex - 1 + MagicUtils.MAGIC_SLOT_COUNT) % MagicUtils.MAGIC_SLOT_COUNT;
        }
        if (ShapeShifterCurseFabricClient.nextActiveSkillKeybind.isDown() && !lastNASPressed) {
            MagicUtils.nowSelectMagicIndex = (MagicUtils.nowSelectMagicIndex + 1) % MagicUtils.MAGIC_SLOT_COUNT;
        }
        if (ShapeShifterCurseFabricClient.activeSkillKeybind.isDown() && mc.player != null) {
            MagicUtils.invokeNowMagic_CLIENT(mc.player, lastASPressed);
        }
        lastPASPressed = ShapeShifterCurseFabricClient.prevActiveSkillKeybind.isDown();
        lastNASPressed = ShapeShifterCurseFabricClient.nextActiveSkillKeybind.isDown();
        lastASPressed = ShapeShifterCurseFabricClient.activeSkillKeybind.isDown();
    }

    public static void onMouseScroll(double amount) {
        scrollCounter += amount;
        int step = 0;
        if (scrollCounter >= scrollStep) {
            step = (int) Math.floor(scrollCounter / scrollStep);
        } else if (scrollCounter <= -scrollStep) {
            step = (int) Math.ceil(scrollCounter / scrollStep);
        }
        if (step != 0) {
            scrollCounter -= step * scrollStep;
            step = step % MagicUtils.MAGIC_SLOT_COUNT;
            MagicUtils.nowSelectMagicIndex = (MagicUtils.nowSelectMagicIndex + step + MagicUtils.MAGIC_SLOT_COUNT) % MagicUtils.MAGIC_SLOT_COUNT;
        }
    }
}
