package net.onixary.shapeShifterCurseFabric.mixin.accessor;

import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * NeoForge/Connector 兼容：Structure.settings 是 private final 字段，原靠 accesswidener 放行
 * （accessible + mutable），但 NeoForge 下 Connector 不应用 SSC 的 accesswidener → IllegalAccessError。
 * 改用 @Accessor mixin（Fabric 下也无需 AW，跨环境统一；@Accessor 字段名经 refmap 重映射，
 * NeoForge 下也能定位）。参考 ver/1.21.1_Connector 分支的修补。
 */
@Mixin(Structure.class)
public interface StructureAccessor {
    @Accessor("settings")
    @Mutable
    void ssc_setSettings(Structure.StructureSettings settings);
}
