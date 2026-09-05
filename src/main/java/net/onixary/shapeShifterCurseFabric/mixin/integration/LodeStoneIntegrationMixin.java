package net.onixary.shapeShifterCurseFabric.mixin.integration;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.onixary.shapeShifterCurseFabric.util.CustomEdibleUtils.getPowerFoodComponent;

@Mixin(LivingEntity.class)
public abstract class LodeStoneIntegrationMixin {
    @Shadow
    protected ItemStack useItem;

    @Shadow
    protected int useItemRemaining;

    @Shadow
    protected abstract void setLivingEntityFlag(int mask, boolean value);

    // 其实应该是Lodestone的ci.cancel的问题 不过我这边还是能修的(也就是今天心情好一些 按理说直接在Fabric.mod.json写一个不兼容就行 Lodestone的直接ci.cancel会和大量Mod冲突)
    // 调了半天 现在心情不好了 感觉Lodestone就没考虑别人的Mixin 直接ci.cancel还把客户端操作给屏蔽了 还有大量位置直接覆盖原始值 还不用mixinExtra读取原始值

    @Inject(method = "startUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration(Lnet/minecraft/world/entity/LivingEntity;)I"), order = 900)
    private void setCurrentHand$getMaxUseTimeA(InteractionHand interactionHand, CallbackInfo ci) {
        if ((Object)this instanceof Player playerEntity) {
            FoodProperties fc = getPowerFoodComponent(playerEntity, useItem);
            if (fc == null) {
                return;
            }
            this.useItemRemaining = fc.eatSeconds() < 1.0f ? 16 : 32;
            if (!playerEntity.level().isClientSide) {
                this.setLivingEntityFlag(1, true);
                this.setLivingEntityFlag(2, interactionHand == InteractionHand.OFF_HAND);
                playerEntity.gameEvent(GameEvent.ITEM_INTERACT_START);
            }
        }
        return;
    }
}
