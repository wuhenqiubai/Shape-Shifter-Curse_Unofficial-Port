package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.util.function.Consumer;

public class FoodUtilsCondition {
    private static final String VeganDelightTag = "vegandelight:is_vegan";

    public static boolean FC_isVegan(ItemStack itemStack, boolean Default) {
        var customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            return customData.copyTag().getByte(VeganDelightTag).orElse((byte)0) == 1;
        }
        return Default;
    }


    public static void registerCondition(Consumer<ConditionFactory<ItemStack>> register) {
        register.accept(
		        new ConditionFactory<ItemStack>(
				        ShapeShifterCurseFabric.identifier("is_vegan_ex"),
				        new SerializableData()
						        .add("default", SerializableDataTypes.BOOLEAN, false),
                        (data, itemstack) -> FC_isVegan(itemstack, data.getBoolean("default"))
		        )
        );
    }
}
