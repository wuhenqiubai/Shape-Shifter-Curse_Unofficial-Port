package io.github.apace100.apoli.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import io.github.apace100.apoli.access.EndRespawningEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ActionOnItemUsePower;
import io.github.apace100.apoli.power.KeepInventoryPower;
import io.github.apace100.apoli.power.ModifyPlayerSpawnPower;
import io.github.apace100.apoli.power.PreventSleepPower;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.storage.LevelData;
import org.jetbrains.annotations.Nullable;
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

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin extends Player implements ContainerListener, EndRespawningEntity {

    @Shadow
    @Final
    public MinecraftServer server;

    @Shadow
    public ServerGamePacketListenerImpl connection;

    public ServerPlayerEntityMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Shadow
    public abstract void displayClientMessage(Component message, boolean actionBar);

    @Shadow @Nullable private ServerPlayer.@Nullable RespawnConfig respawnConfig;

    @Shadow
    protected static Optional findRespawnAndUseSpawnBlock(ServerLevel level, ServerPlayer.RespawnConfig respawnConfig, boolean useCharge) {
        throw new AssertionError();
    }

    // FRESH_AIR
    @Inject(method = "startSleepInBed", at = @At(value = "INVOKE",target = "Lnet/minecraft/server/level/ServerPlayer;setRespawnPosition(Lnet/minecraft/server/level/ServerPlayer$RespawnConfig;Z)V"), cancellable = true)
    public void preventAvianSleep(BlockPos pos, CallbackInfoReturnable<Either<BedSleepingProblem, Unit>> info) {
        PowerHolderComponent.getPowers(this, PreventSleepPower.class).forEach(p -> {
                if(p.doesPrevent(level(), pos)) {
                    if(p.doesAllowSpawnPoint()) {
                        ((ServerPlayer)(Object)this).setRespawnPosition(new ServerPlayer.RespawnConfig(new LevelData.RespawnData(GlobalPos.of(this.level().dimension(), pos), this.getYRot(), this.getXRot()), false), true);
                    }
                    info.setReturnValue(Either.left(null));
                    this.displayClientMessage(Component.translatable(p.getMessage()), true);
                }
            }
        );
    }

    @Inject(at = @At("RETURN"), method = "getRespawnConfig", cancellable = true)
    private void modifySpawnPointConfig(CallbackInfoReturnable<ServerPlayer.RespawnConfig> info) {
        if (!this.origins_isEndRespawning && PowerHolderComponent.getPowers(this, ModifyPlayerSpawnPower.class).size() > 0) {
            ModifyPlayerSpawnPower power = PowerHolderComponent.getPowers(this, ModifyPlayerSpawnPower.class).get(0);

            BlockPos spawnPos = findPlayerSpawn();

            if (respawnConfig == null) {
                info.setReturnValue(new ServerPlayer.RespawnConfig(new LevelData.RespawnData(GlobalPos.of(power.dimension, spawnPos), 0f, 0f), true));
            } else if (hasObstructedSpawn(respawnConfig.respawnData().dimension())) {
//                connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
                info.setReturnValue(new ServerPlayer.RespawnConfig(new LevelData.RespawnData(GlobalPos.of(power.dimension, spawnPos), 0f, 0f), true));
            }
        }
    }

    @Inject(method = "restoreFrom", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/server/level/ServerPlayer;enchantmentSeed:I"))
    private void copyInventoryWhenKeeping(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        if(PowerHolderComponent.hasPower(oldPlayer, KeepInventoryPower.class)) {
            this.getInventory().replaceWith(oldPlayer.getInventory());
        }
    }

    @Unique
    private boolean hasObstructedSpawn(ResourceKey<Level> dimension) {
        ServerLevel world = server.getLevel(dimension);
        if(respawnConfig != null && world != null) {
            return findRespawnAndUseSpawnBlock(world, respawnConfig, false).isEmpty();
        }
        return false;
    }

    @Unique
    private BlockPos findPlayerSpawn() {
        ModifyPlayerSpawnPower power = PowerHolderComponent.getPowers(this, ModifyPlayerSpawnPower.class).get(0);
        Tuple<ServerLevel, BlockPos> spawn = power.getSpawn(true);
        if(spawn != null) {
            return spawn.getB();
        }
        return BlockPos.ZERO;
    }

    @Unique
    private ItemStack apoli$stackBeforeDrop;

    @Inject(method = "drop(Z)V", at = @At("HEAD"))
    private void cacheItemStackBeforeDropping(boolean entireStack, CallbackInfo ci) {
        apoli$stackBeforeDrop = this.getInventory().getSelectedItem().copy();
    }

    @Inject(method = "drop(Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;findSlot(Lnet/minecraft/world/Container;I)Ljava/util/OptionalInt;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void checkItemUsageStopping(boolean entireStack, CallbackInfo ci, Inventory playerInventory, ItemStack itemStack) {
        if(this.isUsingItem() && !ItemStack.isSameItem(apoli$stackBeforeDrop, this.getInventory().getSelectedItem())) {
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
        return respawnConfig != null && !hasObstructedSpawn(respawnConfig.respawnData().dimension());
    }
}
