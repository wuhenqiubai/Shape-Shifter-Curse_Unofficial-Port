package net.onixary.shapeShifterCurseFabric.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.onixary.shapeShifterCurseFabric.additional_power.LootingPower;
import net.onixary.shapeShifterCurseFabric.additional_power.SoulSpeedPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @Unique
    private static int getLootingLevel(LivingEntity entity, int PreValue) {
        AtomicInteger powerLooting = new AtomicInteger(PreValue);
        PowerHolderComponent.getPowers(entity, LootingPower.class).forEach(power -> powerLooting.set(power.getLevel(powerLooting.get())));
        return powerLooting.get();
    }

    @Unique
    private static int getSoulSpeedLevel(LivingEntity entity, int PreValue) {
        AtomicInteger powerSoulSpeed = new AtomicInteger(PreValue);
        PowerHolderComponent.getPowers(entity, SoulSpeedPower.class).forEach(power -> powerSoulSpeed.set(power.getLevel(powerSoulSpeed.get())));
        return powerSoulSpeed.get();
    }

    @Inject(method = "getEnchantmentLevel", at = @At("RETURN"), cancellable = true)
    private static void getEquipmentLevelMixin(Holder<Enchantment> enchantment, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (enchantment.is(Enchantments.LOOTING)) {
            cir.setReturnValue(getLootingLevel(entity, cir.getReturnValue()));
        } else if (enchantment.is(Enchantments.SOUL_SPEED)) {
            cir.setReturnValue(getSoulSpeedLevel(entity, cir.getReturnValue()));
        }
    }
    // 1.21.11：hasSoulSpeed 判定已被 getEnchantmentLevel(SOUL_SPEED) 吸收（上方注入已提升）；
    // getPossibleEntries 已由 EnchantmentUtilAnvilMixin / EnchantmentUtilEnchantCommandMixin 接管（util/EnchantmentUtils）。
}
