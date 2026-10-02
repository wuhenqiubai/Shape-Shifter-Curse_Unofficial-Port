package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.util.CraftingInputContainerHolder;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CraftingInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CraftingContainer.class)
public interface CraftingContainerMixin {
    @ModifyReturnValue(method = "asPositionedCraftInput", at = @At("RETURN"))
    private CraftingInput.Positioned apoli$storeCraftingContainer(CraftingInput.Positioned original) {
        ((CraftingInputContainerHolder) original.input()).apoli$setCraftingContainer((CraftingContainer) this);
        return original;
    }
}
