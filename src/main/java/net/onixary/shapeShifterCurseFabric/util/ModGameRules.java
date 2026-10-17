package net.onixary.shapeShifterCurseFabric.util;

import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRule;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

public class ModGameRules {
    public static final GameRule<Boolean> USE_INITIAL_FORM = GameRuleBuilder.forBoolean(true)
            .category(new CustomGameRuleCategory(
                    ShapeShifterCurseFabric.identifier("ssc"),
                    Component.literal("Shape Shifter Curse")
            ))
            .buildAndRegister(ShapeShifterCurseFabric.identifier("use_initial_form"));

    public static void register() {
    }

    public static boolean isUseInitialForm(MinecraftServer server) {
        if (server == null) {
            return true;
        }
        return server.overworld().getGameRules().get(USE_INITIAL_FORM);
    }
}
