package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyBreakSpeedPower;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockBehaviour.class)
public abstract class AbstractBlockMixin {

    @ModifyReturnValue(at = @At("RETURN"), method = "getDestroyProgress")
    private float modifyBlockBreakSpeed(float base, BlockState state, Player player, BlockGetter world, BlockPos pos) {
        return PowerHolderComponent.modify(player, ModifyBreakSpeedPower.class, base, p -> p.doesApply(player.level(), pos));
    }

}
