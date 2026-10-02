package io.github.apace100.apoli.mixin.integration.connector;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyHarvestPower;
import io.github.apace100.apoli.util.HarvestContext;
import io.github.apace100.apoli.util.SavedBlockPosition;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code ModifyHarvestPower} 的 Connector 侧承接点（对应 1.21.1 提交 ea98804）。
 *
 * <p>主版在 {@code ServerPlayerInteractionManagerMixin} 的 {@code destroyBlock} 里用 {@code @ModifyVariable}
 * 内联处理；NeoForge 重编译后该注入点不稳定（见 {@link ServerPlayerInteractionManagerMixin} 的说明）。
 * 本版改为在 {@link Player#hasCorrectToolForDrops} HEAD 处理：读取由 connector 版
 * {@code ServerPlayerInteractionManagerMixin} 的 {@code destroyBlock HEAD} 写进
 * {@link HarvestContext} 的方块位置，命中即返回 {@code power.isHarvestAllowed()}。
 *
 * <p>由 {@link io.github.apace100.apoli.ApoliMixinPlugin} 在 Connector 环境下启用、Fabric 下跳过。
 */
@Mixin(Player.class)
public class PlayerEntityMixin {

    @Inject(method = "hasCorrectToolForDrops", at = @At("HEAD"), cancellable = true)
    private void apoli$modifyHarvestCheck(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        SavedBlockPosition saved = HarvestContext.getBlockPosition();
        // hasCorrectToolForDrops 也会在破坏进度计算（BlockBehaviour#getDestroyProgress）等路径被调用，
        // 此时 HarvestContext 尚未写入 —— 直接放行原逻辑，避免对 null 求条件导致 NPE。
        if (saved == null) {
            return;
        }

        Player player = (Player) (Object) this;
        for (ModifyHarvestPower power : PowerHolderComponent.getPowers(player, ModifyHarvestPower.class)) {
            if (power.doesApply(saved)) {
                cir.setReturnValue(power.isHarvestAllowed());
                return;
            }
        }
    }
}
