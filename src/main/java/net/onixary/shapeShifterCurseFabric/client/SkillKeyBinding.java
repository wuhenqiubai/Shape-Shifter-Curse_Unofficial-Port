package net.onixary.shapeShifterCurseFabric.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.onixary.shapeShifterCurseFabric.magic.IMagic;
import net.onixary.shapeShifterCurseFabric.magic.MagicUtils;
import net.onixary.shapeShifterCurseFabric.magic.PressKeyMagic;

public class SkillKeyBinding extends KeyMapping {
    public static final Minecraft client = Minecraft.getInstance();

    private final String KeyID;

    public SkillKeyBinding(String translationKey, InputConstants.Type type, int code, KeyMapping.Category category) {
        super(translationKey, type, code, category);
        this.KeyID = translationKey;
    }

    @Override
    public boolean isDown() {
        if (super.isDown()) {
            return true;
        }
        IMagic nowMagic = MagicUtils.getNowSelectMagic(client.player);
        if (nowMagic instanceof PressKeyMagic pressKeyMagic && this.KeyID.equals(pressKeyMagic.getKeyBindID())) {
            return ShapeShifterCurseFabricClient.activeSkillKeybind.isDown();
        }
        return false;
    }

    @Override
    public boolean consumeClick() {
        if (super.consumeClick()) {
            return true;
        }
        IMagic nowMagic = MagicUtils.getNowSelectMagic(client.player);
        if (nowMagic instanceof PressKeyMagic pressKeyMagic && this.KeyID.equals(pressKeyMagic.getKeyBindID())) {
            return ShapeShifterCurseFabricClient.activeSkillKeybind.consumeClick();
        }
        return false;
    }
}
