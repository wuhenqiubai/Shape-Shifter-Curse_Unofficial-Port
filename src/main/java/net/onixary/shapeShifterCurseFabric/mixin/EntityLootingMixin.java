package net.onixary.shapeShifterCurseFabric.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.additional_power.ModifyEntityLootPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Mixin(LivingEntity.class)
public abstract class EntityLootingMixin {
    @Unique
    private ItemEntity DropLootStack(ItemStack stack) {
        LivingEntity RealThis = (LivingEntity)(Object)this;
        LivingEntity Attacker = RealThis.getLastHurtByMob();
        if (Attacker instanceof Player player) {
            AtomicReference<ItemStack> FinalStack = new AtomicReference<>(stack);
            PowerHolderComponent.getPowers(player, ModifyEntityLootPower.class).forEach(
                    power -> FinalStack.set(power.ApplyModifyDrop(FinalStack.get(), RealThis.getRandom()))
            );
            if (RealThis.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                return RealThis.spawnAtLocation(serverLevel, FinalStack.get());
            }
        }
        if (RealThis.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            return RealThis.spawnAtLocation(serverLevel, stack);
        }
        return null;
    }

    @ModifyArg(method = "dropFromLootTable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;JLjava/util/function/Consumer;)V"), index = 2)
    private Consumer<ItemStack> modifyLootTableArgs(Consumer<ItemStack> lootConsumer) {
        return this::DropLootStack;
    }
}
