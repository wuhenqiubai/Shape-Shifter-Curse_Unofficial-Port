package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.util.ApoliLivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.equipment.Equippable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateInject implements ApoliLivingEntityRenderState {
    @Unique private PowerHolderComponent apoli$powerHolder;
    @Unique private Equippable apoli$cachedEquippable;

    @Override
    public void apoli$setPowerHolder(PowerHolderComponent component) {
        this.apoli$powerHolder = component;
    }

    @Override
    public PowerHolderComponent apoli$getPowerHolder() {
        return apoli$powerHolder;
    }

    @Override
    public void apoli$setCachedEquippable(Equippable equippable) {
        this.apoli$cachedEquippable = equippable;
    }

    @Override
    public Equippable apoli$getCachedEquippable() {
        return this.apoli$cachedEquippable;
    }
}
