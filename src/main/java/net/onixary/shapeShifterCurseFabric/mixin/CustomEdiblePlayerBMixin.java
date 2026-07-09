package net.onixary.shapeShifterCurseFabric.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyFoodPower;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static net.onixary.shapeShifterCurseFabric.util.CustomEdibleUtils.getPowerFoodComponent;

@Mixin(PlayerEntity.class)
public abstract class CustomEdiblePlayerBMixin extends LivingEntity {

    protected CustomEdiblePlayerBMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "eatFood", at = @At(value = "HEAD"), cancellable = true)
    private void eatFood(World world, ItemStack stack, FoodComponent vanillaComponent, CallbackInfoReturnable<ItemStack> cir) {
        if ((Object)this instanceof PlayerEntity playerEntity) {
            FoodComponent foodComponent = getPowerFoodComponent(playerEntity, stack);
            if (foodComponent == null) {
                return;
            }
            playerEntity.getHungerManager().eat(foodComponent);
            playerEntity.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
            world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
            if (playerEntity instanceof ServerPlayerEntity spe) {
                Criteria.CONSUME_ITEM.trigger(spe, stack);
            }
            cir.setReturnValue(super.eatFood(world, stack, foodComponent));
        }
    }

    @ModifyVariable(method = "eatFood", at = @At("HEAD"), argsOnly = true)
    private FoodComponent ssc$modifyFoodComponent(FoodComponent original, World world, ItemStack stack) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        List<ModifyFoodPower> powers = PowerHolderComponent.getPowers(player, ModifyFoodPower.class)
                .stream()
                .filter(p -> p.doesApply(stack))
                .toList();

        if (powers.isEmpty()) return original;

        List<Modifier> nutritionModifiers = powers.stream()
                .flatMap(p -> p.getFoodModifiers().stream())
                .toList();
        List<Modifier> saturationModifiers = powers.stream()
                .flatMap(p -> p.getSaturationModifiers().stream())
                .toList();

        int newNutrition = (int) ModifierUtil.applyModifiers(player, nutritionModifiers, original.nutrition());
        float newSaturation = (float) ModifierUtil.applyModifiers(player, saturationModifiers, original.saturation());

        if (newNutrition == original.nutrition() && newSaturation == original.saturation()) {
            return original;
        }

        return new FoodComponent(
                newNutrition,
                newSaturation,
                original.canAlwaysEat(),
                original.eatSeconds(),
                original.usingConvertsTo(),
                original.effects()
        );
    }
}
