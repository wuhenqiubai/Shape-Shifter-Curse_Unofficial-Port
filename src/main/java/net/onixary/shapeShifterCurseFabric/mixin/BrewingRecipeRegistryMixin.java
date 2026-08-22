package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import net.onixary.shapeShifterCurseFabric.recipes.BrewingRecipeUtils;
import net.onixary.shapeShifterCurseFabric.status_effects.CTPUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionBrewing.class)
public class BrewingRecipeRegistryMixin {
	@Unique
    private static void setTargetForm(ItemStack stack, net.minecraft.resources.ResourceLocation formID) {
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CTPUtils.setCTPFormIDToNBT(nbt, formID);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
    }

    // 材料格校验（BrewingStandBlockEntity.canPlaceItem / isBrewable 走 PotionBrewing.isIngredient）。
    // 数据包 dynamic_brewing_recipes 不进原版 potionMixes/containerMixes → 原版 isIngredient 不认识其材料。
    // 改为 HEAD 注入：命中数据包配方材料即视为合法（材料格放得下），未命中走原版。
    @Inject(method = "isIngredient", at = @At("HEAD"), cancellable = true)
    private void ssc$isIngredient(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        for (BrewingRecipeUtils.DynamicRecipe<Potion> recipe : BrewingRecipeUtils.getPotionRecipes()) {
            if (recipe.ingredient.test(itemStack)) {
                cir.setReturnValue(true);
                return;
            }
        }
        for (BrewingRecipeUtils.DynamicRecipe<Item> recipe : BrewingRecipeUtils.getItemRecipes()) {
            if (recipe.ingredient.test(itemStack)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }

    // 原实现只注入 mix @At("RETURN")：原版对不认识的数据包组合返回原输入，RETURN 时结果已定且非空，
    // 只能改 targetForm，无法把输入药水变成输出药水 → 永不生效。
    // 改为 HEAD 注入 + cancellable：命中数据包配方即自行构造结果（含 targetForm），未命中走原版。
    @Inject(method = "mix", at = @At("HEAD"), cancellable = true)
    private void ssc$mix(ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        if (input.isEmpty()) {
            return;
        }
        Item item = input.getItem();

        // Check mod's dynamic item recipes（物品/容器变换）
        for (BrewingRecipeUtils.DynamicRecipe<Item> recipe : BrewingRecipeUtils.getItemRecipes()) {
            if (recipe.matchesInput(item) && recipe.ingredient.test(ingredient)) {
                ItemStack out;
                PotionContents contents = input.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                Holder<Potion> potionEntry = contents.potion().orElse(null);
                if (potionEntry != null) {
                    // 容器变换：保留输入 potion，换成输出容器 item（参考 vanilla containerMix）
                    out = PotionContents.createItemStack(recipe.output, potionEntry);
                } else {
                    out = new ItemStack(recipe.output);
                }
                if (recipe.targetForm != null) {
                    setTargetForm(out, recipe.targetForm);
                }
                cir.setReturnValue(out);
                return;
            }
        }

        // Check mod's dynamic potion recipes（potion 变换）
        PotionContents potionContents = input.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        Holder<Potion> potionEntry = potionContents.potion().orElse(null);
        if (potionEntry == null) {
            return;
        }
        Potion potion = potionEntry.value();
        for (BrewingRecipeUtils.DynamicRecipe<Potion> recipe : BrewingRecipeUtils.getPotionRecipes()) {
            if (recipe.matchesInput(potion) && recipe.ingredient.test(ingredient)) {
                // 参考 vanilla mix 的 potion 变换：PotionContents.createItemStack(容器item, 输出 Holder<Potion>)
                ItemStack out = PotionContents.createItemStack(input.getItem(), BuiltInRegistries.POTION.wrapAsHolder(recipe.output));
                if (recipe.targetForm != null) {
                    setTargetForm(out, recipe.targetForm);
                }
                cir.setReturnValue(out);
                return;
            }
        }
    }
}
