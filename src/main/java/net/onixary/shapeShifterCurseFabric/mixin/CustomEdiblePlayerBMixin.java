package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

/**
 * TODO: 1.21.11 移除 Player.eat(Level, ItemStack, FoodProperties)，此 mixin 的三个注入点（eat 的
 * @Inject/@ModifyVariable/@Redirect）全部失效。SSC 自定义食物的营养应用已由 CustomEdibleItemMixin
 * 的 finishUsingItem 注入处理；ModifyFoodPower 修改营养/统计/成就逻辑需在 1.21.11 的
 * Item.finishUsingItem 流程中重新接入。
 */
@Mixin(Player.class)
public abstract class CustomEdiblePlayerBMixin {
}
