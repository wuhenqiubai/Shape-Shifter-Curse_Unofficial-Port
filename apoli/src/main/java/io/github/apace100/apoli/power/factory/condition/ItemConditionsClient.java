package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class ItemConditionsClient {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ConditionFactory<>(Apoli.identifier("smeltable"), new SerializableData(),
            (data, stack) -> {
                Level world = Minecraft.getInstance().level;
                if(world == null) {
                    return false;
                }
                var optional = world.getRecipeManager()
                    .getRecipeFor(
                        RecipeType.SMELTING,
                        new SingleRecipeInput(stack),
                        world
                    );
                return optional.isPresent();
            }));
    }

    private static void register(ConditionFactory<ItemStack> conditionFactory) {
        Registry.register(ApoliRegistries.ITEM_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
