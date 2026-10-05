package net.onixary.shapeShifterCurseFabric.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FriendlyByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.additional_power.ActionOnJumpPower;
import net.onixary.shapeShifterCurseFabric.additional_power.BatBlockAttachPower;
import net.onixary.shapeShifterCurseFabric.additional_power.SlowdownPercentPower;
import net.onixary.shapeShifterCurseFabric.additional_power.SneakingJumpClashPower;
import net.onixary.shapeShifterCurseFabric.additional_power.SprintingStateTracker;
import net.onixary.shapeShifterCurseFabric.networking.BytePayload;
import net.onixary.shapeShifterCurseFabric.networking.ModPackets;
import net.onixary.shapeShifterCurseFabric.util.Interface.IMoveController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Player.class)
public class PlayerMovementControlMixin implements IMoveController {

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void preventTravelWhenAttached(Vec3 movementInput, CallbackInfo ci) {
        Player player = (Player) (Object) this;

        // 添加空值检查
        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(player);
        if (component == null) {
            return; // 组件未初始化，跳过处理
        }

        BatBlockAttachPower attachPower = PowerHolderComponent.getPowers(player, BatBlockAttachPower.class)
                .stream()
                .filter(BatBlockAttachPower::isAttached)
                .findFirst()
                .orElse(null);

        if (attachPower != null) {
            // 1.21.11 中 jumpFromGround 已上移到 LivingEntity（Player 不再覆写），此处无法再拦截 Player 的跳跃。
            // 改在 travel 内检测跳跃输入：吸附状态下按住跳跃键即向服务器请求脱离吸附（服务器侧幂等，重复发送安全）。
            if (player.isJumping() && player.level().isClientSide()) {
                FriendlyByteBuf buf = FriendlyByteBufs.create();
                ClientPlayNetworking.send(new BytePayload(BytePayload.id(ModPackets.JUMP_DETACH_REQUEST_ID), buf));
            }

            // 完全取消移动，类似蜂蜜块的效果
            player.setDeltaMovement(0, 0, 0);
            ci.cancel();
        }
    }

    @Inject(method = "getSpeed()F", at = @At("RETURN"), cancellable = true)
    private void zeroMovementSpeedWhenAttached(CallbackInfoReturnable<Float> cir) {
        if (noMoveTick > 0) {
            cir.setReturnValue(0.0f);
        }
        Player player = (Player) (Object) this;

        // 添加空值检查
        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(player);
        if (component == null) {
            return; // 组件未初始化，跳过处理
        }

	    PowerHolderComponent.getPowers(player, BatBlockAttachPower.class)
			    .stream()
			    .filter(BatBlockAttachPower::isAttached)
			    .findFirst().ifPresent(attachPower -> cir.setReturnValue(0.0f));
    }

    // [已迁移，勿再当作待办] jumpFromGround 上移至 LivingEntity 后，@Mixin(Player.class) 解析不到该注入点。
    // jump_event 条件 / JUMP_EVENT 包 / ActionOnJumpPower 的触发已迁至 LivingEntityJumpMixin
    // （@Mixin(LivingEntity.class) 注入 LivingEntity.jumpFromGround，并在 handler 内 instanceof Player 过滤）。
    // 其中「服务端权威执行 ActionOnJumpPower / SneakingJumpClashPower、不依赖 C2S 包往返」的修复
    // 也一并保留在 LivingEntityJumpMixin（见其 else 分支）。
    // 吸附状态下的跳跃脱离另由 preventTravelWhenAttached 通过 player.isJumping() 检测。
    // 下列旧实现保留作参考。
    // @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    // private void handleJump(CallbackInfo ci) {
    //     Player player = (Player) (Object) this;
    //
    //     // 添加空值检查
    //     PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(player);
    //     if (component == null) {
    //         return; // 组件未初始化，跳过处理
    //     }
    //
    //     BatBlockAttachPower attachPower = PowerHolderComponent.getPowers(player, BatBlockAttachPower.class)
    //             .stream()
    //             .filter(BatBlockAttachPower::isAttached)
    //             .findFirst()
    //             .orElse(null);
    //
    //     if (attachPower != null) {
    //         // 处理跳跃取消吸附
    //         if (player.level().isClientSide()) {
    //             FriendlyByteBuf buf = PacketByteBufs.create();
    //             ClientPlayNetworking.send(new BytePayload(BytePayload.id(ModPackets.JUMP_DETACH_REQUEST_ID),  buf));
    //         }
    //         ci.cancel();
    //     }
    //
    //     // handle jump_event condition
    //     JumpEventCondition.setJumping(player, true);
    //
    //     // 发送网络包到服务器
    //     if (player.level().isClientSide()) {
    //         FriendlyByteBuf buf = PacketByteBufs.create();
    //         buf.writeUUID(player.getUUID());
    //         ClientPlayNetworking.send(new BytePayload(BytePayload.id(ModPackets.JUMP_EVENT_ID),  buf));
    //     }
    // }

    @Inject(method = "tryToStartFallFlying", at = @At("HEAD"), cancellable = true)
    private void preventElytraCheckWhenAttached(CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;

        // 添加空值检查
        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(player);
        if (component == null) {
            return; // 组件未初始化，跳过处理
        }

        BatBlockAttachPower attachPower = PowerHolderComponent.getPowers(player, BatBlockAttachPower.class)
                .stream()
                .filter(BatBlockAttachPower::isAttached)
                .findFirst()
                .orElse(null);

        if (attachPower != null) {

            if (player.level().isClientSide()) {
                FriendlyByteBuf buf = FriendlyByteBufs.create();
                ClientPlayNetworking.send(new BytePayload(BytePayload.id(ModPackets.JUMP_DETACH_REQUEST_ID),  buf));
            }

            // 重置鞘翅相关标志
            player.stopFallFlying();
            // 强制设置为在地面上，这样空格键就不会触发鞘翅
            player.setOnGround(true);
            // 取消鞘翅检测
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void trackSprintingState(CallbackInfo ci) {
        Player player = (Player) (Object) this;

        boolean wasSprintingLastTick = SprintingStateTracker.wasSprintingLastTick(player);
        boolean isCurrentlySprinting = player.isSprinting();
        boolean isCurrentlySneaking = player.isShiftKeyDown();

        // 先更新疾跑状态（这会在开始疾跑时重置触发标志）
        SprintingStateTracker.updateSprintingState(player, isCurrentlySprinting);

        // 检查从疾跑转为潜行的条件
        if (wasSprintingLastTick  && isCurrentlySneaking && SprintingStateTracker.canTrigger(player)) {
            ShapeShifterCurseFabric.LOGGER.info("Triggering sprint-to-sneak action for player: {}", player.getName().getString());

            // 设置已触发标志
            SprintingStateTracker.setTriggered(player);

            // 发送网络包到服务器
            if (player.level().isClientSide()) {
                FriendlyByteBuf buf = FriendlyByteBufs.create();
                buf.writeUUID(player.getUUID());
                ClientPlayNetworking.send(new BytePayload(BytePayload.id(ModPackets.SPRINTING_TO_SNEAKING_EVENT_ID),  buf));
            }
        }
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void cleanupSprintingState(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        SprintingStateTracker.removePlayer(player);
    }

    @ModifyVariable(method = "makeStuckInBlock", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private Vec3 SlowdownPercentMixin(Vec3 multiplier) {
        Player player = (Player) (Object) this;
        List<SlowdownPercentPower> slowdownPower = PowerHolderComponent.getPowers(player, SlowdownPercentPower.class);
        float slowdownPercent = 1.0f;
        for (SlowdownPercentPower power : slowdownPower) {
            slowdownPercent *= power.Multiplier;
        }
        return multiplier.scale(slowdownPercent);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (this.noMoveTick > 0) {
            this.noMoveTick--;
        }
    }

    @Unique
    public int noMoveTick = 0;

    @Override
    public void shape_shifter_curse$setNoMoveTick(int tick) {
        this.noMoveTick = tick;
    }

    @Override
    public int shape_shifter_curse$getNoMoveTick() {
        return this.noMoveTick;
    }

}