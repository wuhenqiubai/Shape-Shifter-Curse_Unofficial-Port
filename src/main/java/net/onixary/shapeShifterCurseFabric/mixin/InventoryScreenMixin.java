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
    private static void drawEntityHead(GuiGraphicsExtractor context, int x, int y, int size, int mouseX, int mouseY, float f, float g, float h, LivingEntity entity, CallbackInfo ci) {
        ClientUtils.isOpenInventoryScreen = true;
        prevBodyYaw = entity.yBodyRotO;
        float angle = (float)Math.atan((double)(mouseX / 40.0F));
        entity.yBodyRotO = 180.0F + angle * 20.0F;
        return;
    }

    @Inject(method = "extractEntityInInventoryFollowsMouse(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V", at = @At("RETURN"))
    private static void drawEntityTail(GuiGraphicsExtractor context, int x, int y, int size, int mouseX, int mouseY, float f, float g, float h, LivingEntity entity, CallbackInfo ci) {
        ClientUtils.isOpenInventoryScreen = false;
        entity.yBodyRotO = prevBodyYaw;
        return;
    }

    @Inject(method = "extractEntityInInventoryFollowsMouse(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V", at = @At("HEAD"))
    private static void drawEntityHead2(GuiGraphicsExtractor context, int x1, int y1, int x2, int y2, int size, float f, float mouseX, float mouseY, LivingEntity entity, CallbackInfo ci) {
        ClientUtils.isOpenInventoryScreen = true;
        return;
    }

    @Inject(method = "extractEntityInInventoryFollowsMouse(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V", at = @At("RETURN"))
    private static void drawEntityTail2(GuiGraphicsExtractor context, int x1, int y1, int x2, int y2, int size, float f, float mouseX, float mouseY, LivingEntity entity, CallbackInfo ci) {
        ClientUtils.isOpenInventoryScreen = false;
        return;
    }
}