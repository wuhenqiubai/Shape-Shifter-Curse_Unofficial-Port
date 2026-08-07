package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimFSM.FSMUtils;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateController.IdleStayAnimController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimSystem;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;

import java.util.HashMap;
import java.util.UUID;

// 判定玩家当前是否处于Idle Stay(静止Idle达到该形态配置的阈值) 服务端复算 与客户端动画逐帧一致
public class IdleStayCondition {
    // 每玩家静止Idle累计tick数 服务端复算
    private static final HashMap<UUID, Long> idleStayTickCounterMap = new HashMap<>();
    // 每玩家上一tick位置 用于计算IsWalking
    private static final HashMap<UUID, Vec3> lastPositionMap = new HashMap<>();

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                tickIdleStayCounter(player);
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            idleStayTickCounterMap.remove(handler.player.getUUID());
            lastPositionMap.remove(handler.player.getUUID());
        });
    }

    private static void tickIdleStayCounter(ServerPlayer player) {
        Vec3 lastPos = lastPositionMap.getOrDefault(player.getUUID(), player.position());
        AnimSystem.AnimSystemData data = new AnimSystem.AnimSystemData(player);
        data.playerForm = FormUtils.getPlayerForm(player);
        data.IsOnGround = AnimSystem.checkOnGroundSuper(player);
        data.IsWalking = !lastPos.equals(player.position());
        if (FSMUtils.IsIdleStayCondition(player, data)) {
            idleStayTickCounterMap.merge(player.getUUID(), 1L, Long::sum);
        } else {
            idleStayTickCounterMap.remove(player.getUUID());
        }
        lastPositionMap.put(player.getUUID(), player.position());
    }

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof ServerPlayer player)) {
            return false;
        }
        // 形态检测:当前形态是否为ANIM_STATE_IDLE配置了IdleStayAnimController 未注册则永远为false
        IForm form = FormUtils.getPlayerForm(player);
        AnimSystem.AnimSystemData animData = new AnimSystem.AnimSystemData(player);
        animData.playerForm = form;
        IdleStayAnimController idleStayController = FSMUtils.GetIdleStayController(player, animData);
        if (idleStayController == null) {
            return false;
        }
        long counter = idleStayTickCounterMap.getOrDefault(player.getUUID(), 0L);
        return counter >= idleStayController.getStayTickThreshold();
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
                ShapeShifterCurseFabric.identifier("idle_stay"),
                new SerializableData(),
                IdleStayCondition::condition
        );
    }
}