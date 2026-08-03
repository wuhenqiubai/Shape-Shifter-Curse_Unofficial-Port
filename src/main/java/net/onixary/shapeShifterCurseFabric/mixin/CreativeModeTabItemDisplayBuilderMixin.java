package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

/**
 * 1.21.11 修复 vanilla 创造物品栏跨 tab 重复物品导致的崩溃：
 * GRANITE 在 building_blocks 与 natural_blocks 两个 tab 都添加（PARENT_AND_SEARCH_TABS），
 * 搜索物品 tab 收集时 {@code ItemDisplayBuilder.accept} 检测到 tabContents 已含同物品
 * （{@code tabContents.contains} 且非 SEARCH_TAB_ONLY）→ 抛 "Accidentally adding the same item stack twice" 崩服。
 * 这里在 HEAD 重复时 cancel（跳过，不抛异常），与 {@code CreativeModeTabsSuspiciousStewMixin} 同源。
 */
@Mixin(targets = "net.minecraft.world.item.CreativeModeTab$ItemDisplayBuilder")
public abstract class CreativeModeTabItemDisplayBuilderMixin {
    @Shadow
    @Final
    public Collection<ItemStack> tabContents;

    @Inject(
            method = "accept(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/CreativeModeTab$TabVisibility;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ssc$skipDuplicateStack(ItemStack itemStack, CreativeModeTab.TabVisibility tabVisibility, CallbackInfo ci) {
        if (tabContents.contains(itemStack) && tabVisibility != CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY) {
            ci.cancel();
        }
    }
}
