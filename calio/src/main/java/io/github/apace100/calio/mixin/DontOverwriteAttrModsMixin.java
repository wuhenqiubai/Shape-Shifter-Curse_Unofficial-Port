package io.github.apace100.calio.mixin;

import io.github.apace100.calio.Calio;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/***
 * This mixin makes sure that adding attribute modifiers to an equipment item does not overwrite the existing ones.
 */
@Mixin(ItemStack.class)
public abstract class DontOverwriteAttrModsMixin {

    @Inject(at = @At("HEAD"), method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V")
    private void addAttributeModifiersFromItem(CallbackInfo info) {
        ItemStack thisStack = (ItemStack)(Object)this;
        Calio.areEntityAttributesAdditional(thisStack);
    }
}
