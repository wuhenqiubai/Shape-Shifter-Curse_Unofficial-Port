package net.onixary.shapeShifterCurseFabric.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 访问 {@link Font} 的 provider 字段。
 * ScaleTextRenderer 需要 provider 来构造（super(Provider)），但运行时字段是 intermediary 混淆名，
 * 反射 getDeclaredField("provider") 找不到 → 必须用 @Accessor（mixin 处理器会映射字段名）。
 */
@Environment(EnvType.CLIENT)
@Mixin(Font.class)
public interface FontAccessor {
    @Accessor("provider")
    Font.Provider getProvider();
}
