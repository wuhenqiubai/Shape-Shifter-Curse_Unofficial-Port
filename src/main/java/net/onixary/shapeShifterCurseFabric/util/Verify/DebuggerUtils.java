package net.onixary.shapeShifterCurseFabric.util.Verify;

// 由于我设计上等级3可以用命令修改其他的赞助者功能 所以需要保护

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import org.jetbrains.annotations.Nullable;

public class DebuggerUtils {
    // 只需要维护调试等级1和2的命令 等级3仅开发者可用 炸了就炸了 行为异常不计入Bug

    public static int getDebuggerLevel(@Nullable CommandContext<CommandSourceStack> commandContext, @Nullable Player player) {
        int maxLevel = 0;
        if (commandContext != null && commandContext.getSource().permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(2)))) {
            maxLevel = 1;
        }
        if (ShapeShifterCurseFabric.commonConfig.enableDebugCommand) {
            maxLevel = 2;
        }
        if (player != null) {
            maxLevel = Math.max(maxLevel, DebuggerDataSegment.getLevel(player));
        }
        return maxLevel;
    }

    public static boolean canExecute(@Nullable CommandContext<CommandSourceStack> commandContext, @Nullable Player player, int requireLevel) {
        return getDebuggerLevel(commandContext, player) >= requireLevel;
    }
}
