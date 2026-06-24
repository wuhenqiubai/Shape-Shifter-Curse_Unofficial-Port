package net.onixary.shapeShifterCurseFabric.status_effects;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;

/**
 * 变形状态效果基类。扩展原版 {@link StatusEffect}，增加目标形态和效果激活回调。
 * <p>
 * 当玩家被施加此效果时，会通过 {@link #getToForm} 获取目标形态，
 * 并通过 {@link #ActiveEffect} 触发施加时的附加逻辑。
 * 最终由 {@link net.onixary.shapeShifterCurseFabric.status_effects.transformative_effects.TransformativeStatusInstance} 协调变形流程。
 *
 * @see net.onixary.shapeShifterCurseFabric.status_effects.transformative_effects.TransformativeStatusInstance
 */
// 自定义药水效果基类（含类型和回调）
public abstract class BaseTransformativeStatusEffect extends StatusEffect {
    public boolean IS_INSTANT = false;
    private final IForm toForm;

    public BaseTransformativeStatusEffect(IForm toForm, StatusEffectCategory category, int color, boolean isInstant) {
        super(category, color);
        IS_INSTANT = isInstant;
        this.toForm = toForm;
    }

    public IForm getToForm(PlayerEntity player) {
        return toForm;
    }

    // 抽象方法：效果应用时的回调
    public void ActiveEffect(ServerPlayerEntity player){

    }
}
