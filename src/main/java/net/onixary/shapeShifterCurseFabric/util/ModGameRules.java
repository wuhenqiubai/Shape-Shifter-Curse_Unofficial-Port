package net.onixary.shapeShifterCurseFabric.util;

import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.gamerules.GameRules;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

public class ModGameRules {
    public static final CustomGameRuleCategory SSC_CATEGORY = new CustomGameRuleCategory(ShapeShifterCurseFabric.identifier("gamerule"), Component.translatable("gamerule.category.ssc_gamerule"));

    public static final GameRules.Key<GameRules.BooleanValue> USE_INITIAL_FORM = GameRuleRegistry.register(
            "sscUseInitialForm",
            SSC_CATEGORY,
            GameRuleFactory.createBooleanRule(true)
    );

    public static void register() {
    }
}