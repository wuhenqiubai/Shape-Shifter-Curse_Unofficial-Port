package net.onixary.shapeShifterCurseFabric.mixin.forge;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.items.accessory.AccessoryItem;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

@Mixin(AccessoryItem.class)
public class CurioImpl implements ICurioItem {

    @Unique
    private AccessoryItem.SlotData getSlotData(SlotContext slot) {
        return AccessoryItem.slotDataOf("curios", null, slot.identifier(), slot.index());
    }

    @Inject(method = "accessoryInit", at = @At("HEAD"))
    private void initCurio(Item.Properties settings, CallbackInfo ci) {
        CuriosApi.registerCurio((Item) (Object) this, (ICurioItem) this);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        ((AccessoryItem) (Object) this).accessoryTick(stack, slotContext.entity(), getSlotData(slotContext));
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        ((AccessoryItem) (Object) this).onEquip(stack, slotContext.entity(), getSlotData(slotContext));
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        ((AccessoryItem) (Object) this).onUnequip(stack, slotContext.entity(), getSlotData(slotContext));
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return ((AccessoryItem) (Object) this).canEquip(stack, slotContext.entity(), getSlotData(slotContext));
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return ((AccessoryItem) (Object) this).canUnequip(stack, slotContext.entity(), getSlotData(slotContext));
    }

    @Override
    public void curioBreak(SlotContext slotContext, ItemStack stack) {
        ((AccessoryItem) (Object) this).onBreak(stack, slotContext.entity(), getSlotData(slotContext));
    }

    @Override
    public ICurio.@NotNull DropRule getDropRule(SlotContext slotContext, DamageSource source, boolean recentlyHit, ItemStack stack) {
        return switch (((AccessoryItem) (Object) this).getDropRule(stack, slotContext.entity(), getSlotData(slotContext))) {
            case KEEP -> ICurio.DropRule.ALWAYS_KEEP;
            case DROP -> ICurio.DropRule.ALWAYS_DROP;
            case DESTROY -> ICurio.DropRule.DESTROY;
            default -> ICurio.DropRule.DEFAULT;
        };
    }
}
