package io.github.apace100.apoli.power;

import com.mojang.datafixers.util.Pair;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.util.LazyItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ModifyCraftingPower extends ValueModifyingPower {

    private final Identifier recipeIdentifier;
    private final Predicate<ItemStack> itemCondition;

    private final LazyItemStack newStack;
    private final Consumer<Pair<Level, ItemStack>> itemAction;
    private final Consumer<Pair<Level, ItemStack>> lateItemAction;
    private final Consumer<Entity> entityAction;
    private final Consumer<Triple<Level, BlockPos, Direction>> blockAction;

    public ModifyCraftingPower(PowerType<?> type, LivingEntity entity, Identifier recipeIdentifier, Predicate<ItemStack> itemCondition, LazyItemStack newStack, Consumer<Pair<Level, ItemStack>> itemAction, Consumer<Pair<Level, ItemStack>> lateItemAction, Consumer<Entity> entityAction, Consumer<Triple<Level, BlockPos, Direction>> blockAction) {
        super(type, entity);
        this.recipeIdentifier = recipeIdentifier;
        this.itemCondition = itemCondition;
        this.newStack = newStack;
        this.itemAction = itemAction;
        this.lateItemAction = lateItemAction;
        this.entityAction = entityAction;
        this.blockAction = blockAction;
    }

    public boolean doesApply(CraftingInput inventory, RecipeHolder<CraftingRecipe> recipe) {
        if(recipeIdentifier != null) {
            if(!recipe.id().identifier().equals(recipeIdentifier)) {
                return false;
            }
        }
        if(itemCondition != null) {
            if(!itemCondition.test(recipe.value().assemble(inventory))) {
                return false;
            }
        }
        return true;
    }

    public void applyAfterCraftingItemAction(ItemStack output) {
        if(lateItemAction == null) {
            return;
        }
        lateItemAction.accept(new Pair<>(entity.level(), output));
    }

    public ItemStack getNewResult(CraftingInput input, CraftingRecipe recipe) {
        ItemStack stack;
        if(newStack != null) {
            stack = newStack.getStack().copy();
        } else {
            stack = recipe.assemble(input);
        }
        if(itemAction != null) {
            itemAction.accept(new Pair<>(entity.level(), stack));
        }
        return stack;
    }

    public void executeActions(Optional<BlockPos> craftingBlockPos) {
        if(craftingBlockPos.isPresent() && blockAction != null) {
            blockAction.accept(Triple.of(entity.level(), craftingBlockPos.get(), Direction.UP));
        }
        if(entityAction != null) {
            entityAction.accept(entity);
        }
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_crafting"),
            new SerializableData()
                .add("recipe", SerializableDataTypes.IDENTIFIER, null)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("result", SerializableDataTypes.ITEM_STACK, null)
                .add("item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("item_action_after_crafting", ApoliDataTypes.ITEM_ACTION, null)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("block_action", ApoliDataTypes.BLOCK_ACTION, null),
            data ->
                (type, player) -> new ModifyCraftingPower(type, player,
                    data.getId("recipe"), data.get("item_condition"),
                    data.get("result"), data.get("item_action"),
                    data.get("item_action_after_crafting"), data.get("entity_action"), data.get("block_action")))
            .allowCondition();
    }
}
