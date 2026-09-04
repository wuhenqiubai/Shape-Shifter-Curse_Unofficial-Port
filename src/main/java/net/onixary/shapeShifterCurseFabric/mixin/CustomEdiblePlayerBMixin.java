package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.onixary.shapeShifterCurseFabric.util.CustomEdibleUtils.getPowerFoodComponent;

@Mixin(Player.class)
public abstract class CustomEdiblePlayerBMixin extends LivingEntity {

    protected CustomEdiblePlayerBMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "eat", at = @At(value = "HEAD"), cancellable = true)
    private void eat(Level level, ItemStack itemStack, FoodProperties foodProperties, CallbackInfoReturnable<ItemStack> cir) {
        // ShapeShifterCurseFabric.LOGGER.error("SSC_CE_SYSTEM_CEPB_01");
        if ((Object)this instanceof Player playerEntity) {
            FoodProperties foodComponent = getPowerFoodComponent(playerEntity, itemStack);
            if (foodComponent == null) {
                return;
            }
            playerEntity.getFoodData().eat(foodComponent);
            playerEntity.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
            if (playerEntity instanceof ServerPlayer spe) {
                CriteriaTriggers.CONSUME_ITEM.trigger(spe, itemStack);
            }
            cir.setReturnValue(super.eat(level, itemStack, foodComponent));
        }
    }
}