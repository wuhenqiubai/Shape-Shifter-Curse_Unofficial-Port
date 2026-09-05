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

import java.util.List;

@Mixin(SmithingMenu.class)
public class SmithingScreenHandlerMixin {
    @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
    public void onTake(Player player, ItemStack itemStack, CallbackInfo ci) {
        SmithingMenu realThis = (SmithingMenu) (Object) this;
        SmithingRecipeInput recipeInput = new SmithingRecipeInput(realThis.inputSlots.getItem(0), realThis.inputSlots.getItem(1), realThis.inputSlots.getItem(2));
        List<RecipeHolder<SmithingRecipe>> list = realThis.level.getRecipeManager().getRecipesFor(RecipeType.SMITHING, recipeInput, realThis.level);
        if (list.isEmpty()) {
            return;
        }
        SmithingRecipe recipe = list.getFirst().value();
        if (recipe instanceof ISmithingRecipeEX iSmithingRecipeEX && iSmithingRecipeEX.overrideVanillaOnTakeOutput()) {
            // 在 vanilla onTake 的 shrinkStackInSlot(0..2) 之前运行，onTakeOutput 成为唯一消费者：
            // 核心配方 branch 只 damage core + shrink base/addition（不 shrink slot0=核心），避免核心被 vanilla 当作模板吞掉。
            iSmithingRecipeEX.onTakeOutput(realThis, player, itemStack);
            // 复现被 ci.cancel() 跳过的原版 onTake 部分 housekeeping（onCraftedBy 合成统计 / levelEvent 音效粒子）。
            // 注：原版 awardUsedRecipes 需 resultSlots（父类 ItemCombinerMenu 的 protected 字段，@Shadow 解析受限），此处省略，
            // 仅影响"配方的使用记录/成就"，不影响产出与消耗。
            itemStack.onCraftedBy(player.level(), player, itemStack.getCount());
            realThis.access.execute((world, pos) -> world.levelEvent(1044, pos, 0));
            ci.cancel();
        }
    }
}
