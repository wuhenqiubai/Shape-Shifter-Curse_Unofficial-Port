package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;

public class ScoreboardCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        String name = data.getString("name");
        if (name == null) {
            if (entity instanceof Player playerEntity) name = playerEntity.getName().getString();
            else name = entity.getStringUUID();
        }

        Scoreboard scoreboard = entity.level().getScoreboard();
        Objective scoreboardObjective = scoreboard.getObjective(data.getString("objective"));
        if (scoreboardObjective == null)
            return false;

        if (scoreboard.listPlayerScores(ScoreHolder.forNameOnly(name)).containsKey(scoreboardObjective)) {
            int score = scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly(name), scoreboardObjective).get();
            return ((Comparison) data.get("comparison")).compare(score, data.getInt("compare_to"));
        }

        return false;
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(Apoli.identifier("scoreboard"),
            new SerializableData()
                .add("name", SerializableDataTypes.STRING, null)
                .add("objective", SerializableDataTypes.STRING)
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.INT),
            ScoreboardCondition::condition
        );
    }
}
