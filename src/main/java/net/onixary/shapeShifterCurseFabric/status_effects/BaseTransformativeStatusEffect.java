package net.onixary.shapeShifterCurseFabric.status_effects;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;

// 自定义药水效果基类（含类型和回调）
public abstract class BaseTransformativeStatusEffect extends MobEffect {
    public boolean IS_INSTANT = false;
    private final IForm toForm;

    public BaseTransformativeStatusEffect(IForm toForm, MobEffectCategory category, int color, boolean isInstant) {
        super(category, color);
        IS_INSTANT = isInstant;
        this.toForm = toForm;
    }

    public IForm getToForm(Player player) {
        return toForm;
    }

    // 抽象方法：效果应用时的回调
    public void ActiveEffect(ServerPlayer player){

    };
}
