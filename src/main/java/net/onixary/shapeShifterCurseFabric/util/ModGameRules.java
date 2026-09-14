package net.onixary.shapeShifterCurseFabric.util;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

public class ModGameRules {
    // 1.21.11 gamerule 系统重构为 registry + GameRule 构造器（fabric-game-rule-api 已随聚合 fabric-api 提供）。
    // 恢复：初始形态是否启用改为可运行时开关的 gamerule，替代硬编码常量。
    public static final GameRule<Boolean> USE_INITIAL_FORM = GameRuleBuilder.forBoolean(true)
            // 26.1: Fabric 的 CustomGameRuleCategory(Identifier, Component) 被删除，
            // 改用 MC 原版 GameRuleCategory —— record(Identifier)，展示名由 lang key
            // `gamerule.category.<namespace>.<path>` 提供（见 GameRuleCategory#label()），
            // 所以原字面量 "Shape Shifter Curse" 需在语言文件里补对应条目。
            .category(GameRuleCategory.register(ShapeShifterCurseFabric.identifier("ssc")))
            .buildAndRegister(ShapeShifterCurseFabric.identifier("use_initial_form"));

    public static void register() {
        // static 初始化时已 buildAndRegister 注册到 BuiltInRegistries.GAME_RULE
    }

    /** 读取 gamerule（服务端有效）；无 server 上下文（客户端）时回退为 true（与默认值一致）。 */
    public static boolean isUseInitialForm(MinecraftServer server) {
        if (server == null) {
            return true;
        }
        return server.overworld().getGameRules().get(USE_INITIAL_FORM);
    }
}
