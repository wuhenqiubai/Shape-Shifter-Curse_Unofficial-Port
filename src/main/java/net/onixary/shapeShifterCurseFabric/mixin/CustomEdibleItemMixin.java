package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.onixary.shapeShifterCurseFabric.util.CustomEdibleUtils.getPowerFoodComponent;

@Mixin(Item.class)
public abstract class CustomEdibleItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void ssc$use(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = player.getItemInHand(hand);
        FoodProperties food = getPowerFoodComponent(player, stack);
        if (food == null) {
            return;
        }
        if (player.canEat(food.canAlwaysEat())) {
            player.startUsingItem(hand);
            cir.setReturnValue(InteractionResult.CONSUME);
        } else {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method = "finishUsingItem", at = @At("HEAD"), cancellable = true)
    private void ssc$finishUsing(ItemStack itemStack, Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        if (!(entity instanceof Player player)) {
            return;
        }
        FoodProperties food = getPowerFoodComponent(player, itemStack);
        if (food == null) {
            return;
        }
        if (itemStack.isEmpty()) {
            return;
        }
        Consumable consumable = itemStack.get(DataComponents.CONSUMABLE);
        if (consumable == null) {
            consumable = Consumables.DEFAULT_FOOD;
        }
        FoodProperties previous = itemStack.get(DataComponents.FOOD);
        itemStack.set(DataComponents.FOOD, food);
        try {
            cir.setReturnValue(consumable.onConsume(level, entity, itemStack));
        } finally {
            if (!itemStack.isEmpty()) {
                if (previous == null) {
                    itemStack.remove(DataComponents.FOOD);
                } else {
                    itemStack.set(DataComponents.FOOD, previous);
                }
            }
        }
    }
}
