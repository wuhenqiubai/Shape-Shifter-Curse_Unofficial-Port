package net.onixary.shapeShifterCurseFabric.mixin.projectile;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.onixary.shapeShifterCurseFabric.additional_power.SnowballBlockTransformPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class) // 改为直接注入Entity类
public abstract class EntitySnowballTransformMixin {

    @Shadow
    public abstract Level level();

    @Shadow
    public abstract BlockPos blockPosition();


    @Unique
    private boolean hasTransformedFluid = false;

    // 在实体tick方法中检测流体
    @Inject(method = "tick", at = @At("HEAD"))
    private void checkFluidCollision(CallbackInfo ci) {
        // 检查当前实体是否为雪球
        if (!((Object) this instanceof Snowball)) {
            return;
        }

        Snowball snowball = (Snowball) (Object) this;
	    Level world = snowball.level();

        // 避免重复转换
        if (hasTransformedFluid || world.isClientSide) {
            return;
        }

        // 检查投掷者权限
        Entity owner = snowball.getOwner();
        if (!(owner instanceof Player player)) {
            return;
        }

        boolean hasTransformPower = PowerHolderComponent.getPowers(player, SnowballBlockTransformPower.class)
                .stream()
                .anyMatch(power -> power.isActive());

        if (!hasTransformPower) {
            return;
        }

        // 获取雪球当前位置
        BlockPos currentPos = snowball.blockPosition();
        FluidState fluidState = world.getFluidState(currentPos);

        // 检测是否在流体中
        if (!fluidState.isEmpty()) {
            transformFluidBlock(world, currentPos, fluidState);
            hasTransformedFluid = true;

            // 销毁雪球（模拟碰撞效果）
            if (!world.isClientSide) {
                snowball.level().broadcastEntityEvent(snowball, (byte) 3); // 粒子效果
                snowball.discard();
            }
        }
    }

    // 在流体更新方法中检测
    @Inject(method = "updateInWaterStateAndDoFluidPushing", at = @At("HEAD"))
    private void onEnterWater(CallbackInfoReturnable<Boolean> cir) {
        // 检查当前实体是否为雪球
        if (!((Object) this instanceof Snowball)) {
            return;
        }

        Snowball snowball = (Snowball) (Object) this;
	    Level world = snowball.level();

        if (hasTransformedFluid || world.isClientSide) {
            return;
        }

        Entity owner = snowball.getOwner();
        if (!(owner instanceof Player player)) {
            return;
        }

        boolean hasTransformPower = PowerHolderComponent.getPowers(player, SnowballBlockTransformPower.class)
                .stream()
                .anyMatch(power -> power.isActive());

        if (!hasTransformPower) {
            return;
        }

        // 检查雪球是否刚进入流体
        BlockPos pos = snowball.blockPosition();
        FluidState fluidState = world.getFluidState(pos);

        if (!fluidState.isEmpty()) {
            transformFluidBlock(world, pos, fluidState);
            hasTransformedFluid = true;
        }
    }

    @Unique
    private void transformFluidBlock(Level world, BlockPos pos, FluidState fluidState) {
        BlockState currentState = world.getBlockState(pos);

        // 处理水转冰
        if (fluidState.is(FluidTags.WATER)) {
            world.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
            world.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
            world.playSound(null, pos, SoundEvents.BUCKET_EMPTY_POWDER_SNOW, SoundSource.BLOCKS, 0.8f, 1.2f);
            world.playSound(null, pos, SoundEvents.SNOW_PLACE, SoundSource.BLOCKS, 0.6f, 1.5f);
        }
        // 处理岩浆转换
        else if (fluidState.is(FluidTags.LAVA)) {
            BlockState newState;

            if (fluidState.isSource()) {
                newState = Blocks.OBSIDIAN.defaultBlockState();
                world.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 0.8f);
            } else {
                newState = Blocks.STONE.defaultBlockState();
                world.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.8f, 1.0f);
            }

            world.setBlockAndUpdate(pos, newState);
            world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6f, 1.5f);
        }
    }
}
