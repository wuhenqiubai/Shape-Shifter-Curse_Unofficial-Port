package net.onixary.shapeShifterCurseFabric.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.RestrictArmorPower;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.util.ModTags;
import net.onixary.shapeShifterCurseFabric.util.MorphScaleTagLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.inventory.ArmorSlot")
public abstract class ArmorSlotMixin {
    @Unique
    private static final String MSI_TAG = "MorphScaleItem";

    @Shadow @Final private LivingEntity owner;
    @Shadow @Final private EquipmentSlot slot;

    @Inject(method = "mayPlace", at = @At("RETURN"), cancellable = true)
    private void preventRestrictedArmorInsert(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (isMorphScaleItem(stack)) {
            cir.setReturnValue(true);
            return;
        }
        for (RestrictArmorPower rap : PowerHolderComponent.KEY.get(this.owner).getPowers(RestrictArmorPower.class)) {
            if (!rap.canEquip(stack, this.slot)) {
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
        if (stack.is(ModTags.MorphScaleItem_Tag)) return true;
        if (MorphScaleTagLoader.getMorphScaleItems().contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) return true;
        var c = stack.get(DataComponents.CUSTOM_DATA);
        return c != null && c.copyTag().getBooleanOr(MSI_TAG, false);
    }
}
