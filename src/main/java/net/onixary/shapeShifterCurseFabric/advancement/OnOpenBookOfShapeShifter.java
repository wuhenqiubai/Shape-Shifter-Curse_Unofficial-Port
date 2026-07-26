package net.onixary.shapeShifterCurseFabric.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.util.Optional;

public class OnOpenBookOfShapeShifter extends SimpleCriterionTrigger<OnOpenBookOfShapeShifter.Condition> {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "on_open_book_of_shape_shifter");

    public ResourceLocation getId() {
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
