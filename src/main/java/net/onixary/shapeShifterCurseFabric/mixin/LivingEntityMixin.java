package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.additional_power.*;
import net.onixary.shapeShifterCurseFabric.cursed_moon.CursedMoon;
import net.onixary.shapeShifterCurseFabric.data.StaticParams;
import net.onixary.shapeShifterCurseFabric.items.RegCustomItem;
import net.onixary.shapeShifterCurseFabric.status_effects.RegOtherStatusEffects;
import net.onixary.shapeShifterCurseFabric.util.ModTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.onixary.shapeShifterCurseFabric.additional_power.WaterFlexibilityPower.MAX_FLEXIBILITY;
import static net.onixary.shapeShifterCurseFabric.util.ModTags.LIKE_SCAFFOLDING_TAG;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {


    @Shadow public abstract float getSpeed();

    @Shadow protected abstract void checkFallDamage(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition);

    @Shadow
    protected abstract void blockUsingShield(LivingEntity attacker);

    @Inject(
            method = "die",
            at = @At(
                    value = "HEAD"
            )
    )
    private void onEntityDeath(DamageSource source, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)(Object)this;
        Level world = entity.level();

        // 仅在服务端执行，避免客户端重复触发
        if (world.isClientSide) return;

        Entity attacker = source.getEntity();
        // 拥有 ENTANGLED_FULL_EFFECT 的生物死亡时在其位置生成蜘蛛网。当攻击者为蜘蛛形态时，概率掉落流食囊
        if (entity.hasEffect(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(RegOtherStatusEffects.ENTANGLED_FULL_EFFECT))) {
            BlockPos pos = entity.blockPosition();
            if (world.getBlockState(pos).isAir()) {
                world.setBlockAndUpdate(pos, Blocks.COBWEB.defaultBlockState());
            }

            if (attacker instanceof ServerPlayer player && entity instanceof Mob mobEntity) {
                handleFluidCocoonLoot(mobEntity, player);
            }
        }

        // 自定义实体的掉落逻辑
        // 攻击者判定来防止干扰生电设施
        if (attacker instanceof ServerPlayer) {
            if(entity instanceof Witch || entity instanceof Evoker) {
                if (Math.random() < StaticParams.FAMILIAR_CURSE_POTION_DROP_PROBABILITY){
                    net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> familiarFoxPotion =
                            net.minecraft.core.registries.BuiltInRegistries.POTION.getHolder(
                                    net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.POTION,
                                            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("shape-shifter-curse", "to_familiar_fox_0_potion")))
                                    .orElseThrow();
                    ItemStack customPotion = PotionContents.createItemStack(Items.SPLASH_POTION, familiarFoxPotion);
                    entity.level().addFreshEntity(
                            new ItemEntity(
                                    entity.level(),
                                    entity.getX(),
                                    entity.getY(),
                                    entity.getZ(),
                                    customPotion
                            )
                    );
                }
            }
        }

        if (attacker instanceof ServerPlayer player && entity instanceof Mob mobEntity) {
            handleExtraLoot(mobEntity, player);
        }

        if(!CursedMoon.isInCursedMoon(entity.level())){
            return;
        }

        if (attacker instanceof TamableAnimal tameableEntity) {
            attacker = tameableEntity.getOwner();
        }

        if (attacker instanceof ServerPlayer player) {
            if (entity instanceof Mob) {
                handleMobDeathDrop((Mob) entity, player);
            }
        }
    }

    // 移动到蜘蛛形态判定
    @Unique
    private void handleExtraLoot(Mob mob, ServerPlayer player) {

    }

    @Unique
    private void handleFluidCocoonLoot(Mob mob, ServerPlayer player) {
        if (AdditionalPowers.CAN_LOOT_SPIDER_FLUID_COCOON.isActive(player) && !mob.getType().builtInRegistryHolder().is(ModTags.SPIDER_FLUID_COCOON_BLACKLIST)) {
            // 40% 掉落 1~(血上限/4f)个
            float mobMaxHp = mob.getMaxHealth();
            int lootCount = (Mth.ceil(mobMaxHp / 4.0f));
            RandomSource random = player.getRandom();
            if (random.nextInt(100) < 40) {
                int finalCount = random.nextInt(lootCount);
                // 钳制最少掉落 1 个
                finalCount = Math.max(finalCount, 1);
                ItemStack stack = new ItemStack(RegCustomItem.SPIDER_FLUID_COCOON, finalCount);
                mob.level().addFreshEntity(
                        new ItemEntity(
                                mob.level(),
                                mob.getX(),
                                mob.getY(),
                                mob.getZ(),
                                stack
                        )
                );
            }
        }
    }

    @Unique
    private void handleMobDeathDrop(Mob mob, ServerPlayer player) {
        // 概率掉落未加工的月之尘
        if (Math.random() < StaticParams.MOONDUST_DROP_PROBABILITY) {
            ItemStack stack = new ItemStack(RegCustomItem.UNTREATED_MOONDUST);
            mob.level().addFreshEntity(
                    new ItemEntity(
                            mob.level(),
                            mob.getX(),
                            mob.getY(),
                            mob.getZ(),
                            stack
                    )
            );
        }
    }

    /**
     * Injects into the fall damage calculation method to modify the fall distance
     * used for damage computation, without affecting the original fall distance value
     * used in form's falling protection powers.
     */
    @ModifyVariable(
            method = "calculateFallDamage(FF)I",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private float modifyFallDistanceForDamageCalc(float fallDistance) {
        LivingEntity self = (LivingEntity) (Object) this;

        List<FallingProtectionPower> powers = PowerHolderComponent.getPowers(self, FallingProtectionPower.class);
        if (powers.isEmpty()) {
            return fallDistance;
        }

        float maxProtection = 0f;
        for (FallingProtectionPower power : powers) {
            if (power.isActive() && power.getFallDistance() > maxProtection) {
                maxProtection = power.getFallDistance();
            }
        }

        return Math.max(0f, fallDistance - maxProtection);
    }

    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"))
    private void onStatusEffectAdded(MobEffectInstance effect, Entity source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof Player player) {
            // 检查是否是溅射药水或滞留药水造成的效果
            if ((source instanceof ThrownPotion || source instanceof AreaEffectCloud)) {
                PowerHolderComponent.getPowers(player, ActionOnSplashPotionTakeEffect.class)
                        .forEach(ActionOnSplashPotionTakeEffect::executeAction);
            }
        }
    }

    // Origins的LikeWaterPower
    @ModifyArg(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V", ordinal = 1))
    public Vec3 likeWaterMixin(Vec3 movementInput, @Local(ordinal = 0) double d) {
        LivingEntity self = (LivingEntity) (Object) this;
        if(AdditionalPowers.LIKE_WATER.isActive((LivingEntity) (Object) this)) {
            if (Math.abs(self.getDeltaMovement().y - d / 16.0D) < 0.025D) {
                return new Vec3(movementInput.x, 0, movementInput.z);
            }
        }
        return movementInput;
    }

    // todo: 直接强制修改hasModifyWaterSpeed似乎会导致广泛的与其他模组的mixin冲突，暂时禁用
    /*
    @Unique
    private boolean hasModifyWaterSpeed;

    @Inject(method = "travel", at = @At("HEAD"))
    private void onTravel(Vec3d movementInput, CallbackInfo ci) {
        this.hasModifyWaterSpeed = false;
    }

    @ModifyVariable(method = "travel", at = @At("STORE"), name = "g")
    private float modifyInWaterSpeed(float g) {
        if (this.hasModifyWaterSpeed) { return g; }
        this.hasModifyWaterSpeed = true;
        return this.getMovementSpeed() * 0.2f;  // g * (this.getMovementSpeed() / 0.1f) 或者 0.10000000149011612f g = 0.02f
    }*/

    @ModifyArg(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V"), index = 0)
    private float ModifyInWaterSpeed(float g) {
        if ((LivingEntity)(Object)this instanceof Player player) {
            // g -> 水中速度
            // g 范围 0.02 -> PlayerSpeed
            // 目标 PlayerSpeed * 0.2 -> PlayerSpeed
            // 会让所有其他修改水中速度失效
            // float PlayerSpeed = player.getMovementSpeed();
            // float newG = PlayerSpeed * 0.2f;
            // float h = (float) EnchantmentHelper.getDepthStrider(player);
            // if (h > 3.0F) {
            //     h = 3.0f;
            // }
            // if (!player.isOnGround()) {
            //     h *= 0.5F;
            // }
            // if (h > 0.0F) {
            //     newG += (this.getMovementSpeed() - newG) * h / 3.0F;
            // }
            // return newG;
            List<InWaterSpeedModifierPower> powers = PowerHolderComponent.getPowers(player, InWaterSpeedModifierPower.class);
            float totalSpeedModifier = powers
                    .stream()
                    .map(InWaterSpeedModifierPower::getSpeedModifier)
                    .reduce(1.0f, (a, b) -> a * b);
            return g * totalSpeedModifier;
        }
        return g;
    }

    @ModifyArgs(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;", ordinal = 0))
    private void modifyInWaterFlexibility(Args args) {
        if ((LivingEntity)(Object)this instanceof Player player) {
            double targetSpeedX = args.get(0);
            double targetSpeedZ = args.get(2);
            if (!player.isInWater()) {
                return;
            }
            PowerHolderComponent component = PowerHolderComponent.KEY.get(player);

            for (WaterFlexibilityPower power : component.getPowers(WaterFlexibilityPower.class)) {
                if (power.isActive()) {
                    float resistance = power.getResistance();
                    targetSpeedX = 0.8F + (MAX_FLEXIBILITY - 0.8F) * resistance;
                    targetSpeedZ = 0.8F + (MAX_FLEXIBILITY - 0.8F) * resistance;
                }
            }
            args.set(0, targetSpeedX);
            args.set(2, targetSpeedZ);
        }
    }


    @Inject(method = "isSuppressingSlidingDownLadder", at = @At("HEAD"), cancellable = true)
    private void isHoldingOntoLadder(CallbackInfoReturnable<Boolean> cir) {
        if (((LivingEntity) (Object) this).getInBlockState().is(LIKE_SCAFFOLDING_TAG)) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private <T extends Power> float applyModifier(Class<T> powerClass, float baseValue, Function<T, List<Modifier>> powerModifierGetter) {
        LivingEntity entity = (LivingEntity) (Object) this;
        List<T> powers = PowerHolderComponent.getPowers(entity, powerClass);
        List<Modifier> mps = powers.stream()
                .flatMap(p -> powerModifierGetter.apply(p).stream()).collect(Collectors.toList());
        return (float) ModifierUtil.applyModifiers(entity, mps, baseValue);
    }

    @ModifyVariable(method = "causeFallDamage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float handleFallDamageA(float fallDistance) {
        float finalV = applyModifier(ModifyFallDamagePower.class, fallDistance, ModifyFallDamagePower::getModifiers_FallDistance);
        return Math.max(0f, finalV);
    }

    @ModifyVariable(method = "causeFallDamage", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private float handleFallDamageB(float damageMultiplier) {
        float finalV = applyModifier(ModifyFallDamagePower.class, damageMultiplier, ModifyFallDamagePower::getModifiers_DamageMultiplier);
        return Math.max(0f, finalV);
    }

    // 旧方案 使用模拟原版盾牌方案 可以避免任何情况下的盾牌损坏问题
    // @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true, order = 9999)
    // private float modifyDamageTaken(float originalValue, DamageSource source, float amount) {
    //     LivingEntity realThis = (LivingEntity) (Object) this;
    //     float finalDamage = originalValue;
    //     for (VirtualShieldPower power : PowerHolderComponent.getPowers(realThis, VirtualShieldPower.class)) {
    //         if (power.blockDamage(source)) {
    //             finalDamage = 0.0f;
    //             Entity attacker = source.getAttacker();
    //             if (!source.isIn(DamageTypeTags.IS_PROJECTILE) && (attacker instanceof LivingEntity ale)) {
    //                 this.takeShieldHit(ale);
    //             }
    //             realThis.getWorld().sendEntityStatus(realThis, (byte)29);
    //         }
    //     }
    //     return finalDamage;
    // }

    // 新方案 修改原版盾牌检查函数 对于伤害防护兼容性强 但是使用了Redirect(需要防止玩家当前使用的盾牌不会受损) 可能兼容性不太高
    @Unique
    private boolean bypassNextShieldDamage = false;

    @Inject(method = "isDamageSourceBlocked", at = @At("HEAD"), cancellable = true)
    private void blockedByShield(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity realThis = (LivingEntity) (Object) this;
        for (VirtualShieldPower power : PowerHolderComponent.getPowers(realThis, VirtualShieldPower.class)) {
            if (power.blockDamage(source)) {
                this.bypassNextShieldDamage = true;
                cir.setReturnValue(true);
            }
        }
    }

    @WrapOperation(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurtCurrentlyUsedShield(F)V"))
    private void damageShield(LivingEntity instance, float amount, Operation<Void> original) {
        if (!this.bypassNextShieldDamage) {
            original.call(instance, amount);
        }
        this.bypassNextShieldDamage = false;
    }
}