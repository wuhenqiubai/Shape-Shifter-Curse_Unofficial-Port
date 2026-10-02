package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FriendlyByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.additional_power.ActionOnJumpPower;
import net.onixary.shapeShifterCurseFabric.additional_power.JumpEventCondition;
import net.onixary.shapeShifterCurseFabric.additional_power.SneakingJumpClashPower;
import net.onixary.shapeShifterCurseFabric.additional_power.TripleJumpPower;
import net.onixary.shapeShifterCurseFabric.networking.BytePayload;
import net.onixary.shapeShifterCurseFabric.networking.ModPackets;
import net.onixary.shapeShifterCurseFabric.util.Interface.IJumpController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityJumpMixin implements IJumpController {

    // 注入到 jump() 方法的开头，用于更新 Power 的状态
    @Inject(method = "jumpFromGround", at = @At("HEAD"))
    private void onJump(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        // 在计算跳跃速度之前，先让 Power 更新它的内部状态（如跳跃次数）
        PowerHolderComponent.getPowers(entity, TripleJumpPower.class).forEach(TripleJumpPower::onJump);
        // 1.21.11 恢复 jump_event 条件 + JUMP_EVENT 包：jumpFromGround 上移到 LivingEntity，
        // 原 PlayerMovementControlMixin.handleJump（@Mixin(Player) 注入 Player.jumpFromGround）失效
        if (entity instanceof Player player) {
            JumpEventCondition.setJumping(player, true);
            if (player.level().isClientSide()) {
                FriendlyByteBuf buf = FriendlyByteBufs.create();
                buf.writeUUID(player.getUUID());
                ClientPlayNetworking.send(new BytePayload(BytePayload.id(ModPackets.JUMP_EVENT_ID), buf));
            } else {
                // 服务端权威执行，不要再绕 JUMP_EVENT 包。
                // 1.21.11 里客户端对普通跳跃不再走 jumpFromGround（只有切换飞行能力时才会），
                // 跳跃改由服务端在处理 ServerboundMovePlayerPacket 时调用 jumpFromGround()
                // （ServerGamePacketListenerImpl：以 player.onGround() 为真为前提）——
                // 此刻玩家仍在地面，条件里的 apoli:on_block 能通过。
                // 若改回「客户端发包、服务端收包后执行」，包到达时跳跃已被应用、玩家已离地，
                // on_block 必然为假（实测 pass=false, onGround=false），action 永不执行。
                PowerHolderComponent.getPowers(player, ActionOnJumpPower.class).forEach(ActionOnJumpPower::executeAction);
                PowerHolderComponent.getPowers(player, SneakingJumpClashPower.class).forEach(sneakingJumpClashPower -> sneakingJumpClashPower.jumpTicks = 5);
            }
        }
    }

    // ⚠ 必须带完整描述符：LivingEntity 上 getJumpPower 有无参与带参两个重载
    // （getJumpPower() / getJumpPower(float)），只写方法名会被 IDEA 判为 ambiguous，
    // 运行时也可能匹配到错误目标。这里要的是无参版本。
    @ModifyReturnValue(method = "getJumpPower()F", at = @At("RETURN"))
    private float modifyJumpVelocity(float originalVelocity) {
        LivingEntity entity = (LivingEntity) (Object) this;

        return PowerHolderComponent.getPowers(entity, TripleJumpPower.class).stream()
                .findFirst()
                .map(power -> {
                    float powerMultiplier = power.getActiveJumpMultiplier();

                    if (powerMultiplier != 1.0f) {
                        // 假设基础跳跃速度为 0.42F
                        float baseJumpVelocity = 0.42F;
                        // 计算额外效果（如跳跃提升）
                        float additionalVelocity = originalVelocity - baseJumpVelocity;
                        // 只对基础速度应用倍率，保持额外效果不变
                        return (baseJumpVelocity * powerMultiplier) + additionalVelocity;
                    }
                    return originalVelocity;
                })
                .orElse(originalVelocity);
    }

    @Inject(method = "getJumpPower()F", at = @At("HEAD"), cancellable = true)
    private void onGetJumpVelocity(CallbackInfoReturnable<Float> cir) {
        if (this.noJumpTick > 0) {
            cir.setReturnValue(0.0F);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (this.noJumpTick > 0) {
            this.noJumpTick--;
        }
    }

    @Unique
    public int noJumpTick = 0;

    @Override
    public void shape_shifter_curse$setNoJumpTick(int tick) {
        this.noJumpTick = tick;
    }

    @Override
    public int shape_shifter_curse$getNoJumpTick() {
        return this.noJumpTick;
    }
}