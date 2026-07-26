package net.onixary.shapeShifterCurseFabric.status_effects.transformative_effects;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.utils.TransformManager;
import net.onixary.shapeShifterCurseFabric.status_effects.BaseTransformativeStatusEffect;
import net.onixary.shapeShifterCurseFabric.status_effects.CTPUtils;

public class CustomTransformativeStatue extends BaseTransformativeStatusEffect {
    public CustomTransformativeStatue() {
        super(null, MobEffectCategory.NEUTRAL, 0xFFFFFF, false);
    }


    public IForm getToForm(Player player) {
        return CTPUtils.getTransformativePotionForm(player);
    }

    // 抽象方法：效果应用时的回调
    public void ActiveEffect(ServerPlayer player){
        IForm targetForm = this.getToForm(player);
        if (RegPlayerForms.ORIGINAL_BEFORE_ENABLE.isPlayerForm(player)) {
            return;
        }
        TransformManager.startTransform(player, targetForm, null);
        CTPUtils.resetTransformativePotionForm(player);
    };
}
