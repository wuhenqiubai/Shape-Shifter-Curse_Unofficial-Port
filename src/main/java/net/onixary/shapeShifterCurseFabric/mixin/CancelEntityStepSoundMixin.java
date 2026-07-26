package net.onixary.shapeShifterCurseFabric.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.onixary.shapeShifterCurseFabric.additional_power.NoStepSoundPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class CancelEntityStepSoundMixin {
    /**
     * 拦截脚步声播放逻辑
     */
    @Inject(
            method = "playStepSound",
            at = @At("HEAD"),
            cancellable = true
    )
    private void disablePlayerStepSound(BlockPos pos, BlockState state, CallbackInfo ci) {
        // 类型检查确保是玩家实体
        if ((Object)this instanceof Player) {
            if (PowerHolderComponent.hasPower((Player) (Object)this, NoStepSoundPower.class)) {
                ci.cancel();
            }
        }
    }
}
