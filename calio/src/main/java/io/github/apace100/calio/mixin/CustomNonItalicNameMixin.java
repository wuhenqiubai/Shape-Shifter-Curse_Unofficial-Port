package io.github.apace100.calio.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.apace100.calio.Calio;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/***
 * These mixins allow setting a custom name for item stacks via NBT.
 */
public abstract class CustomNonItalicNameMixin {

    @Mixin(ItemStack.class)
    public abstract static class ModifyItalicDisplayItem {
        @ModifyExpressionValue(method = "getStyledHoverName", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;has(Lnet/minecraft/core/component/DataComponentType;)Z"))
        private boolean hasCustomNameWhichIsItalic(boolean original) {
            return original && !Calio.hasNonItalicName((ItemStack) (Object) this);
        }
    }

    @Mixin(Gui.class)
    public abstract static class ModifyItalicDisplayHud {
        @Shadow private ItemStack lastToolHighlight;

        @ModifyExpressionValue(method = "extractSelectedItemName", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;has(Lnet/minecraft/core/component/DataComponentType;)Z"))
        private boolean hasCustomNameWhichIsItalic(boolean original) {
            return original && !Calio.hasNonItalicName(this.lastToolHighlight);
        }
    }
}
