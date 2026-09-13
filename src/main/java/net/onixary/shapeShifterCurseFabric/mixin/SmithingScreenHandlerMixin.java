package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.onixary.shapeShifterCurseFabric.recipes.ISmithingRecipeEX;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * 超级塑形核心的锻造台配方（issue #22 的另一半，与 {@code SuperMorphScaleCore} 的耐久修复配套）。
 *
 * <p><b>注入点是 {@code @At("HEAD")}，不要改回 {@code ContainerLevelAccess.execute} 那个 INVOKE。</b>
 * 在 {@code SmithingMenu.onTake} 里，{@code access.execute(...)} 位于三次 {@code shrinkStackInSlot(0..2)}
 * <b>之后</b>；挂在那里意味着核心（slot 0）已被 vanilla 当作模板消耗掉，才轮到
 * {@code onTakeOutput}——这正是「物品耐久与实际不符」的成因。
 * 移到 HEAD 后本注入在 vanilla 消耗之前运行，{@code onTakeOutput} 成为唯一消费者
 * （核心配方只 damage core + shrink base/addition，不 shrink slot 0）。
 * 附带好处：HEAD 注入不依赖方法体内部结构，Sinytra Connector 下也不会因内部调用点对不上而被拒绝加载。</p>
 */
@Mixin(SmithingMenu.class)
public class SmithingScreenHandlerMixin {
    @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
    public void onTake(Player player, ItemStack itemStack, CallbackInfo ci) {
        SmithingMenu realThis = (SmithingMenu) (Object) this;
        SmithingRecipeInput recipeInput = new SmithingRecipeInput(realThis.inputSlots.getItem(0), realThis.inputSlots.getItem(1), realThis.inputSlots.getItem(2));
        Optional<RecipeHolder<SmithingRecipe>> recipeOptional = realThis.level.getServer().getRecipeManager().getRecipeFor(RecipeType.SMITHING, recipeInput, realThis.level);
        if (recipeOptional.isEmpty()) {
            return;
        }
        SmithingRecipe recipe = recipeOptional.get().value();
        if (recipe instanceof ISmithingRecipeEX iSmithingRecipeEX && iSmithingRecipeEX.overrideVanillaOnTakeOutput()) {
            // 在 vanilla onTake 的 shrinkStackInSlot(0..2) 之前运行，onTakeOutput 成为唯一消费者：
            // 核心配方 branch 只 damage core + shrink base/addition（不 shrink slot0=核心），避免核心被 vanilla 当作模板吞掉。
            iSmithingRecipeEX.onTakeOutput(realThis, player, itemStack);
            // 复现被 ci.cancel() 跳过的原版 onTake 部分 housekeeping（onCraftedBy 合成统计 / levelEvent 音效粒子）。
            // 注：原版 awardUsedRecipes 需 resultSlots（父类 ItemCombinerMenu 的 protected 字段，@Shadow 解析受限），此处省略，
            // 仅影响"配方的使用记录/成就"，不影响产出与消耗。
            // 1.21.11: ItemStack.onCraftedBy 去掉了 Level 参数（1.21.1 是 (Level, Player, int)）。
            itemStack.onCraftedBy(player, itemStack.getCount());
            realThis.access.execute((world, pos) -> world.levelEvent(1044, pos, 0));
            ci.cancel();
        }
    }
}
