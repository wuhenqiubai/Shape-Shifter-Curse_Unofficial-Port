package net.onixary.shapeShifterCurseFabric.render.tech;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.util.UIPositionUtils;

import java.util.ArrayList;
import java.util.List;

public class ItemStorePowerRender {
    private static final Identifier WIDGETS_TEXTURE = Identifier.parse("textures/gui/sprites/hud/hotbar_offhand_left.png");

    public static interface itemStorePowerRenderInterface {
        public int getSlot();
        public ItemStack getStack();
        public default float getBobbingAnimationTime() {
            return 0;
        }
    }

    private static final Minecraft mc = Minecraft.getInstance();
    private static final List<itemStorePowerRenderInterface> tempPower = new ArrayList<itemStorePowerRenderInterface>();
    private static int timer = 0;
    private static final int MaxSlot = 12;
    private static final int SlotPerRow = 4;
    private static int NowCol = 0;
    private static int NowRow = 0;

    static {
        for (int i = 0; i < MaxSlot; i++) {
            tempPower.add(null);
        }
    }

    private static void timerTick() {
        if (mc.player == null) {
            return;
        }
        if (timer > 60) {
            timer = 0;
            NowCol = 0;
            NowRow = 0;
            for (int i = 0; i < MaxSlot; i++) {
                tempPower.set(i, null);
            }
            for(Power power : PowerHolderComponent.KEY.get(mc.player).getPowers()) {
                if(itemStorePowerRenderInterface.class.isAssignableFrom(power.getClass()) && power.isActive()) {
                    itemStorePowerRenderInterface trueR = (itemStorePowerRenderInterface) power;
                    tempPower.set(trueR.getSlot(), trueR);
                    NowCol = Math.max(NowCol, (trueR.getSlot() % SlotPerRow) + 1);
                    NowRow = Math.max(NowRow, (trueR.getSlot() / SlotPerRow) + 1);
                }
            }
        }
        timer++;
    }

    private static void renderSlot(GuiGraphics context, float tickDelta, itemStorePowerRenderInterface power) {
        Tuple<Integer, Integer> SlotBegin = UIPositionUtils.getCorrectPosition(ShapeShifterCurseFabric.clientConfig.itemStorePowerPosType, ShapeShifterCurseFabric.clientConfig.itemStorePowerPosOffsetX - (NowCol * 20), ShapeShifterCurseFabric.clientConfig.itemStorePowerPosOffsetY - (NowRow * 20));
        int SlotX = power.getSlot() % SlotPerRow;
        int SlotY = power.getSlot() / SlotPerRow;
        int SlotXFinal = SlotBegin.getA() + SlotX;
        int SlotYFinal = SlotBegin.getB() + SlotY;
        // 2D 变换栈不支持 z 平移，去除 translate(0,0,-90)
        context.pose().pushMatrix();
        context.blit(WIDGETS_TEXTURE, SlotXFinal - 2, SlotYFinal - 4, 22, 22, 0, 1, 29, 24);
        context.pose().popMatrix();
        ItemStack stack = power.getStack();
        if (stack.isEmpty()) {
            return;
        }
        float g = power.getBobbingAnimationTime() - tickDelta;
        if (g > 0.0f) {
            float h = 1.0f + g / 5.0f;
            context.pose().pushMatrix();
            context.pose().translate(SlotXFinal + 8, SlotYFinal + 12);
            context.pose().scale(1.0f / h, (h + 1.0f) / 2.0f);
            context.pose().translate(-(SlotXFinal + 8), -(SlotYFinal + 12));
        }
        context.renderItem(mc.player, stack, SlotXFinal, SlotYFinal, power.getSlot());
        if (g > 0.0f) {
            context.pose().popMatrix();
        }
        context.renderItemDecorations(mc.font, stack, SlotXFinal, SlotYFinal);
    }

    public static void render(GuiGraphics context, float tickDelta) {
        timerTick();
        if (!mc.options.hideGui) {
            // RenderSystem.enableBlend()/disableBlend() 已移除，RenderPipeline 自带渲染状态
            for (itemStorePowerRenderInterface power : tempPower) {
                if (power != null) {
                    renderSlot(context, tickDelta, power);
                }
            }
        }
    }
}