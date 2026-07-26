package net.onixary.shapeShifterCurseFabric.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyFoodPower;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static net.onixary.shapeShifterCurseFabric.util.CustomEdibleUtils.getPowerFoodComponent;

@Mixin(Player.class)
public abstract class CustomEdiblePlayerBMixin extends LivingEntity {

    protected CustomEdiblePlayerBMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/food/FoodProperties;)Lnet/minecraft/world/item/ItemStack;", at = @At(value = "HEAD"), cancellable = true)
    private void eatFood(Level world, ItemStack stack, FoodProperties vanillaComponent, CallbackInfoReturnable<ItemStack> cir) {
        if ((Object)this instanceof Player playerEntity) {
            FoodProperties foodComponent = getPowerFoodComponent(playerEntity, stack);
            if (foodComponent == null) {
                return;
            }
            playerEntity.getFoodData().eat(foodComponent);
            playerEntity.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
            if (playerEntity instanceof ServerPlayer spe) {
                CriteriaTriggers.CONSUME_ITEM.trigger(spe, stack);
            }
            cir.setReturnValue(super.eat(world, stack, foodComponent));
        }
    }

    @ModifyVariable(method = "eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/food/FoodProperties;)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), argsOnly = true)
    private FoodProperties ssc$modifyFoodComponent(FoodProperties original, Level world, ItemStack stack) {
        Player player = (Player) (Object) this;

        List<ModifyFoodPower> powers = PowerHolderComponent.getPowers(player, ModifyFoodPower.class)
                .stream()
                .filter(p -> p.doesApply(stack))
                .toList();

        if (powers.isEmpty()) return original;

        int newNutrition = (int) ModifierUtil.applyModifiers(player,
                powers.stream().flatMap(p -> p.getFoodModifiers().stream()).toList(), original.nutrition());

        float origSatMod = original.saturation() / (original.nutrition() * 2.0f);
        float newSatMod = (float) ModifierUtil.applyModifiers(player,
                powers.stream().flatMap(p -> p.getSaturationModifiers().stream()).toList(), origSatMod);
        float newSaturation = FoodConstants.saturationByModifier(newNutrition, newSatMod);

        if (newNutrition == original.nutrition() && newSaturation == original.saturation()) {
            return original;
        }

        return new FoodProperties(
                newNutrition,
                newSaturation,
                original.canAlwaysEat(),
                original.eatSeconds(),
                original.usingConvertsTo(),
                original.effects()
        );
    }

    @Redirect(method = "eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/food/FoodProperties;)Lnet/minecraft/world/item/ItemStack;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/food/FoodProperties;)V"))
    private void ssc$redirectEat(FoodData manager, FoodProperties foodComponent) {
        manager.eat(foodComponent);
        if ((Object)this instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(
                    new ClientboundSetHealthPacket(
                            serverPlayer.getHealth(),
                            manager.getFoodLevel(),
                            manager.getSaturationLevel()));
        }
    }
}