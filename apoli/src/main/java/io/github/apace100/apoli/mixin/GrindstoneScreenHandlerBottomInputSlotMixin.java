package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerModifiedGrindstone;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyGrindstonePower;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 改为注入外层 {@link GrindstoneMenu}，不再 targeting 匿名的底部输入槽 {@code GrindstoneMenu$3}。
 * （1.21.1 提交 ea98804；NeoForge/Connector 重新编译会重排匿名类编号，{@code $N} 目标会静默失效。）
 *
 * <p>原实现注入匿名槽的 {@code mayPlace}。外层菜单本身没有 {@code mayPlace}，
 * 因此改为：构造器 RETURN 时把 {@code slots} 里 index 1 的槽**换成包装槽**，
 * 其 {@code mayPlace} 先问 {@link ModifyGrindstonePower#allowsInBottom}，否则委托原槽判定。
 * 包装槽就是真正躺在菜单 {@code slots} 列表里的那个，所以直接点击 / shift 点击 / 拖拽 / 交换
 * 各条入槽路径都覆盖得到；而注入点（{@code GrindstoneMenu} 构造器 + 继承来的 {@code slots} 列表）
 * 在 Fabric / NeoForge 两侧都稳定。
 */
@Mixin(GrindstoneMenu.class)
public class GrindstoneScreenHandlerBottomInputSlotMixin {

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
    private void apoli$allowPowerStacksInBottom(CallbackInfo ci) {
        GrindstoneMenu menu = (GrindstoneMenu) (Object) this;
        Slot original = menu.slots.get(1);
        Slot wrapper = new Slot(original.container, original.getContainerSlot(), original.x, original.y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                Player player = ((PowerModifiedGrindstone) menu).getPlayer();
                if (PowerHolderComponent.hasPower(player, ModifyGrindstonePower.class, p -> p.allowsInBottom(stack))) {
                    return true;
                }
                return original.mayPlace(stack);
            }
        };
        wrapper.index = original.index;
        menu.slots.set(1, wrapper);
    }
}
