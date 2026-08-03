package net.onixary.shapeShifterCurseFabric.integration.origins.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.onixary.shapeShifterCurseFabric.integration.origins.power.ModifyTypeTagPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Intercepts {@link EntityType#is(TagKey)} to also check
 * {@link ModifyTypeTagPower}. The entity context is provided via
 * {@link ModifyTypeTagPower#CURRENT_ENTITY} ThreadLocal.
 *
 * TODO(Apoli-Legacy 2.12.10 降级): 1.21.1 用 InTagConditionMixin 在 Apoli 的 in_tag 条件前设置 CURRENT_ENTITY，
 * 但 2.12.10（1.20 移植）没有 InTagCondition 类，CURRENT_ENTITY 无设置者 → modify_type_tag 的 tag 效果
 * 目前不生效（power 能正常加载，报错已消除）。若需恢复需给 2.12.10 的 EntityConditions.in_tag/entity_group
 * 条件补注入点。
 */
@Mixin(EntityType.class)
public abstract class ModifyTypeTagMixin {

    @Inject(method = "is(Lnet/minecraft/tags/TagKey;)Z", at = @At("RETURN"), cancellable = true)
    private void calio$modifyIsIn(TagKey<?> tagKey, CallbackInfoReturnable<Boolean> cir) {
        if (!tagKey.isFor(Registries.ENTITY_TYPE)) {
            return;
        }
        if (cir.getReturnValue()) {
            return;
        }

        var entity = ModifyTypeTagPower.CURRENT_ENTITY.get();
        if (entity != null) {
            @SuppressWarnings("unchecked")
            TagKey<EntityType<?>> entityTag = (TagKey<EntityType<?>>) tagKey;
            if (ModifyTypeTagPower.isEntityInTag(entity.getId(), entityTag)) {
                cir.setReturnValue(true);
            }
        }
    }
}
