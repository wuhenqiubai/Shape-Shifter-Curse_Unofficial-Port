package io.github.apace100.apoli.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class GainedPowerCriterion extends SimpleCriterionTrigger<GainedPowerCriterion.Conditions> {

    public static GainedPowerCriterion INSTANCE = new GainedPowerCriterion();

    private static final Identifier ID = Apoli.identifier("gained_power");

    public void trigger(ServerPlayer player, PowerType type) {
        this.trigger(player, (conditions -> conditions.matches(type)));
    }

    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public static class Conditions implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Conditions> CODEC =  RecordCodecBuilder.create(instance ->
            instance.group(
                    EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player")
                        .forGetter(Conditions::player),
                    Identifier.CODEC.fieldOf("power")
                        .forGetter(c -> c.powerId)
                )
                .apply(instance, Conditions::new)
        );

        private final Optional<ContextAwarePredicate> player;
        private final Identifier powerId;

        public Conditions(Optional<ContextAwarePredicate> player, Identifier powerId) {
            this.player = player;
            this.powerId = powerId;
        }

        public boolean matches(PowerType powerType) {
            return powerType.getIdentifier().equals(powerId);
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return player;
        }
    }
}
