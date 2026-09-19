package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.util.CraftingInputContainerHolder;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CraftingInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CraftingInput.class)
public abstract class CraftingInputMixin implements CraftingInputContainerHolder {
    @Unique private CraftingContainer apoli$craftingContainer;

    @Override
    public CraftingContainer apoli$getCraftingContainer() {
        return this.apoli$craftingContainer;
    }

    @Override
    public void apoli$setCraftingContainer(CraftingContainer container) {
        this.apoli$craftingContainer = container;
    }
}
