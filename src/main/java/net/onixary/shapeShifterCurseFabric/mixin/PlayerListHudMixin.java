package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.data.CodexData;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;

@Mixin(PlayerTabOverlay.class)
public class PlayerListHudMixin {
    @ModifyReturnValue(method = "getNameForDisplay", at = @At("RETURN"))
    private Component getNameForDisplay(Component original, @Local(argsOnly = true) PlayerInfo entry) {
        // 1.21.11: GameProfile 的访问器改为 record 风格 id()（原 getId()）
        UUID uuid = entry.getProfile().id();
        Minecraft client = Minecraft.getInstance();
        LocalPlayer nowPlayer = client.player;
        Player playerEntity = null;
        if (client.level != null) {
            playerEntity = client.level.getPlayerByUUID(uuid);
        }
        if (nowPlayer == null || playerEntity == null) {
            return original;
        }
        // 1.21.11: ClientInput.shiftKeyDown 已改为 Input keyPresses 上的 shift()
        if (nowPlayer.input.keyPresses.shift()) {
            IForm form = FormUtils.getPlayerForm(playerEntity);
            if (RegPlayerForms.ORIGINAL_BEFORE_ENABLE.isEquals(form)) {
                return original;
            }
            Component formName = form.getContentText(CodexData.ContentType.NAME);
            return Component.nullToEmpty(original.getString() + " | " + formName.getString());
        }
        return original;
    }
}
