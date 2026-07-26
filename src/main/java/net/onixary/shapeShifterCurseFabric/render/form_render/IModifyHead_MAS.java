package net.onixary.shapeShifterCurseFabric.render.form_render;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.player.Player;

public interface IModifyHead_MAS {
    void modifyHeadPart(Player player, HumanoidModel<?> model, FormModel formModel);
}
