package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(targets = "net.minecraft.world.item.CreativeModeTab$ItemDisplayBuilder")
public abstract class CreativeModeTabItemDisplayBuilderMixin {
    @Shadow
    @Final
    public Collection<ItemStack> tabContents;

    @Inject(
            method = "accept(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/CreativeModeTab$TabVisibility;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ssc$skipDuplicateStack(ItemStack itemStack, CreativeModeTab.TabVisibility tabVisibility, CallbackInfo ci) {
        if (tabContents.contains(itemStack) && tabVisibility != CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY) {
            ci.cancel();
        }
    }
}
