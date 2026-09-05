package net.onixary.shapeShifterCurseFabric.mixin.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.additional_power.AdditionalPowers;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * NeoForge/Connector 兼容：替代 mob.CatEntityMixin / mob.OcelotEntityMixin。
 * 原两个 mixin 用 @Mixin(targets = "...$CatAvoidEntityGoal") 匿名内部类 target，
 * NeoForge 重编译会重新编号匿名类，导致注入点失效。这里改为注入外层
 * {@link AvoidEntityGoal#canUse()}（跨 Fabric/NeoForge 稳定）：
 * 当躲避目标是激活了 CAT_FRIENDLY 的玩家时，猫/豹猫的逃跑目标直接返回 false。
 *
 * <p>注意：{@code toAvoid} 是 {@link AvoidEntityGoal} 在 {@code canUse()} 里赋值的
 * {@literal @Shadow} 字段（mojmap 名），不要用 {@literal @Unique} 自建字段（恒为 null）。
 */
@Mixin(AvoidEntityGoal.class)
public abstract class FleeEntityGoalCatFriendlyMixin<T extends LivingEntity> {

    @Shadow
    @Final
    protected PathfinderMob mob;

    @Shadow
    @Nullable
    protected T toAvoid;

    @Inject(method = "canUse", at = @At("RETURN"), cancellable = true)
    private void ssc$modifyCanUse(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && this.toAvoid instanceof Player player) {
            if ((this.mob instanceof Ocelot || this.mob instanceof Cat) && AdditionalPowers.CAT_FRIENDLY.isActive(player)) {
                cir.setReturnValue(false);
            }
        }
    }
}
