package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerModifiedGrindstone;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyGrindstonePower;
import io.github.apace100.apoli.util.ApoliSharedMixinValues;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneScreenHandlerMixin extends AbstractContainerMenu implements PowerModifiedGrindstone {

    @Shadow
    @Final
    private Container repairSlots;

    @Shadow @Final private Container resultSlots;

    @Unique
    private Player apoli$cachedPlayer;

    @Unique
    private Optional<BlockPos> apoli$cachedPosition;

    @Unique
    private List<ModifyGrindstonePower> apoli$appliedPowers;

    protected GrindstoneScreenHandlerMixin(@Nullable MenuType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/inventory/GrindstoneMenu;access:Lnet/minecraft/world/inventory/ContainerLevelAccess;", ordinal = 0))
    private void storeCurrentGrindstoneMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access, CallbackInfo ci) {
        ApoliSharedMixinValues.CURRENT_GRINDSTONE_MENU.set((GrindstoneMenu) (Object) this);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
    private void cachePlayer(int syncId, Inventory playerInventory, ContainerLevelAccess context, CallbackInfo ci) {
        apoli$cachedPlayer = playerInventory.player;
        apoli$cachedPosition = context.evaluate((w, bp) -> bp);
        ApoliSharedMixinValues.CURRENT_GRINDSTONE_MENU.remove();
    }

    @Inject(method = "createResult", at = @At("RETURN"))
    private void modifyResult(CallbackInfo ci) {
        ItemStack top = repairSlots.getItem(0);
        ItemStack bottom = repairSlots.getItem(1);
        ItemStack output = resultSlots.getItem(0);
        List<ModifyGrindstonePower> applyingPowers = PowerHolderComponent.getPowers(apoli$cachedPlayer, ModifyGrindstonePower.class);
        applyingPowers = applyingPowers.stream().filter(mgp -> mgp.doesApply(top, bottom, output, apoli$cachedPosition)).toList();
        ItemStack newOutput = output;
        for(ModifyGrindstonePower mgp : applyingPowers) {
            newOutput = mgp.getOutput(top, bottom, newOutput);
        }
        apoli$appliedPowers = applyingPowers;
        resultSlots.setItem(0, newOutput);
        this.broadcastChanges();
    }

    @Override
    public List<ModifyGrindstonePower> getAppliedPowers() {
        return apoli$appliedPowers;
    }

    @Override
    public Player getPlayer() {
        return apoli$cachedPlayer;
    }

    @Override
    public Optional<BlockPos> getPos() {
        return apoli$cachedPosition;
    }
}
