package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.networking.PowerListPacket;
import io.github.apace100.apoli.power.ModifyPlayerSpawnPower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.ladysnake.cca.api.v3.component.ComponentProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@SuppressWarnings("rawtypes")
@Mixin(value = PlayerList.class, priority = 800)
public abstract class LoginMixin {

	@Shadow public abstract List<ServerPlayer> getPlayers();

	@Inject(at = @At("TAIL"), method = "placeNewPlayer")
	private void syncPowerTypes(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
		ServerPlayNetworking.send(player, new PowerListPacket(PowerTypeRegistry.get()));

		List<ServerPlayer> playerList = getPlayers();
		playerList.forEach(spe -> {
			PowerHolderComponent.KEY.syncWith(spe, (ComponentProvider) player);
			PowerHolderComponent.KEY.syncWith(player, (ComponentProvider) spe);
		});
		PowerHolderComponent.sync(player);
	}

	// TODO O-L: is this broken?
	/*@WrapOperation(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setRespawnPosition(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/BlockPos;FZZ)V"))
	private void preventEndExitSpawnPointSetting(ServerPlayer playerEntity, ResourceKey<Level> dimension, BlockPos position, float angle, boolean forced, boolean sendMessage, Operation<Void> original) {
		EndRespawningEntity ere = (EndRespawningEntity) playerEntity;
		// Prevent setting the spawn point if the player has a "fake" respawn point
		if(ere.hasRealRespawnPoint()) {
			original.call(playerEntity, dimension, position, angle, forced, sendMessage);
		}
	}*/

	@Inject(method = "remove", at = @At("HEAD"))
	private void invokeOnRemovedCallback(ServerPlayer player, CallbackInfo ci) {
		PowerHolderComponent component = PowerHolderComponent.KEY.get(player);
		component.getPowers().forEach(Power::onRemoved);
	}

	@WrapOperation(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;findRespawnPositionAndUseSpawnBlock(ZLnet/minecraft/world/level/portal/DimensionTransition$PostDimensionTransition;)Lnet/minecraft/world/level/portal/DimensionTransition;"))
	private DimensionTransition retryObstructedSpawnpointIfFailed(ServerPlayer instance, boolean keepInventory, DimensionTransition.PostDimensionTransition postDimensionTransition, Operation<DimensionTransition> operation) {
		DimensionTransition original = operation.call(instance, keepInventory, postDimensionTransition);
		if(original.missingRespawnBlock()) {
			if(PowerHolderComponent.hasPower(instance, ModifyPlayerSpawnPower.class)) {
				return new DimensionTransition(instance.serverLevel(), DismountHelper.findSafeDismountLocation(EntityType.PLAYER, original.newLevel(), BlockPos.containing(original.pos()), keepInventory), Vec3.ZERO, original.yRot(), original.xRot(), postDimensionTransition);
			}
		}
		return original;
	}

	@Inject(method = "respawn", at = @At("HEAD"))
	private void invokePowerRemovedCallback(ServerPlayer player, boolean keepInventory, Entity.RemovalReason reason, CallbackInfoReturnable<ServerPlayer> cir) {
		List<Power> powers = PowerHolderComponent.KEY.get(player).getPowers();
		powers.forEach(Power::onRemoved);
	}

	@Inject(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;initInventoryMenu()V"))
	private void invokePowerRespawnCallback(ServerPlayer player, boolean keepInventory, Entity.RemovalReason reason, CallbackInfoReturnable<ServerPlayer> cir, @Local(argsOnly = true) ServerPlayer serverPlayerEntity) {
		if(keepInventory) {
			List<Power> powers = PowerHolderComponent.KEY.get(serverPlayerEntity).getPowers();
			powers.forEach(Power::onRespawn);
		}
	}
}
