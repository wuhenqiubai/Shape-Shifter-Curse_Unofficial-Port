package net.onixary.shapeShifterCurseFabric.mixin.accessory;

import dev.emi.trinkets.api.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.items.accessory.AccessoryItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@Mixin(AccessoryItem.class)
public class TrinketImpl implements Trinket {
    @Unique
    private static final HashMap<Integer, AccessoryItem.SlotData> slotDataCache = new HashMap<>();

    @Unique
    private AccessoryItem.SlotData getSlotData(SlotReference slot) {
        SlotType slotType = slot.inventory().getSlotType();
        return slotDataCache.computeIfAbsent(slot.hashCode(), k ->
            new AccessoryItem.SlotData(ResourceLocation.fromNamespaceAndPath("trinket", "%s/%s".formatted(slotType.getGroup(), slotType.getName())), slot.index()));
    }

    @Inject(method = "accessoryInit", at = @At("HEAD"))
    private void initAccessory(CallbackInfo ci) {
        TrinketsApi.registerTrinket((AccessoryItem) (Object) this, this);
    }

    @Override
    public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
        ((AccessoryItem) (Object) this).accessoryTick(stack, entity, getSlotData(slot));
    }

    @Override
    public void onEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        ((AccessoryItem) (Object) this).onEquip(stack, entity, getSlotData(slot));
    }

    @Override
    public void onUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        ((AccessoryItem) (Object) this).onUnequip(stack, entity, getSlotData(slot));
    }

    @Override
    public boolean canEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        return ((AccessoryItem) (Object) this).canEquip(stack, entity, getSlotData(slot));
    }

    @Override
    public boolean canUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        return ((AccessoryItem) (Object) this).canUnequip(stack, entity, getSlotData(slot));
    }

    @Override
    public void onBreak(ItemStack stack, SlotReference slot, LivingEntity entity) {
        ((AccessoryItem) (Object) this).onBreak(stack, entity, getSlotData(slot));
    }

    @Override
    public TrinketEnums.DropRule getDropRule(ItemStack stack, SlotReference slot, LivingEntity entity) {
        AccessoryItem.DropRule dropRule = ((AccessoryItem) (Object) this).getDropRule(stack, entity, getSlotData(slot));
        return switch (dropRule) {
            case KEEP -> TrinketEnums.DropRule.KEEP;
            case DROP -> TrinketEnums.DropRule.DROP;
            case DESTROY -> TrinketEnums.DropRule.DESTROY;
            default -> TrinketEnums.DropRule.DEFAULT;
        };
    }
}