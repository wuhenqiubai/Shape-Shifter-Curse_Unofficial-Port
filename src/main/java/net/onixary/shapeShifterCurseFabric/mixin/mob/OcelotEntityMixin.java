package net.onixary.shapeShifterCurseFabric.mixin.mob;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.additional_power.AdditionalPowers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

// 1.21.11: Ocelot 移到 net.minecraft.world.entity.animal.feline 包
@Mixin(targets = "net.minecraft.world.entity.animal.feline.Ocelot$OcelotAvoidEntityGoal")
public class OcelotEntityMixin {
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/AvoidEntityGoal;<init>(Lnet/minecraft/world/entity/PathfinderMob;Ljava/lang/Class;FDDLjava/util/function/Predicate;)V"), index = 5)
    private static Predicate<LivingEntity> modifyCatFleeGoalPredicate(Predicate<LivingEntity> predicate) {
        return predicate.and(
                livingEntity -> {
                    if (livingEntity instanceof Player player) {
                        return !AdditionalPowers.CAT_FRIENDLY.isActive(player);
                    }
                    return true;
                }
        );
    }
}
