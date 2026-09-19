package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PhasingPower;
import io.github.apace100.apoli.power.PreventBlockSelectionPower;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("deprecation")
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class AbstractBlockStateMixin {

    @Shadow
    public abstract Block getBlock();

    @Inject(method = "getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true)
    private void preventBlockSelection(BlockGetter world, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if(context instanceof EntityCollisionContext) {
            if(((EntityCollisionContext)context).getEntity() != null) {
                Entity entity = ((EntityCollisionContext)context).getEntity();
                if(PowerHolderComponent.getPowers(entity, PreventBlockSelectionPower.class).stream().anyMatch(p -> p.doesPrevent(entity.level(), pos))) {
                    cir.setReturnValue(Shapes.empty());
                }
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", cancellable = true)
    private void phaseThroughBlocks(BlockGetter world, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> info) {
        VoxelShape blockShape = info.getReturnValue();
        if(!blockShape.isEmpty() && context instanceof EntityCollisionContext) {
            EntityCollisionContext esc = (EntityCollisionContext)context;
            if(esc.getEntity() != null) {
                Entity entity = esc.getEntity();
                boolean isAbove = isAbove(entity, blockShape, pos, false);
                for (PhasingPower phasingPower : PowerHolderComponent.getPowers(entity, PhasingPower.class)) {
                    if(!isAbove || phasingPower.shouldPhaseDown(entity)) {
                        if(phasingPower.doesApply(pos)) {
                            info.setReturnValue(Shapes.empty());
                        }
                    }
                }
            }
        }
    }

    @Unique
    private boolean isAbove(Entity entity, VoxelShape shape, BlockPos pos, boolean defaultValue) {
        return entity.getY() > (double)pos.getY() + shape.max(Direction.Axis.Y) - (entity.onGround() ? 8.05/16.0 : 0.0015);
    }

    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    private void preventCollisionWhenPhasing(Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        for (PhasingPower phasingPower : PowerHolderComponent.getPowers(entity, PhasingPower.class)) {
            if(phasingPower.doesApply(pos)) {
                ci.cancel();
            }
        }
    }
}
