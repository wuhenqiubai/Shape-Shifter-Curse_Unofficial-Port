package io.github.apace100.apoli.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class GainedPowerCriterion extends SimpleCriterionTrigger<GainedPowerCriterion.Conditions> {

    public static GainedPowerCriterion INSTANCE = new GainedPowerCriterion();

    private static final ResourceLocation ID = Apoli.identifier("gained_power");

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
                    ResourceLocation.CODEC.fieldOf("power")
                        .forGetter(c -> c.powerId)
                )
                .apply(instance, Conditions::new)
        );

        private final Optional<ContextAwarePredicate> player;
        private final ResourceLocation powerId;

        public Conditions(Optional<ContextAwarePredicate> player, ResourceLocation powerId) {
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
