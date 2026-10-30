package net.onixary.shapeShifterCurseFabric.util;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

public class ModGameRules {
    public static final GameRule<Boolean> USE_INITIAL_FORM = GameRuleBuilder.forBoolean(true)
            // 26.1: Fabric 的 CustomGameRuleCategory(Identifier, Component) 被删除，
            // 改用 MC 原版 GameRuleCategory —— record(Identifier)，展示名由 lang key
            // `gamerule.category.<namespace>.<path>` 提供（见 GameRuleCategory#label()），
            // 所以原字面量 "Shape Shifter Curse" 需在语言文件里补对应条目。
            .category(GameRuleCategory.register(ShapeShifterCurseFabric.identifier("ssc")))
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
