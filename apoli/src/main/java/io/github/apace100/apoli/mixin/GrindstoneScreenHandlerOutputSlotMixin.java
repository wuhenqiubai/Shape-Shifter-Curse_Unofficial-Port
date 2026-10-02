package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerModifiedGrindstone;
import io.github.apace100.apoli.power.ModifyGrindstonePower;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 * 改为注入外层 {@link GrindstoneMenu}，不再 targeting 匿名的结果槽 {@code GrindstoneMenu$4}。
 * （1.21.1 提交 ea98804；NeoForge/Connector 重新编译会重排匿名类编号，{@code $N} 目标会静默失效。）
 * 下面用到的注入点全是 {@link GrindstoneMenu} 自身的稳定成员。
 *
 * <p>原结果槽 {@code onTake} / {@code getExperienceAmount} 两个注入的外层等价实现：
 *
 * <ul>
 *   <li><b>取出时执行动作</b>：原版结果槽的 {@code onTake} 取出后会清空两个输入槽，
 *       这同步触发 {@code slotsChanged} → {@code createResult()}。在 {@code createResult} HEAD 时刻，
 *       结果槽已经是空的（刚被取走），而 {@link #apoli$lastOutput} 还留着上一次算出的输出。
 *       这个组合是「取出」独有的 —— 手动拿走一个输入时，结果槽里会一直留着旧结果直到重算 ——
 *       所以恰好每次取出触发一次，且覆盖直接点击 / shift 点击 / Q 丢弃 / 交换各条路径。</li>
 *   <li><b>经验修饰</b>：Fabric 上经验是在匿名结果槽的私有 {@code getExperienceAmount()} 里现算的，
 *       从外层菜单拦不到。NeoForge 重新编译的 {@code GrindstoneMenu} 会多一个私有 {@code xp} 字段，
 *       在 {@code createResult()} 里填、由 {@code getExperienceAmount()} 返回；
 *       这里用反射改那个字段（尽力而为，Fabric 上取不到字段即 no-op）。</li>
 * </ul>
 */
@Mixin(value = GrindstoneMenu.class, priority = 2000)
public class GrindstoneScreenHandlerOutputSlotMixin {

    @Shadow
    @Final
    private Container resultSlots;

    @Unique
    private ItemStack apoli$lastOutput = ItemStack.EMPTY;

    @Inject(method = "createResult", at = @At("HEAD"))
    private void apoli$executeGrindstoneActions(CallbackInfo ci) {
        if (apoli$lastOutput == null || apoli$lastOutput.isEmpty()) {
            return;
        }
        if (!resultSlots.getItem(0).isEmpty()) {
            return;
        }
        PowerModifiedGrindstone pmg = (PowerModifiedGrindstone) this;
        List<ModifyGrindstonePower> applyingPowers = pmg.getAppliedPowers();
        if (applyingPowers == null || applyingPowers.isEmpty()) {
            return;
        }
        ItemStack output = apoli$lastOutput.copy();
        applyingPowers.forEach(mgp -> {
            mgp.applyAfterGrindingItemAction(output);
            mgp.executeActions(pmg.getPos());
        });
        apoli$lastOutput = ItemStack.EMPTY;
    }

    /**
     * 记下刚算好（且已被 power 改写）的结果堆，供下一次「取出」判定使用。
     * 本 mixin 的 priority 高于 {@code GrindstoneScreenHandlerMixin}，因此它的 RETURN 处理器
     * 在该方法返回前最后执行 —— 也就是 {@code modifyResult()} 已经把最终结果写进 {@link #resultSlots} 之后。
     */
    @Inject(method = "createResult", at = @At("RETURN"))
    private void apoli$captureLastOutput(CallbackInfo ci) {
        apoli$lastOutput = resultSlots.getItem(0).copy();
    }

    @Inject(method = "createResult", at = @At("RETURN"))
    private void apoli$modifyExperience(CallbackInfo ci) {
        PowerModifiedGrindstone pmg = (PowerModifiedGrindstone) this;
        List<ModifyGrindstonePower> applyingPowers = pmg.getAppliedPowers();
        if (applyingPowers == null || applyingPowers.isEmpty()) {
            return;
        }
        List<Modifier> modifiers = applyingPowers.stream()
            .map(ModifyGrindstonePower::getExperienceModifier)
            .filter(Objects::nonNull)
            .toList();
        if (modifiers.isEmpty()) {
            return;
        }
        try {
            Field xpField = GrindstoneMenu.class.getDeclaredField("xp");
            xpField.setAccessible(true);
            int original = xpField.getInt(this);
            if (original == -1) {
                return;
            }
            xpField.setInt(this, (int) ModifierUtil.applyModifiers(pmg.getPlayer(), modifiers, original));
        } catch (Exception ignored) {
            // Fabric 版本的 GrindstoneMenu 没有 `xp` 字段：xp_modifier 在 Fabric 上失效是已知且可接受的降级。
        }
    }
}
