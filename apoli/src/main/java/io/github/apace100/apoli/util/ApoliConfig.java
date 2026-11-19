package io.github.apace100.apoli.util;

import io.github.apace100.apoli.Apoli;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.permissions.Permissions;

public class ApoliConfig implements ConfigData {

    @ConfigEntry.Gui.CollapsibleObject
    public ExecuteCommand executeCommand = new ExecuteCommand();

    public static class ExecuteCommand {
        @ConfigEntry.BoundedDiscrete(min = 0, max = 4)
        public int permissionLevel = 2;
        public boolean showOutput = false;

        public PermissionSet getPermissionHandler() {
            return (permission) -> switch (this.permissionLevel) { // TODO: make this proper
                case 0 -> LevelBasedPermissionSet.NO_PERMISSIONS.hasPermission(permission);
                case 1 -> LevelBasedPermissionSet.MODERATOR.hasPermission(permission);
                case 2 -> LevelBasedPermissionSet.GAMEMASTER.hasPermission(permission);
                case 3 -> LevelBasedPermissionSet.ADMIN.hasPermission(permission);
                case 4 -> LevelBasedPermissionSet.OWNER.hasPermission(permission);
                default -> false;
            };
        }
    }
}
