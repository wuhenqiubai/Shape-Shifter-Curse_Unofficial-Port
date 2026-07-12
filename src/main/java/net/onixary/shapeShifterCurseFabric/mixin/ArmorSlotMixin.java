package net.onixary.shapeShifterCurseFabric.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.RestrictArmorPower;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.onixary.shapeShifterCurseFabric.util.ModTags;
import net.onixary.shapeShifterCurseFabric.util.MorphScaleTagLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.screen.slot.ArmorSlot")
public abstract class ArmorSlotMixin {
    @Unique
    private static final String MSI_TAG = "MorphScaleItem";

    @Shadow @Final private LivingEntity entity;
    @Shadow @Final private EquipmentSlot equipmentSlot;

    @Inject(method = "canInsert", at = @At("RETURN"), cancellable = true)
    private void preventRestrictedArmorInsert(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (isMorphScaleItem(stack)) {
            cir.setReturnValue(true);
            return;
        }
        for (RestrictArmorPower rap : PowerHolderComponent.KEY.get(this.entity).getPowers(RestrictArmorPower.class)) {
            if (!rap.canEquip(stack, this.equipmentSlot)) {
                cir.setReturnValue(false);
                return;
            }
        }
        // Apoli 的 ArmorSlotMixin 谓词写反（p.canEquip 而非 !p.canEquip），
        // 导致未被限制的槽位也返回 false。此处主动返回 true 覆盖。
        cir.setReturnValue(true);
    }

    @Unique
    private static boolean isMorphScaleItem(ItemStack stack) {
        if (stack.isIn(ModTags.MorphScaleItem_Tag)) return true;
        if (MorphScaleTagLoader.getMorphScaleItems().contains(Registries.ITEM.getId(stack.getItem()).toString())) return true;
        var c = stack.get(DataComponentTypes.CUSTOM_DATA);
        return c != null && c.copyNbt().getBoolean(MSI_TAG);
    }
}
