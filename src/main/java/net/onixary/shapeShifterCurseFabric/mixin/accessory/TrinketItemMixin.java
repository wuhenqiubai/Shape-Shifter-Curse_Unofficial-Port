package net.onixary.shapeShifterCurseFabric.mixin.accessory;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.SlotType;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.util.Accessory.AccessoryUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

/*
// 本来想挂个Event 结果发现需要的Event 1.20.1 版本没有 所以只能用Mixin了 可能部分物品会失效 取决于是否使用super.onEquip等方法
@Mixin(TrinketItem.class)
public class TrinketItemMixin implements Trinket {
    @Unique
    public boolean CanAutoExecute(Identifier ItemID) {
        return TrinketUtils.getAccessoryMixinAuto(ItemID);
    }

    @Override
    public void onEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (entity instanceof PlayerEntity player) {
            Identifier ItemID = Registries.ITEM.getId(stack.getItem());
            if (CanAutoExecute(ItemID)) {
                TrinketUtils.ApplyAccessoryPowerOnEquip(player, ItemID);
            }
        }
    }

    @Override
    public void onUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (entity instanceof PlayerEntity player) {
            Identifier ItemID = Registries.ITEM.getId(stack.getItem());
            if (CanAutoExecute(ItemID)) {
                TrinketUtils.ApplyAccessoryPowerOnUnEquip(player, ItemID);
            }
        }
    }
}
 */

// 仿照Trinket处理饰品变更的代码写的 应该没问题
@Mixin(value = LivingEntity.class, priority = 1001)
public class TrinketItemMixin {
    @Unique
    private final Map<String, ItemStack> lastEquippedTrinkets = new HashMap<>();

    @Unique
    private final String pluginID = "trinkets";

    @Unique
    public void onEquip(ItemStack stack, SlotReference slot, Player player) {
        ResourceLocation ItemID = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (AccessoryUtils.CanAutoExecute(ItemID, pluginID)) {
            AccessoryUtils.onPlayerEquip(player, ItemID, pluginID);
        }
    }

    @Unique
    public void onUnequip(ItemStack stack, SlotReference slot, Player player) {
        ResourceLocation ItemID = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (AccessoryUtils.CanAutoExecute(ItemID, pluginID)) {
            AccessoryUtils.onPlayerUnEquip(player, ItemID, pluginID);
        }
    }

    @Inject(at = {@At("TAIL")}, method = {"tick"})
    public void tick(CallbackInfo ci) {
        if ((Object)this instanceof Player player) {
            if (!player.isRemoved()) {
                Map<String, ItemStack> newlyEquippedTrinkets = new HashMap<>();
	            TrinketsApi.getTrinketComponent(player).ifPresent((trinkets) -> trinkets.forEach((ref, stack) -> {
		            TrinketInventory inventory = ref.inventory();
		            SlotType slotType = inventory.getSlotType();
		            int index = ref.index();
		            ItemStack oldStack = this.getOldStack(slotType, index);
		            ItemStack newStack = inventory.getItem(index);
		            ItemStack newStackCopy = newStack.copy();
		            String newRef = slotType.getGroup() + "/" + slotType.getName() + "/" + index;
		            if (!ItemStack.matches(newStack, oldStack)) {
			            this.onUnequip(oldStack, ref, player);
			            this.onEquip(newStack, ref, player);
		            }
		            ItemStack tickedStack = inventory.getItem(index);
		            if (tickedStack.getItem() == newStackCopy.getItem()) {
			            newlyEquippedTrinkets.put(newRef, tickedStack.copy());
		            } else {
			            newlyEquippedTrinkets.put(newRef, newStackCopy);
		            }
	            }));
                this.lastEquippedTrinkets.clear();
                this.lastEquippedTrinkets.putAll(newlyEquippedTrinkets);
            }
        }
    }

    @Unique
    private ItemStack getOldStack(SlotType type, int index) {
        return this.lastEquippedTrinkets.getOrDefault(type.getGroup() + "/" + type.getName() + "/" + index, ItemStack.EMPTY);
    }
}