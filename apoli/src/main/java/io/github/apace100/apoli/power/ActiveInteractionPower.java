package io.github.apace100.apoli.power;

import com.mojang.datafixers.util.Pair;
import io.github.apace100.calio.util.LazyItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActiveInteractionPower extends InteractionPower implements Prioritized<ActiveInteractionPower> {

    private final int priority;

    public ActiveInteractionPower(PowerType<?> type, LivingEntity entity, EnumSet<InteractionHand> hands, InteractionResult actionResult, Predicate<ItemStack> itemCondition, Consumer<Pair<Level, ItemStack>> heldItemAction, LazyItemStack itemResult, Consumer<Pair<Level, ItemStack>> resultItemAction, int priority) {
        super(type, entity, hands, actionResult, itemCondition, heldItemAction, itemResult, resultItemAction);
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
