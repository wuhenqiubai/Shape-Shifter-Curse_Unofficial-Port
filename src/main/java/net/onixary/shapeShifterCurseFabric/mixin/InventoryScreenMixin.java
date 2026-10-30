package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.onixary.shapeShifterCurseFabric.util.ClientUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {
    @Unique
    private static float prevBodyYaw;

    @Inject(method = "extractEntityInInventoryFollowsMouse(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V", at = @At("HEAD"))
    private static void drawEntityHead(GuiGraphicsExtractor context, int x1, int y1, int x2, int y2, int size,
                                       float yOffset, float mouseX, float mouseY, LivingEntity entity, CallbackInfo ci) {
        ClientUtils.isOpenInventoryScreen = true;
        prevBodyYaw = entity.yBodyRotO;
        float p = (float)Math.atan((((x1 + x2) / 2.0F) - mouseX) / 40.0F);
        entity.yBodyRotO = 180.0F + p * 20.0F;
    }

    @Inject(method = "extractEntityInInventoryFollowsMouse(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V", at = @At("RETURN"))
    private static void drawEntityTail(GuiGraphicsExtractor context, int x1, int y1, int x2, int y2, int size,
                                       float yOffset, float mouseX, float mouseY, LivingEntity entity, CallbackInfo ci) {
        ClientUtils.isOpenInventoryScreen = false;
        entity.yBodyRotO = prevBodyYaw;
    }
}
