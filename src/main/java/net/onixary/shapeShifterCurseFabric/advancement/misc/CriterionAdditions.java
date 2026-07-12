package net.onixary.shapeShifterCurseFabric.advancement.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public final class CriterionAdditions {

    public static OnTransformForm createOnTransformForm() {
        return new OnTransformForm();
    }

    public static OnWebEntity createOnWebEntity() {
        return new OnWebEntity();
    }

    public static final class OnTransformForm extends AbstractCriterion<OnTransformForm.Cnd> {
        public static final Identifier ID = Identifier.of("shape-shifter-curse", "on_transform_form");
        public Identifier getId() { return ID; }
        public Codec<Cnd> getConditionsCodec() { return Cnd.CODEC; }
        public void trigger(ServerPlayerEntity player) { trigger(player, Cnd::matchesAny); }
        public void trigger(ServerPlayerEntity player, Identifier formID) { trigger(player, c -> c.matches(formID)); }
        public record Cnd(Optional<LootContextPredicate> player, List<String> form, Optional<List<Integer>> formTier, Optional<List<String>> flags, Optional<List<String>> notFlags) implements AbstractCriterion.Conditions {
            public static final Codec<Cnd> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(Cnd::player),
                Codec.STRING.listOf().optionalFieldOf("form", List.of()).forGetter(Cnd::form),
                Codec.INT.listOf().optionalFieldOf("form_tier").forGetter(Cnd::formTier),
                Codec.STRING.listOf().optionalFieldOf("flags").forGetter(Cnd::flags),
                Codec.STRING.listOf().optionalFieldOf("not_flags").forGetter(Cnd::notFlags)
            ).apply(instance, Cnd::new));
            public boolean matchesAny() { return true; }
            public boolean matches(Identifier id) {
                if (form.isEmpty() && formTier.isEmpty() && flags.isEmpty() && notFlags.isEmpty())
                    return true;
                if (!form.isEmpty() && form.stream().anyMatch(f -> id.toString().equals(f)))
                    return true;
                return false;
            }
        }
    }

    public static final class OnWebEntity extends AbstractCriterion<OnWebEntity.Cnd> {
        public static final Identifier ID = Identifier.of("shape-shifter-curse", "on_web_entity");
        public Identifier getId() { return ID; }
        public Codec<Cnd> getConditionsCodec() { return Cnd.CODEC; }
        public void trigger(ServerPlayerEntity player, Identifier id) { trigger(player, c -> c.matches(id)); }
        public record Cnd(Optional<LootContextPredicate> player, List<String> entity) implements Conditions {
            public static final Codec<Cnd> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(Cnd::player),
                Codec.STRING.listOf().fieldOf("entity").forGetter(Cnd::entity)
            ).apply(instance, Cnd::new));
            public boolean matches(Identifier id) { return entity.stream().anyMatch(e -> id.toString().equals(e)); }
        }
    }
}