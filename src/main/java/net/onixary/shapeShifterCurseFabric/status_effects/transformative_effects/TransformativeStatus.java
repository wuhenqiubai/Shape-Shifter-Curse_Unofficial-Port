package net.onixary.shapeShifterCurseFabric.status_effects.transformative_effects;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.utils.TransformManager;
import net.onixary.shapeShifterCurseFabric.status_effects.BaseTransformativeStatusEffect;

public class TransformativeStatus extends BaseTransformativeStatusEffect {
    public TransformativeStatus(IForm toForm) {
        super(toForm, MobEffectCategory.NEUTRAL, 0xFFFFFF, false);
    }

    @Override
    public void ActiveEffect(ServerPlayer player) {
        TransformManager.startTransform(player, this.getToForm(player), null);
    }
}
