package net.onixary.shapeShifterCurseFabric.render.tech;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.LoopType;
import com.geckolib.animation.object.PlayState;
import com.zigythebird.playeranimcore.animation.Animation;

public class EmptyAnimatable implements GeoAnimatable {
    AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(animationState -> {
            // GL5 的 RawAnimation.then 需要 GeckoLib 自己的 LoopType
            // （software.bernie.geckolib.animation.object.LoopType，是个带 LOOP 静态常量的接口）。
            // 原写法 (LoopType) Animation.LoopType.LOOP 是从 PAL API 照搬的：PAL 的 Animation.LoopType
            // 与 GeckoLib 的 LoopType 是两个毫不相关的类型，这个强转会在渲染茧 overlay 时抛
            // ClassCastException（实体一旦被 entangled 就崩客户端）。
            animationState.setAnimation(RawAnimation.begin().then("idle", LoopType.LOOP));
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}