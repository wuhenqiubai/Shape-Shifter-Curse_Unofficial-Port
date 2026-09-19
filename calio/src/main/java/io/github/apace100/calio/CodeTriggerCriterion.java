package io.github.apace100.calio;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class CodeTriggerCriterion extends SimpleCriterionTrigger<CodeTriggerCriterion.Conditions> {

    public static final CodeTriggerCriterion INSTANCE = new CodeTriggerCriterion();

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("apacelib", "code_trigger");

    public ResourceLocation getId() {
        return ID;
    }

    public void trigger(ServerPlayer player, String triggeredId) {
        this.trigger(player, (conditions) -> conditions.matches(triggeredId));
    }

    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public record Conditions(Optional<ContextAwarePredicate> player, String triggerId) implements SimpleInstance {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player")
                    .forGetter(Conditions::player),
                Codec.STRING.fieldOf("trigger_id")
                    .forGetter(Conditions::triggerId)
            )
                .apply(instance, Conditions::new)
        );

        public static CodeTriggerCriterion.Conditions trigger(String triggerId) {
                return new CodeTriggerCriterion.Conditions(Optional.empty(), triggerId);
            }

            public boolean matches(String triggered) {
                return this.triggerId.equals(triggered);
            }
        }
}
