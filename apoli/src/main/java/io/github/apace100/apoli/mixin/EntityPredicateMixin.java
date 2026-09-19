package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.SetEntityGroupPower;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.EntityTypePredicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(EntityPredicate.class)
public abstract class EntityPredicateMixin {
    // SetEntityGroupPower
    @WrapOperation(method = "matches(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/Entity;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/criterion/EntityTypePredicate;matches(Lnet/minecraft/world/entity/EntityType;)Z"))
    private boolean checkMatchesEntityGroup(EntityTypePredicate instance, EntityType<?> entityType, Operation<Boolean> original, @Local(argsOnly = true) Entity entity) {
        var value = original.call(instance, entityType);
        var entityTypeTag = instance.types().unwrapKey().orElse(null);

        if (entityTypeTag == null) {
            return value;
        }

        if(entity instanceof LivingEntity livingEntity) {
            PowerHolderComponent component = PowerHolderComponent.KEY.get(livingEntity);
            List<SetEntityGroupPower> groups = component.getPowers(SetEntityGroupPower.class);
            /*if(groups.size() > 1) { // TODO O-L: is this needed anymore?
                    Apoli.LOGGER.warn("Entity {} has two instances of SetEntityGroupPower.", entity.getDisplayName());
                }*/

            for (SetEntityGroupPower group : groups) {
                for (TagKey<EntityType<?>> groupTag : group.groupTags) {
                    // TODO O-L: We should really have a way to properly match via root tags instead of having to individually define
                    //           each tag.
                    if (entityTypeTag.equals(groupTag)) {
                        return true;
                    }
                }
            }
        }

        return value;
    }
}
