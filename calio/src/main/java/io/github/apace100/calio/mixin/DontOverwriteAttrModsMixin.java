package io.github.apace100.calio.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.calio.Calio;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/***
 * This mixin makes sure that adding attribute modifiers to an equipment item does not overwrite the existing ones.
 */
@Mixin(ItemStack.class)
public abstract class DontOverwriteAttrModsMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/ItemAttributeModifiers;forEach(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V"), method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V")
    private void addAttributeModifiersFromItem(CallbackInfo info, @Local ItemAttributeModifiers modifiers) {
        ItemStack thisStack = (ItemStack)(Object)this;
        if(Calio.areEntityAttributesAdditional(thisStack)) { // TODO: hold on, what does this do?
            //multimap.putAll(thisStack.getItem().getDefaultAttributeModifiers(slot));
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/ItemAttributeModifiers;forEach(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V"), method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V")
    private void addAttributeModifiersFromItem2(CallbackInfo info, @Local ItemAttributeModifiers modifiers) {
        ItemStack thisStack = (ItemStack)(Object)this;
        if(Calio.areEntityAttributesAdditional(thisStack)) { // TODO: hold on, what does this do?
            //multimap.putAll(thisStack.getItem().getDefaultAttributeModifiers(slot));
        }
    }
}
