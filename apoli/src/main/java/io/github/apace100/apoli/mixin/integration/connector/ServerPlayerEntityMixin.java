package io.github.apace100.apoli.mixin.integration.connector;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import io.github.apace100.apoli.access.EndRespawningEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ActionOnItemUsePower;
import io.github.apace100.apoli.power.KeepInventoryPower;
import io.github.apace100.apoli.power.ModifyPlayerSpawnPower;
import io.github.apace100.apoli.power.PreventSleepPower;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Tuple;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

/**
 * NeoForge/Connector 兼容：主 ServerPlayerEntityMixin 的 preventAvianSleep 用
 * {@code @Inject(method = "startSleepInBed", at = @At(value = "INVOKE", target = "...setRespawnPosition(...)V"))}
 * 依赖 NeoForge 重编译后可能变化的字节码，此版本改为 startSleepInBed HEAD（方法名/描述符两版一致，最稳）。
 * 其余逻辑与主版完全一致。由 ApoliMixinPlugin 在 NeoForge 下启用、Fabric 下跳过。
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin extends Player implements ContainerListener, EndRespawningEntity {

    @Shadow
    private ResourceKey<Level> respawnDimension;

    @Shadow
    private BlockPos respawnPosition;

    @Shadow
    @Final
    public MinecraftServer server;

    @Shadow
    public ServerGamePacketListenerImpl connection;

    public ServerPlayerEntityMixin(Level world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow
    public abstract void displayClientMessage(Component message, boolean actionBar);

    @Shadow private boolean respawnForced;

    @Shadow
    private static Optional findRespawnAndUseSpawnBlock(ServerLevel level, BlockPos pos, float angle, boolean forced, boolean keepInventory) {
        throw new IllegalStateException();
    };

    // FRESH_AIR
    // NeoForge 安全注入点：原主版 @Inject(value = "INVOKE", target = "...setRespawnPosition(...)V") 依赖 NeoForge 重编译后可能变化的调用点。
    // 改 startSleepInBed HEAD——PreventSleepPower 激活时直接取消入睡并设置重生点，两版方法名/描述符一致。
    @Inject(method = "startSleepInBed", at = @At("HEAD"), cancellable = true)
    public void preventAvianSleep(BlockPos pos, CallbackInfoReturnable<Either<BedSleepingProblem, Unit>> info) {
        PowerHolderComponent.getPowers(this, PreventSleepPower.class).forEach(p -> {
                if(p.doesPrevent(level(), pos)) {
                    if(p.doesAllowSpawnPoint()) {
                        ((ServerPlayer)(Object)this).setRespawnPosition(this.level().dimension(), pos, this.getYRot(), false, true);
                    }
                    info.setReturnValue(Either.left(null));
                    this.displayClientMessage(Component.translatable(p.getMessage()), true);
                }
            }
        );
    }

    @Inject(at = @At("HEAD"), method = "getRespawnDimension", cancellable = true)
    private void modifySpawnPointDimension(CallbackInfoReturnable<ResourceKey<Level>> info) {
        if (!this.origins_isEndRespawning && (respawnPosition == null || hasObstructedSpawn()) && PowerHolderComponent.getPowers(this, ModifyPlayerSpawnPower.class).size() > 0) {
            ModifyPlayerSpawnPower power = PowerHolderComponent.getPowers(this, ModifyPlayerSpawnPower.class).get(0);
            info.setReturnValue(power.dimension);
        }
    }

    @Inject(at = @At("HEAD"), method = "getRespawnPosition", cancellable = true)
    private void modifyPlayerSpawnPosition(CallbackInfoReturnable<BlockPos> info) {
        if(!this.origins_isEndRespawning && PowerHolderComponent.getPowers(this, ModifyPlayerSpawnPower.class).size() > 0) {
            if(respawnPosition == null) {
                info.setReturnValue(findPlayerSpawn());
            } else if(hasObstructedSpawn()) {
                connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
                info.setReturnValue(findPlayerSpawn());
            }
        }
    }


    @Inject(at = @At("HEAD"), method = "isRespawnForced", cancellable = true)
    private void modifySpawnPointSet(CallbackInfoReturnable<Boolean> info) {
        if(!this.origins_isEndRespawning && (respawnPosition == null || hasObstructedSpawn()) && PowerHolderComponent.hasPower(this, ModifyPlayerSpawnPower.class)) {
            info.setReturnValue(true);
        }
    }

    @Inject(method = "restoreFrom", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/server/level/ServerPlayer;enchantmentSeed:I"))
    private void copyInventoryWhenKeeping(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        if(PowerHolderComponent.hasPower(oldPlayer, KeepInventoryPower.class)) {
            this.getInventory().replaceWith(oldPlayer.getInventory());
        }
    }

    private boolean hasObstructedSpawn() {
        ServerLevel world = server.getLevel(respawnDimension);
        if(respawnPosition != null && world != null) {
            Optional optional = findRespawnAndUseSpawnBlock(world, respawnPosition, 0f, respawnForced, true);
            return !optional.isPresent();
        }
        return false;
    }

    private BlockPos findPlayerSpawn() {
        ModifyPlayerSpawnPower power = PowerHolderComponent.getPowers(this, ModifyPlayerSpawnPower.class).get(0);
        Tuple<ServerLevel, BlockPos> spawn = power.getSpawn(true);
        if(spawn != null) {
            return spawn.getB();
        }
        return null;
    }

    @Unique
    private ItemStack apoli$stackBeforeDrop;

    @Inject(method = "drop(Z)Z", at = @At("HEAD"))
    private void cacheItemStackBeforeDropping(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        apoli$stackBeforeDrop = this.getInventory().getSelected().copy();
    }

    @Inject(method = "drop(Z)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;findSlot(Lnet/minecraft/world/Container;I)Ljava/util/OptionalInt;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void checkItemUsageStopping(boolean entireStack, CallbackInfoReturnable<Boolean> cir, Inventory playerInventory, ItemStack itemStack) {
        if(this.isUsingItem() && !ItemStack.isSameItem(apoli$stackBeforeDrop, this.getInventory().getSelected())) {
            ActionOnItemUsePower.executeActions(this, itemStack, apoli$stackBeforeDrop,
                ActionOnItemUsePower.TriggerType.STOP, ActionOnItemUsePower.PriorityPhase.ALL);
        }
    }

    @Unique
    private boolean origins_isEndRespawning;

    @Override
    public void setEndRespawning(boolean endSpawn) {
        this.origins_isEndRespawning = endSpawn;
    }

    @Override
    public boolean isEndRespawning() {
        return this.origins_isEndRespawning;
    }

    @Override
    public boolean hasRealRespawnPoint() {
        return respawnPosition != null && !hasObstructedSpawn();
    }
}
