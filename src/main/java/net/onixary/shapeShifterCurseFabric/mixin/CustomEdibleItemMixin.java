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

/**
 * 自定义可食物品（由 CustomEdiblePower 让原本不可食用的物品变成可食用）。
 *
 * <p><b>⚠️ 注入点必须用 {@code @At("HEAD")}，不要改回 {@code @ModifyExpressionValue} 去匹配
 * {@code Item.use} / {@code Item.finishUsingItem} 内部的组件读取调用。</b>
 * NeoForge 给 {@code Item} 打了 patch，那些内部调用点在 NeoForge 字节码里对不上，Sinytra Connector 的
 * {@code MixinTransformSafeguard} 会把整个 jar 判定为「has failing mixins」并<b>拒绝加载 mod</b>
 * （报错形如 {@code CustomEdibleItemMixin#use$getFoodProperties}）。
 * HEAD 注入不依赖方法体内部结构，注入点恒存在，Fabric / Connector 双端都安全。</p>
 *
 * <p><b>1.21.11 的食用流程已重构</b>：{@code Player.eat(Level, ItemStack, FoodProperties)} 被移除，
 * 食用完全由数据组件驱动 —— {@code Item.use} 读 {@code DataComponents.CONSUMABLE}、
 * {@code Item.finishUsingItem} 走 {@code Consumable.onConsume(...)}，而饱食度/饱和度
 * 来自「栈上那个实现了 {@code ConsumableListener} 的 {@code FoodProperties} 组件」。
 * 自定义可食物品本身既无 FOOD 也无 CONSUMABLE 组件，故两个注入点都要在这里接管。</p>
 */
@Mixin(Item.class)
public abstract class CustomEdibleItemMixin {

    /** Item.use 入口：命中自定义可食物品时接管，走原版的「开始食用」流程。 */
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void ssc$use(Level level, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = user.getItemInHand(hand);
        FoodProperties food = getPowerFoodComponent(user, stack);
        if (food == null) {
            return; // 非自定义食物：完全走原版
        }
        if (user.canEat(food.canAlwaysEat())) {
            user.startUsingItem(hand);
            cir.setReturnValue(InteractionResult.CONSUME);
        } else {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    /**
     * Item.finishUsingItem 入口：食用完成时用自定义 FoodProperties 结算（饱食度/饱和度/音效/触发器等）。
     *
     * <p>做法：临时把自定义 FoodProperties 装到栈上，借用原版 {@code Consumable.onConsume} 完整走一遍
     * （粒子音效 / awardStat / CONSUME_ITEM / on_consume_effects / gameEvent / consume(1)），
     * 结束后再还原 —— 否则会在物品上残留 FOOD 组件，把它变成真正的食物。</p>
     */
    @Inject(method = "finishUsingItem", at = @At("HEAD"), cancellable = true)
    private void ssc$finishUsing(ItemStack stack, Level level, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (!(user instanceof Player player)) {
            return;
        }
        FoodProperties food = getPowerFoodComponent(player, stack);
        if (food == null) {
            return;
        }
        if (stack.isEmpty()) {
            return; // 理论上到不了；ItemStack.set 对空栈会抛 IllegalStateException
        }
        Consumable consumable = stack.get(DataComponents.CONSUMABLE);
        if (consumable == null) {
            consumable = Consumables.DEFAULT_FOOD;
        }
        FoodProperties previous = stack.get(DataComponents.FOOD);
        stack.set(DataComponents.FOOD, food);
        try {
            cir.setReturnValue(consumable.onConsume(level, user, stack));
        } finally {
            // consume(1) 之后栈可能已被缩减甚至清空，空栈不能再改组件
            if (!stack.isEmpty()) {
                if (previous == null) {
                    stack.remove(DataComponents.FOOD);
                } else {
                    stack.set(DataComponents.FOOD, previous);
                }
            }
        }
    }
}
