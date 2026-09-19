package io.github.apace100.apoli.util;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class HudRender {

    public static final HudRender DONT_RENDER = new HudRender(false, 0, Apoli.identifier("textures/gui/resource_bar.png"), null, false);

    private final boolean shouldRender;
    private final int barIndex;
    private final ResourceLocation spriteLocation;
    private final ConditionFactory<LivingEntity>.Instance playerCondition;
    private final boolean inverted;

    public HudRender(boolean shouldRender, int barIndex, ResourceLocation spriteLocation, ConditionFactory<LivingEntity>.Instance condition, boolean inverted) {
        this.shouldRender = shouldRender;
        this.barIndex = barIndex;
        this.spriteLocation = spriteLocation;
        this.playerCondition = condition;
        this.inverted = inverted;
    }

    public ResourceLocation getSpriteLocation() {
        return spriteLocation;
    }

    public int getBarIndex() {
        return barIndex;
    }

    public boolean isInverted() {
        return inverted;
    }

    public boolean shouldRender() {
        return shouldRender;
    }

    public boolean shouldRender(Player player) {
        return shouldRender && (playerCondition == null || playerCondition.test(player));
    }

    public ConditionFactory<LivingEntity>.Instance getCondition() {
        return playerCondition;
    }
}
