package net.onixary.shapeShifterCurseFabric.render.tech;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class ItemSubmitUtils {
    private static final ItemStackRenderState STATE = new ItemStackRenderState();

    private ItemSubmitUtils() {
    }

    public static void submitItem(LivingEntity entity, ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        if (stack.isEmpty()) {
            return;
        }
        STATE.clear();
        Minecraft.getInstance().getItemModelResolver().updateForLiving(STATE, stack, displayContext, entity);
        STATE.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
    }
}
