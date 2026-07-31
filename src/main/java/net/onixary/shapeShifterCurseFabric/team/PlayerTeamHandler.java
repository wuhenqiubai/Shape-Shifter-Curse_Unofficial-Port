package net.onixary.shapeShifterCurseFabric.team;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.onixary.shapeShifterCurseFabric.additional_power.PillagerFriendlyPower;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;
// 为了避免与Team功能冲突已弃用，替换为其他逻辑
// Deprecated to avoid conflicts with Team functionality, replaced with other logic

public class PlayerTeamHandler {
    private static IForm currentForm;
    public static void updatePlayerTeam(ServerPlayer player) {
        currentForm = FormUtils.getPlayerForm(player);
        updateSorceryTeam(player);
    }


    private static void updateSorceryTeam(ServerPlayer player) {
        if(MobTeamManager.sorceryTeam == null) {
            // 确保队伍已注册
            MobTeamManager.registerTeam(player.level());
        }
        if (PowerHolderComponent.hasPower(player, PillagerFriendlyPower.class)) {
            // 将玩家添加到队伍
            MobTeamManager.sorceryTeam.getPlayers().add(player.getScoreboardName());
        } else {
            // 从队伍中移除（如果是成员）
            PlayerTeam team = player.getTeam();
            if (team != null && team.getName().equals(MobTeamManager.SORCERY_TEAM_NAME)) {
                team.getPlayers().remove(player.getScoreboardName());
            }
        }
    }
}

