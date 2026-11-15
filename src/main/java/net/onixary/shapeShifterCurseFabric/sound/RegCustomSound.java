package net.onixary.shapeShifterCurseFabric.sound;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.Identifier;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

public class RegCustomSound {
    public static final Identifier AVALI_CHATTER_ID = ShapeShifterCurseFabric.identifier("avali_chatter");
    public static final SoundEvent AVALI_CHATTER = Registry.register(
            BuiltInRegistries.SOUND_EVENT, AVALI_CHATTER_ID, SoundEvent.createVariableRangeEvent(AVALI_CHATTER_ID));

    public static void init() {
    }
}
