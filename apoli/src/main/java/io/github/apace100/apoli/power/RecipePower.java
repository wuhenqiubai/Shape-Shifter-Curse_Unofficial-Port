package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;

public class RecipePower extends Power {

    private final Recipe<CraftingInput> recipe;

    public RecipePower(PowerType<?> type, LivingEntity entity, Recipe<CraftingInput> recipe) {
        super(type, entity);
        this.recipe = recipe;
    }

    public Recipe<CraftingInput> getRecipe() {
        return recipe;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("recipe"),
            new SerializableData()
                .add("recipe", SerializableDataTypes.RECIPE),
            data ->
                (type, player) -> {
                    Recipe<CraftingInput> recipe = data.get("recipe");
                    return new RecipePower(type, player, recipe);
                })
            .allowCondition();
    }
}
