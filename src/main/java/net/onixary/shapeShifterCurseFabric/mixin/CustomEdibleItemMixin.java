package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.onixary.shapeShifterCurseFabric.util.CustomEdibleUtils.getPowerFoodComponent;

/**
 * 自定义可食物品（1.21.1 食物 = 数据组件 {@code DataComponents.FOOD}）。
 *
 * <p><b>⚠️ 注入点必须用 {@code @At("HEAD")}，不要改回 {@code @ModifyExpressionValue} 去匹配
 * {@code Item.use} 内部的 {@code ItemStack.get(DataComponents.FOOD)} 调用。</b>
 * NeoForge 给 {@code Item} 打了 patch，那个内部调用点在 NeoForge 字节码里对不上，Sinytra Connector 的
 * {@code MixinTransformSafeguard} 会把整个 jar 判定为「has failing mixins」并<b>拒绝加载 mod</b>
 * （报错形如 {@code CustomEdibleItemMixin#use$getFoodProperties}）。
 * HEAD 注入不依赖方法体内部结构，注入点恒存在，Fabric / Connector 双端都安全。</p>
 */
@Mixin(Item.class)
public abstract class CustomEdibleItemMixin {

    /** Item.use 入口：命中自定义可食物品时接管，走原版的「开始食用」流程。 */
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void ssc$use(Level level, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = user.getItemInHand(hand);
        FoodProperties food = getPowerFoodComponent(user, stack);
        if (food == null) {
            return; // 非自定义食物：完全走原版
        }
        if (user.canEat(food.canAlwaysEat())) {
            user.startUsingItem(hand);
            cir.setReturnValue(InteractionResultHolder.consume(stack));
        } else {
            cir.setReturnValue(InteractionResultHolder.fail(stack));
        }
    }

    /** Item.finishUsingItem 入口：食用完成时用自定义 FoodProperties 结算（饱食度/饱和度/效果）。 */
    @Inject(method = "finishUsingItem", at = @At("HEAD"), cancellable = true)
    private void ssc$finishUsing(ItemStack stack, Level level, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (user instanceof Player player) {
            FoodProperties food = getPowerFoodComponent(player, stack);
            if (food != null) {
                cir.setReturnValue(player.eat(level, stack, food));
            }
        }
    }
}
