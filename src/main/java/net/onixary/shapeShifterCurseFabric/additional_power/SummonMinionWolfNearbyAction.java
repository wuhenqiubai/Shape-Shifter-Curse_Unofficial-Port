package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.minion.IPlayerEntityMinion;
import net.onixary.shapeShifterCurseFabric.minion.MinionRegister;
import net.onixary.shapeShifterCurseFabric.minion.mobs.AnubisWolfMinionEntity;

public class SummonMinionWolfNearbyAction {
    public static void action(SerializableData.Instance data, Tuple<Entity, Entity> entities) {
        Entity Owner = entities.getA();
        Entity SpawnNearbyTarget = entities.getB();
        if (data.isPresent("reverse") && data.getBoolean("reverse")) {
            Owner = entities.getB();
            SpawnNearbyTarget = entities.getA();
        }
        int MinionLevel = data.getInt("minion_level");
        int MinionCount = data.getInt("count");
        int MaxMinionCount = data.getInt("max_minion_count");
        int Cooldown = data.getInt("cooldown");
        ActionFactory<Entity>.Instance OwnerAction = data.get("owner_action");
        ActionFactory<Entity>.Instance TargetAction = data.get("target_action");
        if (Owner instanceof ServerPlayer player) {
            boolean IsSummonSuccess = false;
            for (int i = 0; i < MinionCount; i++) {
                if (player instanceof IPlayerEntityMinion playerEntityMinion) {
                    if (playerEntityMinion.shape_shifter_curse$getMinionsCount(AnubisWolfMinionEntity.MinionID) >= MaxMinionCount) {
                        return;
                    }
                    if (MinionRegister.IsInCoolDown(AnubisWolfMinionEntity.MinionID, player, Cooldown)) {
                        return;
                    }
                }
                else {
                    ShapeShifterCurseFabric.LOGGER.warn("Can't spawn minion, player is not IPlayerEntityMinion");
                    return;
                }
                BlockPos targetPos = MinionRegister.getNearbyEmptySpace(SpawnNearbyTarget.level(), player.getRandom(), SpawnNearbyTarget.blockPosition(), 3, 1, 1, 4);
                if (targetPos == null) {
                    targetPos = SpawnNearbyTarget.blockPosition();
                }
                if (SpawnNearbyTarget.level() instanceof ServerLevel world) {
                    AnubisWolfMinionEntity anubisWolfMinionEntity = MinionRegister.SpawnMinion(MinionRegister.ANUBIS_WOLF_MINION, world, targetPos, player);
                    if (anubisWolfMinionEntity != null) {
                        anubisWolfMinionEntity.setMinionLevel(MinionLevel);
                        IsSummonSuccess = true;
                    } else {
                        ShapeShifterCurseFabric.LOGGER.warn("Can't spawn minion, wolfMinion is null");
                    }
                } else {
                    ShapeShifterCurseFabric.LOGGER.warn("Can't spawn minion, world is not ServerWorld");
                }
            }
            if (IsSummonSuccess) {
                MinionRegister.SetCoolDown(AnubisWolfMinionEntity.MinionID, player);
                if (OwnerAction != null) {
                    OwnerAction.accept(Owner);
                }
                if (TargetAction != null) {
                    TargetAction.accept(SpawnNearbyTarget);
                }
                // 添加音效与粒子效果
                if (!(player.level() instanceof ServerLevel serverWorld)) {
                    return;
                }
                player.level().playSound(null, player.blockPosition(), SoundEvents.WOLF_STEP, player.getSoundSource(), 1.0f, 1.5f);
                var packet = new net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket(
                        ParticleTypes.SOUL_FIRE_FLAME, true, false,
                        player.blockPosition().getX() + 0.5, player.blockPosition().getY() + 0.5, player.blockPosition().getZ() + 0.5,
                        0, 0, 0, 0, 8);
                for (ServerPlayer p : serverWorld.players()) {
                    serverWorld.sendParticles(p, true, player.blockPosition().getX() + 0.5, player.blockPosition().getY() + 0.5, player.blockPosition().getZ() + 0.5, packet);
                }
            }
        }
    }

    public static ActionFactory<Tuple<Entity, Entity>> createBIFactory() {
        return new ActionFactory<>(
                ShapeShifterCurseFabric.identifier("bi_summon_anubis_wolf_minion"),
                new SerializableData()
                        .add("minion_level", SerializableDataTypes.INT, 1)
                        .add("count", SerializableDataTypes.INT, 1)
                        .add("max_minion_count", SerializableDataTypes.INT, Integer.MAX_VALUE)
                        .add("cooldown", SerializableDataTypes.INT, 0)
                        .add("owner_action", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("target_action", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("reverse", SerializableDataTypes.BOOLEAN, false),
                SummonMinionWolfNearbyAction::action
        );
    }

    public static ActionFactory<Entity> createFactory() {
        return new ActionFactory<>(
                ShapeShifterCurseFabric.identifier("summon_anubis_wolf_minion"),
                new SerializableData()
                        .add("minion_level", SerializableDataTypes.INT, 1)
                        .add("count", SerializableDataTypes.INT, 1)
                        .add("max_minion_count", SerializableDataTypes.INT, Integer.MAX_VALUE)
                        .add("cooldown", SerializableDataTypes.INT, 0)
                        .add("owner_action", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("target_action", ApoliDataTypes.ENTITY_ACTION, null)  // 没用 但是防止解析错误 但是会正常执行
                        .add("reverse", SerializableDataTypes.BOOLEAN, false),  // 没用 但是防止解析错误
		        (data, entity) -> action(data, new Tuple<>(entity, entity))
        );
    }
}
