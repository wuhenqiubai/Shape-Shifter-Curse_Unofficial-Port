package net.onixary.shapeShifterCurseFabric.mixin.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.items.accessory.AccessoryItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * Curios 兼容层（NeoForge 版，重写自上游 Forge 预编译 CurioImpl.class）。
 * 让 AccessoryItem 实现 Curios 的 {@link ICurioItem}，构造时注册到 Curios，并把 Curios 的
 * 生命周期回调（tick/装备/卸下/可装/可卸/破坏/掉落）转发到 AccessoryItem 对应方法。
 * 仅在 Curios mod 存在时经 MixinConfigPlugin 条件注入。
 */
@Mixin(AccessoryItem.class)
public class CurioImpl implements ICurioItem {

    @Unique
    private AccessoryItem.SlotData getSlotData(SlotContext slot) {
        return new AccessoryItem.SlotData(ResourceLocation.fromNamespaceAndPath("curios", slot.identifier()), slot.index());
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
    public ICurio.DropRule getDropRule(SlotContext slotContext, DamageSource source, boolean recentlyHit, ItemStack stack) {
        return switch (((AccessoryItem) (Object) this).getDropRule(stack, slotContext.entity(), getSlotData(slotContext))) {
            case KEEP -> ICurio.DropRule.ALWAYS_KEEP;
            case DROP -> ICurio.DropRule.ALWAYS_DROP;
            case DESTROY -> ICurio.DropRule.DESTROY;
            default -> ICurio.DropRule.DEFAULT;
        };
    }
}
