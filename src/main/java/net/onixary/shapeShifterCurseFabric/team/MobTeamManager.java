package net.onixary.shapeShifterCurseFabric.team;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

// 为了避免与Team功能冲突已弃用，替换为其他逻辑
// Deprecated to avoid conflicts with Team functionality, replaced with other logic
public class MobTeamManager {
    public static final String SORCERY_TEAM_NAME = "sorcery_team";
    public static PlayerTeam sorceryTeam;

    public static void registerTeam(ServerLevel world) {
        Scoreboard scoreboard = world.getScoreboard();

        // 创建队伍（如果不存在）
        if (scoreboard.getPlayerTeam(SORCERY_TEAM_NAME) == null) {
            sorceryTeam = scoreboard.addPlayerTeam(SORCERY_TEAM_NAME);

            // 配置队伍属性
            sorceryTeam.setAllowFriendlyFire(false);          // 队友之间不会误伤
            sorceryTeam.setSeeFriendlyInvisibles(true);       // 可以看到隐形的队友
        } else {
            sorceryTeam = scoreboard.getPlayerTeam(SORCERY_TEAM_NAME);
        }
    }
}

