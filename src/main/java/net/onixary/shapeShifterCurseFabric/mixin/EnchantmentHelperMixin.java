package net.onixary.shapeShifterCurseFabric.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.onixary.shapeShifterCurseFabric.additional_power.LootingPower;
import net.onixary.shapeShifterCurseFabric.additional_power.SoulSpeedPower;
import net.onixary.shapeShifterCurseFabric.status_effects.RegOtherStatusEffects;
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
        // Yarn hasStatusEffect(MobEffect) → Mojmap hasEffect(Holder<MobEffect>)：自有 effect 需 wrapAsHolder
        } else if (enchantment == Enchantments.FROST_WALKER
                && entity.hasEffect(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(RegOtherStatusEffects.FROST_CLAW))) {
            cir.setReturnValue(Math.max(1, cir.getReturnValue()));
        }
    }
    // 移植说明：1.20 上游在这里还挂了 hasSoulSpeed / getPossibleEntries 两处注入，这两个方法在 1.21 已移除。
    // 不需要再找替代 —— 1.21 里附魔等级查询统一走 getEnchantmentLevel（本 mixin 挂的就是它）：
    // 战利品侧已核实走这条路（EnchantedCountIncreaseFunction 取 looting 等级即调 getEnchantmentLevel），
    // 魂速同理由附魔效果系统经同一入口取值。故这两个旧注入点无需重现。
}
