package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.util.HashSet;
import java.util.Set;

public class JumpEventCondition {
    // 存储当前帧正在跳跃的玩家
    private static final Set<Player> jumpingPlayers = new HashSet<>();

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof Player playerEntity)) {
            return false;
        }

        return jumpingPlayers.contains(playerEntity);
    }

    public static void setJumping(Player player, boolean jumping) {
        if (jumping) {
            jumpingPlayers.add(player);
        } else {
            jumpingPlayers.remove(player);
        }
    }

    public static void clearJumpState(Player player) {
        jumpingPlayers.remove(player);
    }

    // 每tick清理状态，确保跳跃状态只持续一tick
    public static void tick() {
        jumpingPlayers.clear();
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
                ShapeShifterCurseFabric.identifier("jump_event"),
                new SerializableData(),
                JumpEventCondition::condition
        );
    }
}