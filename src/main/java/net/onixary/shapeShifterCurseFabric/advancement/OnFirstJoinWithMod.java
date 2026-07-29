package net.onixary.shapeShifterCurseFabric.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.util.Optional;

public class OnFirstJoinWithMod extends SimpleCriterionTrigger<OnFirstJoinWithMod.Condition> {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "on_first_join_with_mod");

    public Identifier getId() {
        return ID;
    }

    public void trigger(ServerPlayer player) {
        trigger(player, Condition::requirementsMet);
    }

    @Override
    public Codec<Condition> codec() {
        return Condition.CODEC;
    }

    public record Condition(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Condition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Condition::player)
            ).apply(instance, Condition::new)
        );

        public boolean requirementsMet() {
            return true;
        }
    }
}