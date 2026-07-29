package net.onixary.shapeShifterCurseFabric.integration.origins.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.Origin;

import java.util.Optional;

public class ChoseOriginCriterion extends SimpleCriterionTrigger<ChoseOriginCriterion.Conditions> {

    public static ChoseOriginCriterion INSTANCE = new ChoseOriginCriterion();

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Origins.MODID, "chose_origin");

    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayer player, Origin origin) {
        this.trigger(player, (conditions -> conditions.matches(origin)));
    }

    public record Conditions(Optional<ContextAwarePredicate> player, Identifier originId) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
            Identifier.CODEC.fieldOf("origin").forGetter(Conditions::originId)
        ).apply(instance, Conditions::new));


        public boolean matches(Origin origin) {
            return origin.getIdentifier().equals(originId);
        }

    }

}