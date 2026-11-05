package net.onixary.shapeShifterCurseFabric.mixin.accessor;

import net.minecraft.world.entity.ai.ActivityData;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.axolotl.AxolotlAi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(AxolotlAi.class)
public interface AxolotlAiAccessor {
    @Invoker("getActivities")
    static List<ActivityData<Axolotl>> ssc_getActivities() {
        throw new AssertionError();
    }
}
