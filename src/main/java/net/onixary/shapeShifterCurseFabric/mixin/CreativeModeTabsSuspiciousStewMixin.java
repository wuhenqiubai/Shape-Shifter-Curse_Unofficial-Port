package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(CreativeModeTabs.class)
public abstract class CreativeModeTabsSuspiciousStewMixin {

    @Unique
    private static final Set<SuspiciousStewEffects> ssc$seenStewEffects = new HashSet<>();

    @Inject(method = "generateSuspiciousStews(Lnet/minecraft/world/item/CreativeModeTab$Output;Lnet/minecraft/world/item/CreativeModeTab$TabVisibility;)V", at = @At("HEAD"))
    private static void ssc$resetSeenStewEffects(CallbackInfo ci) {
        ssc$seenStewEffects.clear();
    }

    @Redirect(method = "generateSuspiciousStews(Lnet/minecraft/world/item/CreativeModeTab$Output;Lnet/minecraft/world/item/CreativeModeTab$TabVisibility;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"))
    private static boolean ssc$skipDuplicateStew(Set<ItemStack> set, Object element) {
        ItemStack stack = (ItemStack) element;
        SuspiciousStewEffects effects = stack.get(DataComponents.SUSPICIOUS_STEW_EFFECTS);
        if (effects != null && !ssc$seenStewEffects.add(effects)) {
            return false;
        }
        return set.add(stack);
    }
}
