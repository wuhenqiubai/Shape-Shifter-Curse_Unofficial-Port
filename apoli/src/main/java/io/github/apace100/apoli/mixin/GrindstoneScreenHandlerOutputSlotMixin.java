package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.access.PowerModifiedGrindstone;
import io.github.apace100.apoli.power.ModifyGrindstonePower;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

@Mixin(targets = "net/minecraft/world/inventory/GrindstoneMenu$4")
public class GrindstoneScreenHandlerOutputSlotMixin {

    @Final
    @Shadow
    GrindstoneMenu this$0;

    @Inject(method = "onTake", at = @At(value = "INVOKE",target = "Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V", ordinal = 0))
    private void executeGrindstoneActions(Player player, ItemStack stack, CallbackInfo ci) {
        PowerModifiedGrindstone pmg = (PowerModifiedGrindstone) this$0;
        List<ModifyGrindstonePower> applyingPowers = pmg.getAppliedPowers();
        applyingPowers.forEach(mgp -> {
            mgp.applyAfterGrindingItemAction(stack);
            mgp.executeActions(pmg.getPos());
        });
    }

    @ModifyReturnValue(method = "getExperienceAmount", at = @At("RETURN"))
    private int modifyExperience(int original, Level world) {
        PowerModifiedGrindstone pmg = (PowerModifiedGrindstone) this$0;
        if(pmg.getAppliedPowers().size() == 0) {
            return original;
        }
        List<Modifier> modifiers = pmg.getAppliedPowers().stream().map(ModifyGrindstonePower::getExperienceModifier).filter(Objects::nonNull).toList();
        if(modifiers.size() == 0) {
            return original;
        }
        return (int) ModifierUtil.applyModifiers(pmg.getPlayer(), modifiers, original);
    }
}
