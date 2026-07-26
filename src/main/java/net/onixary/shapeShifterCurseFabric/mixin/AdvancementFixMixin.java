package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementManager;
import net.onixary.shapeShifterCurseFabric.util.AdvancementUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AdvancementManager.class)
public class AdvancementFixMixin {
    @Unique
    private void onAdvancementAdded(AdvancementEntry advancement) {
        AdvancementUtils.onAdvancementAdded(advancement);
    }
}
