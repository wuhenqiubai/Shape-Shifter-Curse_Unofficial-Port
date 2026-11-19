package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.IdentifiedLootTable;
import io.github.apace100.apoli.access.ReplacingLootContext;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ReplaceLootTablePower;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(LootTable.class)
public class LootTableMixin implements IdentifiedLootTable {

    @Unique
    private Identifier apoli$id;
    @Unique
    private HolderGetter.Provider apoli$lootManager;

    @Override
    public void setId(Identifier id, HolderGetter.Provider lootManager) {
        apoli$id = id;
        apoli$lootManager = lootManager;
    }

    @Override
    public Identifier getId() {
        return apoli$id;
    }

    @Inject(method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
    private void modifyLootTable(LootContext context, Consumer<ItemStack> lootConsumer, CallbackInfo ci) {
        if(((ReplacingLootContext)context).isReplaced((LootTable)(Object)this)) {
            return;
        }
        if(context.hasParameter(LootContextParams.THIS_ENTITY)) {
            var type = ((ReplacingLootContext)context).getType();
            Entity entity = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
            if(type == LootContextParamSets.FISHING) {
                if(entity instanceof FishingHook bobber) {
                    entity = bobber.getPlayerOwner();
                }
            } else if(type == LootContextParamSets.ENTITY) {
                if(context.hasParameter(LootContextParams.DIRECT_ATTACKING_ENTITY)) { // TODO: this used to be KILLER_ENTITY
                    entity = context.getOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY);
                }
            } else if(type == LootContextParamSets.PIGLIN_BARTER) {
                if(entity instanceof Piglin piglin) {
                    Optional<Player> optional = piglin.getBrain().getMemoryInternal(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
                    if(optional.isPresent()) {
                        entity = optional.get();
                    }
                }
            }
            List<ReplaceLootTablePower> powers = PowerHolderComponent.getPowers(entity, ReplaceLootTablePower.class);
            powers = powers.stream()
                .filter(p -> p.hasReplacement(apoli$id) && p.doesApply(context))
                .sorted(Comparator.comparing(ReplaceLootTablePower::getPriority))
                .toList();
            if(powers.size() == 0) {
                return;
            }
            ReplaceLootTablePower.addToStack((LootTable)(Object)this);
            LootTable replacement = null;
            for (ReplaceLootTablePower power : powers) {
                Identifier id = power.getReplacement(apoli$id);
                replacement = apoli$lootManager.getOrThrow(ResourceKey.create(Registries.LOOT_TABLE, id)).value();
                ReplaceLootTablePower.addToStack(replacement);
            }
            ((ReplacingLootContext)context).setReplaced((LootTable)(Object)this);
            replacement.getRandomItemsRaw(context, lootConsumer);
            ReplaceLootTablePower.clearStack();
            ci.cancel();
        }
    }

    @Inject(method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootContext;pushVisitedElement(Lnet/minecraft/world/level/storage/loot/LootContext$VisitedEntry;)Z"))
    private void popReplacementStack(LootContext context, Consumer<ItemStack> lootConsumer, CallbackInfo ci) {
        ReplaceLootTablePower.pop();
    }

    @Inject(method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootContext;popVisitedElement(Lnet/minecraft/world/level/storage/loot/LootContext$VisitedEntry;)V"))
    private void restoreReplacementStack(LootContext context, Consumer<ItemStack> lootConsumer, CallbackInfo ci) {
        ReplaceLootTablePower.restore();
    }
}
