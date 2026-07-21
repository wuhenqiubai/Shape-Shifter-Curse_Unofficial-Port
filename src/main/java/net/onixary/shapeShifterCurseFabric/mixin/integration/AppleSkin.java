package net.onixary.shapeShifterCurseFabric.mixin.integration;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyFoodPower;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.util.CustomEdibleUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import squeek.appleskin.helpers.FoodHelper;

import java.util.List;

@Mixin(FoodHelper.class)
// 排除逻辑在net.onixary.shapeShifterCurseFabric.mixin.plugin.MixinConfigPlugin里临时注册
public class AppleSkin {
    static {
        // 写一个理应不会抛异常的静态代码块，防止之后修改MixinConfigPlugin出错导致出现其他问题
        if (!FabricLoader.getInstance().isModLoaded("appleskin")) {
            throw new IllegalStateException("AppleSkin mixin was loaded but appleskin is not installed!");
        }
    }

    @Inject(method = "canConsume", at = @At("HEAD"), cancellable = true)
    private static void shapeShifterCurseFabric$canConsume(PlayerEntity player, FoodComponent foodComponent, CallbackInfoReturnable<Boolean> cir) {
        if (player != null && foodComponent != null) {
            boolean CanConsume = player.canConsume(foodComponent.canAlwaysEat());
            if (CanConsume) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "getDefaultFoodValues", at = @At("HEAD"), cancellable = true)
    private static void shapeShifterCurseFabric$getDefaultFoodValues(ItemStack itemStack, CallbackInfoReturnable<FoodComponent> cir) {
        if (itemStack == null) return;

        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        // Get base food values from SSC custom edible power or item defaults
        FoodComponent itemFood = CustomEdibleUtils.getPowerFoodComponent(player, itemStack);
        itemFood = itemFood != null ? itemFood : itemStack.get(DataComponentTypes.FOOD);
        if (itemFood == null) return;

        // Apply Apoli ModifyFoodPower modifiers (restored from upstream)
        List<ModifyFoodPower> mfps = PowerHolderComponent.getPowers(player, ModifyFoodPower.class);

        List<Modifier> foodModifiers = mfps.stream()
                .filter(p -> p.doesApply(itemStack))
                .flatMap(p -> p.getFoodModifiers().stream())
                .toList();
        int hunger = foodModifiers.isEmpty()
                ? itemFood.nutrition()
                : (int) ModifierUtil.applyModifiers(player, foodModifiers, itemFood.nutrition());

        List<Modifier> saturationModifiers = mfps.stream()
                .filter(p -> p.doesApply(itemStack))
                .flatMap(p -> p.getSaturationModifiers().stream())
                .toList();
        float saturation = saturationModifiers.isEmpty()
                ? itemFood.saturation()
                : (float) ModifierUtil.applyModifiers(player, saturationModifiers, itemFood.saturation());

        if (hunger != itemFood.nutrition() || saturation != itemFood.saturation()) {
            cir.setReturnValue(new FoodComponent(hunger, saturation, itemFood.canAlwaysEat(),
                    itemFood.eatSeconds(), itemFood.usingConvertsTo(), itemFood.effects()));
        }
    }
}
