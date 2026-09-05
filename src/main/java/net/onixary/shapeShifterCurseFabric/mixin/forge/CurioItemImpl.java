package net.onixary.shapeShifterCurseFabric.mixin.forge;

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
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashMap;
import java.util.Map;

/**
 * Curios 兼容层（NeoForge 版，重写自上游 Forge 预编译 CurioItemImpl.class）。
 * 仿照 TrinketItemMixin 检测 Curios 槽位变化，装备/卸下时经 AccessoryUtils 应用到 accessory_power。
 * 用 NeoForge 的 {@code CuriosApi.getCuriosInventory(...) → Optional}（上游 Forge 用 LazyOptional，NeoForge 已改）。
 * 仅在 Curios mod 存在时经 MixinConfigPlugin 条件注入。
 */
@Mixin(value = LivingEntity.class, priority = 1001)
public class CurioItemImpl {
    @Unique
    private final Map<String, ItemStack> lastEquippedCurios = new HashMap<>();

    @Unique
    private final String pluginID = "curios";

    @Inject(at = @At("TAIL"), method = "tick")
    private void ssc$tick(CallbackInfo ci) {
        if ((Object) this instanceof Player player) {
            if (!player.isRemoved()) {
                Map<String, ItemStack> newlyEquippedCurios = new HashMap<>();
                CuriosApi.getCuriosInventory(player).ifPresent(handler ->
                    handler.getCurios().forEach((slotType, stacksHandler) -> {
                        for (int i = 0; i < stacksHandler.getSlots(); i++) {
                            String ref = slotType + "/" + i;
                            ItemStack oldStack = lastEquippedCurios.getOrDefault(ref, ItemStack.EMPTY);
                            ItemStack newStack = stacksHandler.getStacks().getStackInSlot(i);
                            ItemStack newStackCopy = newStack.copy();
                            if (!ItemStack.matches(newStack, oldStack)) {
                                ResourceLocation oldId = BuiltInRegistries.ITEM.getKey(oldStack.getItem());
                                ResourceLocation newId = BuiltInRegistries.ITEM.getKey(newStack.getItem());
                                if (AccessoryUtils.CanAutoExecute(oldId, pluginID)) {
                                    AccessoryUtils.onPlayerUnEquip(player, oldId, pluginID);
                                }
                                if (AccessoryUtils.CanAutoExecute(newId, pluginID)) {
                                    AccessoryUtils.onPlayerEquip(player, newId, pluginID);
                                }
                            }
                            newlyEquippedCurios.put(ref, newStackCopy);
                        }
                    }));
                lastEquippedCurios.clear();
                lastEquippedCurios.putAll(newlyEquippedCurios);
            }
        }
    }
}
