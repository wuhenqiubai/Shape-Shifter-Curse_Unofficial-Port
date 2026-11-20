package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import io.github.apace100.apoli.access.ModifiableFoodEntity;
import io.github.apace100.apoli.power.ModifyFoodPower;
import io.github.apace100.apoli.util.ApoliSharedMixinValues;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public class HungerManagerMixin {

    @Shadow private int foodLevel;
    @Shadow private float saturationLevel;
    @Unique
    private Player player;

    @Unique
    private boolean apoli$ShouldUpdateManually = false;

    @Inject(method = "add", at = @At("HEAD"))
    private void modifyHunger(int foodLevelModifier, float saturationLevelModifier, CallbackInfo ci, @Local(argsOnly = true) LocalIntRef foodLevel, @Local(argsOnly = true) LocalFloatRef saturationLevel) {
        apoli$ShouldUpdateManually = false;

        if (player == null) return;
        var stack = ApoliSharedMixinValues.CURRENT_STACK.get();
        if (stack == null) return;

        var modifiers = ((ModifiableFoodEntity) player).getCurrentModifyFoodPowers()
            .stream()
            .filter(p -> p.doesApply(stack)).toList();

        var foodModifiers = modifiers.stream().flatMap(p -> p.getFoodModifiers().stream()).toList();
        var saturationModifiers = modifiers.stream().flatMap(p -> p.getSaturationModifiers().stream()).toList();

        int newFood = (int) ModifierUtil.applyModifiers(player, foodModifiers, foodLevelModifier);
        if (newFood != foodLevelModifier) apoli$ShouldUpdateManually = true;

        float newSat = (float) ModifierUtil.applyModifiers(player, saturationModifiers, saturationLevelModifier);
        if (newSat != saturationLevelModifier) apoli$ShouldUpdateManually = true;

        foodLevel.set(newFood);
        saturationLevel.set(newSat);
    }

    @Inject(method = "add", at = @At("TAIL"))
    private void executeAdditionalEatAction(int foodLevelModifier, float saturationLevelModifier, CallbackInfo ci) {

        if (player == null || player.level().isClientSide()) return;
        var stack = ApoliSharedMixinValues.CURRENT_STACK.get();
        if (stack == null) return;

        ((ModifiableFoodEntity) player).getCurrentModifyFoodPowers()
            .stream()
            .filter(p -> p.doesApply(stack))
            .forEach(ModifyFoodPower::eat);

        if (apoli$ShouldUpdateManually) ((ServerPlayer) player).connection.send(new ClientboundSetHealthPacket(player.getHealth(), foodLevel, saturationLevel));

    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void cachePlayer(ServerPlayer player, CallbackInfo ci) {
        this.player = player;
    }
}
