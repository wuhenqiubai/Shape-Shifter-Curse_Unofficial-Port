package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ElytraFlightPower;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ElytraLayer.class)
public class ElytraFeatureRendererMixin {
    @Unique
    private LivingEntity livingEntity;

    // NeoForge/Connector 兼容：主版注入点也改用 getItemBySlot（NeoForge 下 is(ELYTRA) 被替换为 shouldRender() 字段比较而失效）
    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack modifyEquippedStackToElytra(LivingEntity instance, EquipmentSlot slot, Operation<ItemStack> original, @Local(argsOnly = true) LivingEntity livingEntity) {
        this.livingEntity = livingEntity;
        if(PowerHolderComponent.getPowers(livingEntity, ElytraFlightPower.class).stream().anyMatch(ElytraFlightPower::shouldRenderElytra) && !livingEntity.isInvisible()) {
            return new ItemStack(Items.ELYTRA);
        }
        return original.call(instance, slot);
    }

    @ModifyArg(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;armorCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
    private ResourceLocation setTexture(ResourceLocation identifier) {
        for (ElytraFlightPower power : PowerHolderComponent.getPowers(this.livingEntity, ElytraFlightPower.class)) {
            if (power.getTextureLocation() != null) {
                return power.getTextureLocation();
            }
        }
        return identifier;
    }
}