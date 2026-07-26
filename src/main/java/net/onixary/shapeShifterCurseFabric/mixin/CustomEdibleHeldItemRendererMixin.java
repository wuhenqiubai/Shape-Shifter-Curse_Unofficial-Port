package net.onixary.shapeShifterCurseFabric.mixin;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static net.onixary.shapeShifterCurseFabric.util.CustomEdibleUtils.getPowerFoodComponent;

@Mixin(ItemInHandRenderer.class)
public class CustomEdibleHeldItemRendererMixin {
    @Shadow
    private Minecraft minecraft;

    @ModifyExpressionValue(method = "renderArmWithItem", at = @At( value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/UseAnim;"))
    private UseAnim renderFirstPersonItem$getUseAction(UseAnim original, AbstractClientPlayer player, float tickDelta, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
        return getPowerFoodComponent(player, item) != null ? UseAnim.EAT : original;
    }

    @ModifyExpressionValue(method = "applyEatTransform", at = @At( value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int applyEatOrDrinkTransformation$getMaxUseTime(int original, PoseStack matrices, float tickDelta, HumanoidArm arm, ItemStack stack) {
        FoodProperties fc = getPowerFoodComponent(minecraft.player, stack);
        if (fc == null) {
            return original;
        }
        return Mth.ceil(fc.eatSeconds() * 20.0F);
    }
}
