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
 * NeoForge/Connector 兼容：主 ServerPlayerInteractionManagerMixin 里 ModifyHarvestPower 通过
 * {@code @ModifyVariable} 在 destroyBlock 内联处理，NeoForge 重编译使该注入点不稳定。此版本参考 Apoli 2.9.2 connector，
 * 改为在 {@link Player#hasCorrectToolForDrops}（mojmap 1.21.1 名，即 2.9.2 yarn 的 canHarvest）HEAD 处理
 * ModifyHarvestPower：读取由 connector ServerPlayerInteractionManagerMixin 的 destroyBlock HEAD 写入
 * HarvestContext ThreadLocal 的方块位置，命中即返回 {@code power.isHarvestAllowed()}。
 * 由 ApoliMixinPlugin 在 NeoForge 下启用、Fabric 下跳过。
 */
@Mixin(Player.class)
public class PlayerEntityMixin {

    @Inject(method = "hasCorrectToolForDrops", at = @At("HEAD"), cancellable = true)
    private void origins$modifyHarvestCheck(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        SavedBlockPosition saved = HarvestContext.getBlockPosition();
        // hasCorrectToolForDrops 也会在破坏进度计算（BlockBehaviour#getDestroyProgress）等处被调用，
        // 此时 HarvestContext 尚未写入，直接放行原逻辑，避免对 null 求条件导致 NPE。
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
