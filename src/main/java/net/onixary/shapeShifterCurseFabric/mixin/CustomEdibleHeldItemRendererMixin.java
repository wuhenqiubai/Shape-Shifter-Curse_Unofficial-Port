package net.onixary.shapeShifterCurseFabric.mixin;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static net.onixary.shapeShifterCurseFabric.util.CustomEdibleUtils.getPowerFoodComponent;

@Mixin(ItemInHandRenderer.class)
public class CustomEdibleHeldItemRendererMixin {
    @Shadow
    private Minecraft minecraft;

    @ModifyExpressionValue(method = "renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V", at = @At( value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/ItemUseAnimation;"))
    private ItemUseAnimation renderFirstPersonItem$getUseAction(ItemUseAnimation original, AbstractClientPlayer player, float tickDelta, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack matrices, SubmitNodeCollector submitNodeCollector, int light) {
        return getPowerFoodComponent(player, item) != null ? ItemUseAnimation.EAT : original;
    }

    @ModifyExpressionValue(method = "applyEatTransform(Lcom/mojang/blaze3d/vertex/PoseStack;FLnet/minecraft/world/entity/HumanoidArm;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)V", at = @At( value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int applyEatOrDrinkTransformation$getMaxUseTime(int original, PoseStack matrices, float tickDelta, HumanoidArm arm, ItemStack stack) {
        FoodProperties fc = getPowerFoodComponent(minecraft.player, stack);
        if (fc == null) {
            return original;
        }
        return 32;
    }
}