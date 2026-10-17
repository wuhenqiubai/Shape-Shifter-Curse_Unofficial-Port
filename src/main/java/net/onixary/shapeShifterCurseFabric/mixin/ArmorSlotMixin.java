package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.util.ModTags;
import net.onixary.shapeShifterCurseFabric.util.MorphScaleTagLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.inventory.ArmorSlot")
public abstract class ArmorSlotMixin {
    @Unique
    private static final String MSI_TAG = "MorphScaleItem";

    @Inject(method = "mayPlace", at = @At("RETURN"), cancellable = true)
    private void allowMorphScaleArmor(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (isMorphScaleItem(stack)) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private static boolean isMorphScaleItem(ItemStack stack) {
        if (stack.is(ModTags.MorphScaleItem_Tag)) return true;
        if (MorphScaleTagLoader.getMorphScaleItems().contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) return true;
        var c = stack.get(DataComponents.CUSTOM_DATA);
        return c != null && c.copyTag().getBooleanOr(MSI_TAG, false);
    }
}
