package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static net.onixary.shapeShifterCurseFabric.util.CustomEdibleUtils.getPowerFoodComponent;

@Mixin(Item.class)
public abstract class CustomEdibleItemMixin {
    /*
     * 1.21.1 食物 = 数据组件 DataComponents.FOOD（Item.isFood()/getFoodComponent() 已删除）。
     * 唯一的"食物判定"seam 是 itemStack.get(DataComponents.FOOD)（编译为 DataComponentHolder.get）。
     * 这里把它替换成 SSC 的自定义 FoodProperties（非 null），use/finishUsingItem 里的 foodProperties != null 判定自然成立，
     * 再走进 LivingEntity.eat（addEatEffect 应用 mob 效果、Player.eat→FoodData.eat 恢复饥饿/饱和度）。
     */

    // Item.use 里 itemStack.get(DataComponents.FOOD) 处：让自定义可食物品被识别为食物
    @ModifyExpressionValue(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object use$getFoodProperties(Object original, Level world, Player user, InteractionHand hand) {
        if (original instanceof FoodProperties) {
            FoodProperties fc = getPowerFoodComponent(user, user.getItemInHand(hand));
            return fc != null ? fc : original;
        }
        return original;
    }

    // Item.finishUsingItem 里 itemStack.get(DataComponents.FOOD) 处：食用完成后用自定义 FoodProperties
    @ModifyExpressionValue(method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object finishUsing$getFoodProperties(Object original, ItemStack stack, Level world, LivingEntity user) {
        if (original instanceof FoodProperties) {
            if (user instanceof Player player) {
                FoodProperties fc = getPowerFoodComponent(player, stack);
                return fc != null ? fc : original;
            }
        }
        return original;
    }
}
