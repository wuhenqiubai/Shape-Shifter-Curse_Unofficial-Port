package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementTree;
import net.onixary.shapeShifterCurseFabric.util.AdvancementUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AdvancementTree.class)
public class AdvancementFixMixin {
    @Unique
    private void onAdvancementAdded(AdvancementHolder advancement) {
        AdvancementUtils.onAdvancementAdded(advancement);
    }
}
