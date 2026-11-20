package net.onixary.shapeShifterCurseFabric.integration.origins.util;

import net.minecraft.core.Holder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.Origin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ChoseOriginCriterion extends SimpleCriterionTrigger<ChoseOriginCriterion.Conditions> {

    public static ChoseOriginCriterion INSTANCE = new ChoseOriginCriterion();

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Origins.MODID, "chose_origin");

    @Override
    public @NotNull Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayer player, Origin origin) {
        this.trigger(player, (conditions -> conditions.matches(origin)));
    }

    public record Conditions(Optional<Holder<LootItemCondition>> player, Identifier originId) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
            Identifier.CODEC.fieldOf("origin").forGetter(Conditions::originId)
        ).apply(instance, Conditions::new));


        public boolean matches(Origin origin) {
            return origin.getIdentifier().equals(originId);
        }

    }

}