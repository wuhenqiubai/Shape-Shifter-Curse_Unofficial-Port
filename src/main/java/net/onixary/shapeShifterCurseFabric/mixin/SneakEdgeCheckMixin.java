package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.additional_power.ModifyStepHeightPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Player.class, priority = 1200)
public class SneakEdgeCheckMixin {

    @WrapOperation(method = "maybeBackOffFromEdge", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;maxUpStep()F"))
    private float modifyStepHeightForSneaking(Player instance, Operation<Float> original) {
        return getOriginalOrModifiedStepHeight(instance, original);
    }

    @Unique
    private float getOriginalOrModifiedStepHeight(Player playerEntity, Operation<Float> operation) {
        // 检查是否有ModifyStepHeightPower设置了不影响力边缘检测
        boolean shouldUseOriginalHeight = PowerHolderComponent.getPowers(playerEntity, ModifyStepHeightPower.class)
                .stream()
                .anyMatch(power -> !power.shouldAffectSneak());

        // 如果需要使用原始高度，则返回原版的0.5f，否则返回修改后的step height
        if (shouldUseOriginalHeight) {
            return 0.5f; // 原版潜行边缘检测高度
        }

        return operation.call(playerEntity);
    }
}
