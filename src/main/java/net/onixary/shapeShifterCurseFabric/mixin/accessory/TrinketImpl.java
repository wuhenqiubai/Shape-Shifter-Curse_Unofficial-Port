package net.onixary.shapeShifterCurseFabric.mixin.accessory;

import eu.pb4.trinkets.api.SlotType;
import eu.pb4.trinkets.api.TrinketDropRule;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.callback.TrinketCallback;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.items.accessory.AccessoryItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;

// 26.1 / Trinkets 4.0.0 迁移说明：
//   Trinket(3.x) -> TrinketCallback(4.0)，方法名与参数形状不变，仅 SlotReference -> TrinketSlotAccess。
//   TrinketsApi.registerTrinket(...) 被删除 —— 注册改为**隐式**：本 mixin 把 TrinketCallback 加到
//   AccessoryItem 上，TrinketCallback.getCallback(stack) 内部的
//   `item.getItem() instanceof TrinketCallback` 即自动命中，因此原先那个
//   @Inject(method = "accessoryInit") 的注册注入整个删除。
@Mixin(AccessoryItem.class)
public class TrinketImpl implements TrinketCallback {
    @Unique
    private static final HashMap<Integer, AccessoryItem.SlotData> slotDataCache = new HashMap<>();

    @Unique
    private AccessoryItem.SlotData getSlotData(TrinketSlotAccess slot) {
        // 4.0: TrinketInventory#getSlotType() -> slotType()；SlotType#getGroup()/getName() -> group()/name()
        SlotType slotType = slot.inventory().slotType();
        return slotDataCache.computeIfAbsent(slot.hashCode(), k ->
            new AccessoryItem.SlotData(Identifier.fromNamespaceAndPath("trinket", "%s/%s".formatted(slotType.group(), slotType.name())), slot.index()));
    }

    @Override
    public void tick(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
        ((AccessoryItem) (Object) this).accessoryTick(stack, entity, getSlotData(slot));
    }

    @Override
    public void onEquip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
        ((AccessoryItem) (Object) this).onEquip(stack, entity, getSlotData(slot));
    }

    @Override
    public void onUnequip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
        ((AccessoryItem) (Object) this).onUnequip(stack, entity, getSlotData(slot));
    }

    @Override
    public boolean canEquip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
        return ((AccessoryItem) (Object) this).canEquip(stack, entity, getSlotData(slot));
    }

    @Override
    public boolean canUnequip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
        return ((AccessoryItem) (Object) this).canUnequip(stack, entity, getSlotData(slot));
    }

    @Override
    public void onBreak(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
        ((AccessoryItem) (Object) this).onBreak(stack, entity, getSlotData(slot));
    }

    @Override
    public TrinketDropRule getDropRule(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
        // 4.0: TrinketEnums.DropRule（内部枚举）-> eu.pb4.trinkets.api.TrinketDropRule（顶层枚举），常量名不变
        AccessoryItem.DropRule dropRule = ((AccessoryItem) (Object) this).getDropRule(stack, entity, getSlotData(slot));
        return switch (dropRule) {
            case KEEP -> TrinketDropRule.KEEP;
            case DROP -> TrinketDropRule.DROP;
            case DESTROY -> TrinketDropRule.DESTROY;
            default -> TrinketDropRule.DEFAULT;
        };
    }
}
