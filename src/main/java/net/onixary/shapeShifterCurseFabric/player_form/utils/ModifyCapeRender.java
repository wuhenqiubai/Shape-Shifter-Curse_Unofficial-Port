package net.onixary.shapeShifterCurseFabric.player_form.utils;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.phys.Vec3;

public interface ModifyCapeRender {
    public Vec3 getCapeIdleLoc(AbstractClientPlayer player);

    public float getCapeBaseRotateAngle(AbstractClientPlayer player);

    public boolean NeedModifyXRotationAngle();
}
