package net.onixary.shapeShifterCurseFabric.recipes;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

import java.util.function.Function;
import java.util.function.Predicate;

public abstract class UpgradeRecipe implements SmithingRecipe, ISmithingRecipeEX {
    public final Identifier id;
    public final Predicate<ItemStack> template;
    public final Predicate<ItemStack> base;
    public final Predicate<ItemStack> addition;
    public final Function<ItemStack, ItemStack> upgradeResult;

    public boolean isUpgradeAll() {
        return false;
    }

    public UpgradeRecipe(Identifier id, Predicate<ItemStack> template, Predicate<ItemStack> base, Predicate<ItemStack> addition, Function<ItemStack, ItemStack> upgradeResult) {
        this.id = id;
        this.template = template;
        this.base = base;
        this.addition = addition;
        this.upgradeResult = upgradeResult;
    }

    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return this.template.test(stack);
    }

    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        return this.base.test(stack);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return this.addition.test(stack);
    }

    @Override
    public boolean matches(SmithingRecipeInput input, Level world) {
        return this.template.test(input.template()) && this.base.test(input.base()) && this.addition.test(input.addition());
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input, HolderLookup.Provider lookup) {
        ItemStack itemStack = input.base();
        if (this.base.test(itemStack)) {
            ItemStack outputStack = itemStack.copy();
            if (!isUpgradeAll()) {
                outputStack.setCount(1);
            }
            return this.upgradeResult.apply(outputStack);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registriesLookup) {
        ItemStack itemStack = new ItemStack(Items.IRON_CHESTPLATE);
        return this.upgradeResult.apply(itemStack.copy());
    }

    public Identifier getId() {
        return this.id;
    }

    @Override
    public boolean overrideVanillaOnTakeOutput() {
        return this.isUpgradeAll();
    }

    @Override
    public void onTakeOutput(SmithingMenu screenHandler, Player player, ItemStack stack) {
        if (this.isUpgradeAll()) {
            screenHandler.shrinkStackInSlot(0);
            screenHandler.inputSlots.setItem(1, ItemStack.EMPTY);
            screenHandler.shrinkStackInSlot(2);
        }
    }
}