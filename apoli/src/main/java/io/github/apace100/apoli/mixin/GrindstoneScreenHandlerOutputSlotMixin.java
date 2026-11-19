package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerModifiedGrindstone;
import io.github.apace100.apoli.power.ModifyGrindstonePower;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

@Mixin(value = GrindstoneMenu.class, priority = 2000)
public class GrindstoneScreenHandlerOutputSlotMixin {

    @Shadow
    @Final
    private Container resultSlots;

    @Unique
    private ItemStack apoli$lastOutput = ItemStack.EMPTY;

    @Inject(method = "createResult", at = @At("HEAD"))
    private void apoli$executeGrindstoneActions(CallbackInfo ci) {
        if (apoli$lastOutput == null || apoli$lastOutput.isEmpty()) {
            return;
        }
        if (!resultSlots.getItem(0).isEmpty()) {
            return;
        }
        PowerModifiedGrindstone pmg = (PowerModifiedGrindstone) this;
        List<ModifyGrindstonePower> applyingPowers = pmg.getAppliedPowers();
        if (applyingPowers == null || applyingPowers.isEmpty()) {
            return;
        }
        ItemStack output = apoli$lastOutput.copy();
        applyingPowers.forEach(mgp -> {
            mgp.applyAfterGrindingItemAction(output);
            mgp.executeActions(pmg.getPos());
        });
        apoli$lastOutput = ItemStack.EMPTY;
    }

    @Inject(method = "createResult", at = @At("RETURN"))
    private void apoli$captureLastOutput(CallbackInfo ci) {
        apoli$lastOutput = resultSlots.getItem(0).copy();
    }

    @Inject(method = "createResult", at = @At("RETURN"))
    private void apoli$modifyExperience(CallbackInfo ci) {
        PowerModifiedGrindstone pmg = (PowerModifiedGrindstone) this;
        List<ModifyGrindstonePower> applyingPowers = pmg.getAppliedPowers();
        if (applyingPowers == null || applyingPowers.isEmpty()) {
            return;
        }
        List<Modifier> modifiers = applyingPowers.stream()
            .map(ModifyGrindstonePower::getExperienceModifier)
            .filter(Objects::nonNull)
            .toList();
        if (modifiers.isEmpty()) {
            return;
        }
        try {
            Field xpField = GrindstoneMenu.class.getDeclaredField("xp");
            xpField.setAccessible(true);
            int original = xpField.getInt(this);
            if (original == -1) {
                return;
            }
            xpField.setInt(this, (int) ModifierUtil.applyModifiers(pmg.getPlayer(), modifiers, original));
        } catch (Exception ignored) {
        }
    }
}