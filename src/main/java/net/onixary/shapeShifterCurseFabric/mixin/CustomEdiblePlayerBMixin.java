package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

/**
 * [部分已迁移] 1.21.11 移除了 Player.eat(Level, ItemStack, FoodProperties)，此 mixin 的三个注入点
 * （eat 的 @Inject/@ModifyVariable/@Redirect）全部失效，本类现为空壳。
 *
 * 已由别处承担（勿再重复实现）：SSC 自定义食物的营养应用 →
 * CustomEdibleItemMixin.finishUsingItem 注入。
 *
 * TODO(1.21.11 未迁移)：ModifyFoodPower 对营养/统计/成就的修改，需在 1.21.11 的
 * Item.finishUsingItem 流程中重新接入。
 */
@Mixin(Player.class)
public abstract class CustomEdiblePlayerBMixin {
}
