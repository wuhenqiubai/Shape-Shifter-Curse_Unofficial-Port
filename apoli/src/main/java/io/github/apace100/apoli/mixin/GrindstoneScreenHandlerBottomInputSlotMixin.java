package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerModifiedGrindstone;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyGrindstonePower;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Rewritten to target the outer {@link GrindstoneMenu} instead of the anonymous bottom input slot
 * ({@code GrindstoneMenu$3}). NeoForge recompilation renumbers anonymous classes, so the old
 * {@code $N} target silently breaks there.
 *
 * <p>The old mixin injected into the anonymous slot's {@code mayPlace}. The outer menu has no
 * {@code mayPlace}, so instead the registered slot at index 1 (the bottom input) is swapped for a
 * wrapper whose {@code mayPlace} first consults {@link ModifyGrindstonePower#allowsInBottom} and
 * otherwise delegates to the vanilla check. The wrapper is what actually sits in the menu's
 * {@code slots} list (inherited from {@code AbstractContainerMenu}), so every placement path
 * (direct click, shift-click, drag, swap) is covered, and the injection points ({@code GrindstoneMenu}
 * constructor + the inherited {@code slots} list) are stable across Fabric/NeoForge.
 */
@Mixin(GrindstoneMenu.class)
public class GrindstoneScreenHandlerBottomInputSlotMixin {

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
    private void apoli$allowPowerStacksInBottom(CallbackInfo ci) {
        GrindstoneMenu menu = (GrindstoneMenu) (Object) this;
        Slot original = menu.slots.get(1);
        Slot wrapper = new Slot(original.container, original.getContainerSlot(), original.x, original.y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                Player player = ((PowerModifiedGrindstone) menu).getPlayer();
                if (PowerHolderComponent.hasPower(player, ModifyGrindstonePower.class, p -> p.allowsInBottom(stack))) {
                    return true;
                }
                return original.mayPlace(stack);
            }
        };
        wrapper.index = original.index;
        menu.slots.set(1, wrapper);
    }
}
