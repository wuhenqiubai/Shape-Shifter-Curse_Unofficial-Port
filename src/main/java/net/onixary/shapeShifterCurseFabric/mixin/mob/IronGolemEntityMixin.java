package net.onixary.shapeShifterCurseFabric.mixin.mob;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.additional_power.AdditionalPowers;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(IronGolem.class)
public class IronGolemEntityMixin extends AbstractGolem implements NeutralMob {
    protected IronGolemEntityMixin(EntityType<? extends AbstractGolem> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(at = @At("TAIL"), method = "registerGoals")
    private void addGoals(CallbackInfo info) {
        Goal goal = new NearestAttackableTargetGoal<Player>(this, Player.class, 10, true, false, AdditionalPowers.HOSTILE_IRON_GOLEM::isActive);;
	    this.targetSelector.addGoal(3, goal);
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return 0;
    }

    @Override
    public void setRemainingPersistentAngerTime(int angerTime) {

    }

    @Override
    public @Nullable UUID getPersistentAngerTarget() {
        return null;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID angryAt) {

    }

    @Override
    public void startPersistentAngerTimer() {

    }
}
