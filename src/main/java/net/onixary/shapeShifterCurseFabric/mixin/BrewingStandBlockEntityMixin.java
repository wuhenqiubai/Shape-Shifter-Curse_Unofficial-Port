package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.onixary.shapeShifterCurseFabric.recipes.BrewingRecipeUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {

    @Inject(method = "isBrewable", at = @At("HEAD"), cancellable = true)
    private static void ssc$isBrewable(ServerLevel level, BrewingStandBlockEntity entity, CallbackInfoReturnable<Boolean> cir) {
        ItemStack reagent = entity.getItem(3);
        if (reagent.isEmpty()) {
            return;
        }
        for (int dest = 0; dest < 3; dest++) {
            if (BrewingRecipeUtils.findResult(entity.getItem(dest), reagent) != null) {
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Inject(method = "doBrew", at = @At("HEAD"), cancellable = true)
    private static void ssc$doBrew(ServerLevel level, BlockPos pos, BrewingStandBlockEntity entity, CallbackInfo ci) {
        ItemStack reagent = entity.getItem(3);
        if (reagent.isEmpty()) {
            return;
        }
        boolean applied = false;
        for (int dest = 0; dest < 3; dest++) {
            ItemStack out = BrewingRecipeUtils.findResult(entity.getItem(dest), reagent);
            if (out != null) {
                entity.setItem(dest, out);
                applied = true;
            }
        }
        if (!applied) {
            return;
        }
        ItemStackTemplate remainder = reagent.getItem().getCraftingRemainder();
        reagent.shrink(1);
        if (remainder != null) {
            if (reagent.isEmpty()) {
                reagent = remainder.create();
            } else {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), remainder.create());
            }
        }
        entity.setItem(3, reagent);
        level.levelEvent(1035, pos, 0);
        ci.cancel();
    }
}
