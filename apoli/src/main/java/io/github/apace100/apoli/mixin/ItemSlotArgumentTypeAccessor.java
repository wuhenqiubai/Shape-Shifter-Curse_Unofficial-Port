package io.github.apace100.apoli.mixin;

import net.minecraft.commands.arguments.SlotArgument;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SlotArgument.class)
public interface ItemSlotArgumentTypeAccessor {

    //@Accessor("SLOTS")
    //static Map<String, Integer> getSlotMappings() {
        //throw new AssertionError();
    //}

}
