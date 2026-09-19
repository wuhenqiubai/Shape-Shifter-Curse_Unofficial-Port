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

/**
 * Rewritten to target the outer {@link GrindstoneMenu} instead of the anonymous result slot
 * ({@code GrindstoneMenu$4}). NeoForge recompilation renumbers anonymous classes, so targeting the
 * anonymous class by {@code $N} silently breaks there. All injection points below are stable members
 * of {@link GrindstoneMenu} itself.
 *
 * <p>Outer-level equivalent of the old result-slot {@code onTake} / {@code getExperienceAmount}
 * injections:
 *
 * <ul>
 *   <li><b>Actions on take</b>: the vanilla result slot's {@code onTake} clears the repair slots right
 *       after the output is taken, which synchronously triggers {@code createResult()} (via
 *       {@code slotsChanged}). At the {@code createResult} HEAD the result slot is already empty (the
 *       output was just taken) while {@link #apoli$lastOutput} still holds the previously-computed
 *       output. That combination is unique to a take — manually removing an input leaves the old result
 *       in the result slot until recomputation — so it fires exactly once per take and covers direct
 *       click, shift-click, throw and swap paths.</li>
 *   <li><b>Experience modifier</b>: on Fabric the XP is computed privately inside the anonymous result
 *       slot's {@code getExperienceAmount()} at take time, so it cannot be intercepted from the outer
 *       menu. NeoForge recompiles GrindstoneMenu with a private {@code xp} field populated during
 *       {@code createResult()} and returned by {@code getExperienceAmount()}; we reflectively update
 *       that field when present (best-effort, no-op on Fabric).</li>
 * </ul>
 */
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

    /**
     * Captures the freshly computed (and power-modified) result stack for the next take detection.
     * This mixin has a higher priority than {@code GrindstoneScreenHandlerMixin}, so it is applied
     * later and this RETURN handler runs immediately before the method returns — i.e. after
     * {@code modifyResult()} has already written the final stack into {@link #resultSlots}.
     */
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
            // Fabric target has no `xp` field: xp_modifier is a documented NeoForge-only degradation
        }
    }
}
