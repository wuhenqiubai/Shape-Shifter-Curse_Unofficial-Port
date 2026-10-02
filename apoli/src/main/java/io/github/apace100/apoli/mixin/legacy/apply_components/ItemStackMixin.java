package io.github.apace100.apoli.mixin.legacy.apply_components;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.legacy.OverlayableComponentsItemStack;
import io.github.apace100.apoli.legacy.OverlayableDataComponentMap;
import io.github.apace100.apoli.power.legacy.ApplyComponentsPower;
import io.github.apace100.apoli.util.MiscUtil;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements OverlayableComponentsItemStack {
    @Shadow @Final private PatchedDataComponentMap components;
    @Unique private final OverlayableDataComponentMap apoli_legacy$overlayableComponents = new OverlayableDataComponentMap(this.components);
    @Unique private int apoli_legacy$searchDepth = 0;

    @ModifyReturnValue(method = "getComponents", at = @At("RETURN"))
    private DataComponentMap apoli_legacy$wrapComponentsInOverlay(DataComponentMap original) {
        if (!this.apoli_legacy$overlayableComponents.matchesOriginal(original)) {
            this.apoli_legacy$overlayableComponents.setOriginal(original);
        }

        if (this instanceof EntityLinkedItemStack linkedItemStack) {
            var entity = linkedItemStack.apoli$getEntity();
            if (entity instanceof LivingEntity livingEntity) {
                try {
                    if (this.apoli_legacy$searchDepth++ < 1) {
                        this.apoli_legacy$updateOverlayableComponents(livingEntity);
                    }
                } finally {
                    this.apoli_legacy$searchDepth--;
                }

                if (this.apoli_legacy$overlayableComponents.hasOverlays())
                    return this.apoli_legacy$overlayableComponents;
            } else {
                this.apoli_legacy$overlayableComponents.clearOverlays();
            }
        }

        return original;
    }

    public void apoli_legacy$updateOverlayableComponents(LivingEntity entity) {
        PowerHolderComponent.getPowers(entity, ApplyComponentsPower.class)
            .forEach(power -> {
                if (this.apoli_legacy$overlayableComponents.hasOverlay(power.getComponents()))
                    return;

                var stacks = power.getAppliedStacks(entity);
                var stack = (ItemStack) (Object) this;
                if (stacks.containsValue(stack)) {
                    var slots = MiscUtil.getKeysForValue(stacks, stack);
                    for (EquipmentSlot slot : slots) {
                        if (entity.getItemBySlot(slot) == stack) { // not doing an equals check here, we want the same reference.
                            this.apoli_legacy$overlayableComponents.addOverlay(power.getComponents(), power.shouldReplaceExisting());
                        }
                    }
                }
            });
    }
}
