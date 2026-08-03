package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * 修复 1.21.11 vanilla 花效果重复导致的创造物品栏崩溃：
 * {@code CreativeModeTabs.generateSuspiciousStews} 遍历 {@code SuspiciousEffectHolder.getAllEffectHolders()}，
 * 而 vanilla 1.21.11 有重复效果的花（SATURATION 0.35F×2 / NIGHT_VISION 5.0F×2 / WEAKNESS 7.0F×4），
 * 生成的谜之炖菜 {@code SUSPICIOUS_STEW_EFFECTS} 组件相同 → {@code ItemStackLinkedSet} 报
 * "Accidentally adding the same item stack twice"。
 *
 * <p>这里在 {@code set.add(stack)} 前用 {@code SuspiciousStewEffects} 手动去重（仅按效果内容，与炖菜无关），
 * 跳过重复效果的炖菜，避免触发 vanilla 的去重缺陷。</p>
 */
@Mixin(CreativeModeTabs.class)
public abstract class CreativeModeTabsSuspiciousStewMixin {

    @Unique
    private static final Set<SuspiciousStewEffects> ssc$seenStewEffects = new HashSet<>();

    @Inject(method = "generateSuspiciousStews(Lnet/minecraft/world/item/CreativeModeTab$Output;Lnet/minecraft/world/item/CreativeModeTab$TabVisibility;)V", at = @At("HEAD"))
    private static void ssc$resetSeenStewEffects(CallbackInfo ci) {
        ssc$seenStewEffects.clear();
    }

    @Redirect(method = "generateSuspiciousStews(Lnet/minecraft/world/item/CreativeModeTab$Output;Lnet/minecraft/world/item/CreativeModeTab$TabVisibility;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"))
    private static boolean ssc$skipDuplicateStew(Set<ItemStack> set, Object element) {
        ItemStack stack = (ItemStack) element;
        SuspiciousStewEffects effects = stack.get(DataComponents.SUSPICIOUS_STEW_EFFECTS);
        if (effects != null && !ssc$seenStewEffects.add(effects)) {
            // 已生成过相同效果的炖菜 → 跳过（返回 false 表示未添加，原代码忽略返回值）
            return false;
        }
        return set.add(stack);
    }
}
