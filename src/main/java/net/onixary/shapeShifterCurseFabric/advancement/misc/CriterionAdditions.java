package net.onixary.shapeShifterCurseFabric.advancement.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public final class CriterionAdditions {

    public static OnTransformForm createOnTransformForm() {
        return new OnTransformForm();
    }

    public static OnWebEntity createOnWebEntity() {
        return new OnWebEntity();
    }

    public static final class OnTransformForm extends SimpleCriterionTrigger<OnTransformForm.Cnd> {
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("shape-shifter-curse", "on_transform_form");
        public ResourceLocation getId() { return ID; }
        public Codec<Cnd> codec() { return Cnd.CODEC; }
        public void trigger(ServerPlayer player) { trigger(player, Cnd::matchesAny); }
        public void trigger(ServerPlayer player, ResourceLocation formID) { trigger(player, c -> c.matches(formID)); }
        public record Cnd(Optional<ContextAwarePredicate> player, List<String> form, Optional<List<Integer>> formTier, Optional<List<String>> flags, Optional<List<String>> notFlags) implements SimpleCriterionTrigger.SimpleInstance {
            public static final Codec<Cnd> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Cnd::player),
                Codec.STRING.listOf().optionalFieldOf("form", List.of()).forGetter(Cnd::form),
                Codec.INT.listOf().optionalFieldOf("form_tier").forGetter(Cnd::formTier),
                Codec.STRING.listOf().optionalFieldOf("flags").forGetter(Cnd::flags),
                Codec.STRING.listOf().optionalFieldOf("not_flags").forGetter(Cnd::notFlags)
            ).apply(instance, Cnd::new));
            public boolean matchesAny() { return true; }
            public boolean matches(ResourceLocation id) {
                if (form.isEmpty() && formTier.isEmpty() && flags.isEmpty() && notFlags.isEmpty())
                    return true;
                if (!form.isEmpty() && form.stream().anyMatch(f -> id.toString().equals(f)))
                    return true;
                return false;
            }
        }
    }

    public static final class OnWebEntity extends SimpleCriterionTrigger<OnWebEntity.Cnd> {
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("shape-shifter-curse", "on_web_entity");
        public ResourceLocation getId() { return ID; }
        public @NotNull Codec<Cnd> codec() { return Cnd.CODEC; }
        public void trigger(ServerPlayer player, ResourceLocation id) { trigger(player, c -> c.matches(id)); }
        public record Cnd(Optional<ContextAwarePredicate> player, List<String> entity) implements SimpleInstance {
            public static final Codec<Cnd> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Cnd::player),
                Codec.STRING.listOf().fieldOf("entity").forGetter(Cnd::entity)
            ).apply(instance, Cnd::new));
            public boolean matches(ResourceLocation id) { return entity.stream().anyMatch(e -> id.toString().equals(e)); }
        }
    }
}