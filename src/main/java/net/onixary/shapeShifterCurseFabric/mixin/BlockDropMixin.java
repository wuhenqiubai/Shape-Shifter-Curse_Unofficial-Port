package net.onixary.shapeShifterCurseFabric.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.onixary.shapeShifterCurseFabric.additional_power.ModifyBlockDropPower;
import net.onixary.shapeShifterCurseFabric.util.CachedBlockPositionData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static net.minecraft.world.level.block.Block.popResource;

@Mixin(Block.class)
public abstract class BlockDropMixin {
    @Inject(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    private static void dropStacks(BlockState state, Level world, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack tool, CallbackInfo ci) {
        if (world instanceof ServerLevel && entity instanceof Player player) {
            CachedBlockPositionData cachedBlockPosition = new CachedBlockPositionData(world, pos, false, state, blockEntity);
            for (ModifyBlockDropPower power : PowerHolderComponent.getPowers(player, ModifyBlockDropPower.class)) {
                if (power.CanApply(cachedBlockPosition)) {
                    List<ItemStack> stackList = power.Apply(player.getRandom());
                    if (stackList != null) {
                        stackList.forEach((stack) -> {
                            popResource(world, pos, stack.copy());
                        });
                        state.spawnAfterBreak((ServerLevel)world, pos, tool, true);
                        ci.cancel();
                    }
                    // 满足条件但没中概率 执行原掉落物
                    return;
                }
            }
        }
    }
}