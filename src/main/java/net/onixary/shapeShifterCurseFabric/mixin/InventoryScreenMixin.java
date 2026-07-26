package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.LivingEntity;
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

    @Inject(method = "drawEntity(Lnet/minecraft/client/gui/DrawContext;IIIIIFFFLnet/minecraft/entity/LivingEntity;)V", at = @At("HEAD"))
    private static void drawEntityHead(DrawContext context, int x, int y, int size, int mouseX, int mouseY, float f, float g, float h, LivingEntity entity, CallbackInfo ci) {
        ClientUtils.isOpenInventoryScreen = true;
        prevBodyYaw = entity.prevBodyYaw;
        float angle = (float)Math.atan((double)(mouseX / 40.0F));
        entity.prevBodyYaw = 180.0F + angle * 20.0F;
        return;
    }

    @Inject(method = "drawEntity(Lnet/minecraft/client/gui/DrawContext;IIIIIFFFLnet/minecraft/entity/LivingEntity;)V", at = @At("RETURN"))
    private static void drawEntityTail(DrawContext context, int x, int y, int size, int mouseX, int mouseY, float f, float g, float h, LivingEntity entity, CallbackInfo ci) {
        ClientUtils.isOpenInventoryScreen = false;
        entity.prevBodyYaw = prevBodyYaw;
        return;
    }

    @Inject(method = "drawEntity(Lnet/minecraft/client/gui/DrawContext;IIIIIFFFLnet/minecraft/entity/LivingEntity;)V", at = @At("HEAD"))
    private static void drawEntityHead2(DrawContext context, int x1, int y1, int x2, int y2, int size, float f, float mouseX, float mouseY, LivingEntity entity, CallbackInfo ci) {
        ClientUtils.isOpenInventoryScreen = true;
        return;
    }

    @Inject(method = "drawEntity(Lnet/minecraft/client/gui/DrawContext;IIIIIFFFLnet/minecraft/entity/LivingEntity;)V", at = @At("RETURN"))
    private static void drawEntityTail2(DrawContext context, int x1, int y1, int x2, int y2, int size, float f, float mouseX, float mouseY, LivingEntity entity, CallbackInfo ci) {
        ClientUtils.isOpenInventoryScreen = false;
        return;
    }
}