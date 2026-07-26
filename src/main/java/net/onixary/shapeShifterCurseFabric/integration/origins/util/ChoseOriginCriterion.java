package net.onixary.shapeShifterCurseFabric.integration.origins.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.Origin;

import java.util.Optional;

public class ChoseOriginCriterion extends AbstractCriterion<ChoseOriginCriterion.Conditions> {

    public static ChoseOriginCriterion INSTANCE = new ChoseOriginCriterion();

    public static final Identifier ID = Identifier.of(Origins.MODID, "chose_origin");

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, Origin origin) {
        this.trigger(player, (conditions -> conditions.matches(origin)));
    }

    public record Conditions(Optional<LootContextPredicate> player, Identifier originId) implements AbstractCriterion.Conditions {

        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
            Identifier.CODEC.fieldOf("origin").forGetter(Conditions::originId)
        ).apply(instance, Conditions::new));


        public boolean matches(Origin origin) {
            return origin.getIdentifier().equals(originId);
        }

    }

}
