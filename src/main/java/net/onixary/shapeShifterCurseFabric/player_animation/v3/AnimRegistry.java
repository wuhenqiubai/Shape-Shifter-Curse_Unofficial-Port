package net.onixary.shapeShifterCurseFabric.player_animation.v3;

import com.google.gson.JsonObject;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_animation.AnimationHolder;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

// 注册包含AnimState AnimStateController AnimFSM的类
public class AnimRegistry {
    public static class AnimState {
        public AbstractAnimStateController defaultController;

        public AnimState(AbstractAnimStateController defaultController) {
            this.defaultController = defaultController;
        }
    }

    public static class PowerDefaultAnim {  // 覆写时的defaultAnimationData应该为null
        boolean IsRegistered = false;
        AnimUtils.AnimationHolderData defaultAnimationData = null;
        AnimationHolder defaultAnimation = null;
        public PowerDefaultAnim() {
            this((AnimUtils.AnimationHolderData)null);
        }

        public PowerDefaultAnim(@Nullable AnimUtils.AnimationHolderData defaultAnimationData) {
            this.defaultAnimationData = defaultAnimationData;
        }

        public @Nullable AnimationHolder getAnim(Player player, AnimSystem.AnimSystemData animSystemData) {
            return this.defaultAnimation;
        }

        public void registerAnim(Player player, AnimSystem.AnimSystemData animSystemData) {
            if (this.IsRegistered) {
                return;
            }
            if (this.defaultAnimationData != null) {
                this.defaultAnimation = this.defaultAnimationData.build();
            }
            this.IsRegistered = true;
        }

        // 用于动画系统 不推荐覆写 修改逻辑推荐覆写getAnim + registerAnim
        public @Nullable AnimationHolder ANIM_SYSTEM_GET_CURRENT_ANIM(Player player, AnimSystem.AnimSystemData animSystemData) {
            if (!this.IsRegistered) {
                this.registerAnim(player, animSystemData);
            }
            return this.getAnim(player, animSystemData);
        }
    }

    public static ResourceKey<Registry<AnimState>> animStateRegistryKey = ResourceKey.createRegistryKey(ShapeShifterCurseFabric.identifier("anim_state"));
    public static Registry<AnimState> animStateRegistry = new MappedRegistry<>(animStateRegistryKey, Lifecycle.stable());

    public static ResourceKey<Registry<Function<JsonObject, AbstractAnimStateController>>> animStateControllerRegistryKey = ResourceKey.createRegistryKey(ShapeShifterCurseFabric.identifier("anim_state_controller"));
    public static Registry<Function<JsonObject, AbstractAnimStateController>> animStateControllerRegistry = new MappedRegistry<>(animStateControllerRegistryKey, Lifecycle.stable());

    public static ResourceKey<Registry<AbstractAnimFSM>> animFSMRegistryKey = ResourceKey.createRegistryKey(ShapeShifterCurseFabric.identifier("anim_fsm"));
    public static Registry<AbstractAnimFSM> animFSMRegistry = new MappedRegistry<>(animFSMRegistryKey, Lifecycle.stable());

    public static ResourceKey<Registry<PowerDefaultAnim>> powerAnimIDRegistryKey = ResourceKey.createRegistryKey(ShapeShifterCurseFabric.identifier("power_anim_id"));
    public static Registry<PowerDefaultAnim> powerAnimIDRegistry = new MappedRegistry<>(powerAnimIDRegistryKey, Lifecycle.stable());

    public static Identifier registerAnimState(Identifier identifier, AnimState animState) {
        Registry.register(animStateRegistry, identifier, animState);
        return identifier;
    }

    // 用于数据包
    public static Identifier registerAnimStateController(Identifier identifier, Function<JsonObject, AbstractAnimStateController> animStateController) {
        Registry.register(animStateControllerRegistry, identifier, animStateController);
        return identifier;
    }

    public static Identifier registerAnimFSM(Identifier identifier, AbstractAnimFSM animFSM) {
        Registry.register(animFSMRegistry, identifier, animFSM);
        return identifier;
    }

    public static Identifier registerPowerDefaultAnim(Identifier identifier, PowerDefaultAnim powerDefaultAnim) {
        Registry.register(powerAnimIDRegistry, identifier, powerDefaultAnim);
        return identifier;
    }

    public static @Nullable AnimState getAnimState(Identifier identifier) {
        return animStateRegistry.getOptional(identifier).orElse(null);
    }

    // 每个Form里都有一个预设了不同参数的AnimStateController
    public static @Nullable AbstractAnimStateController getAnimStateController(Identifier identifier, JsonObject jsonData) {
        Function<JsonObject, AbstractAnimStateController> animStateController = animStateControllerRegistry.getOptional(identifier).orElse(null);
        if (animStateController != null) {
            return animStateController.apply(jsonData);
        }
        return null;
    }

    public static @Nullable Function<JsonObject, AbstractAnimStateController> getAnimStateControllerSupplier(Identifier identifier) {
        return animStateControllerRegistry.getOptional(identifier).orElse(null);
    }

    public static @Nullable AbstractAnimFSM getAnimFSM(Identifier identifier) {
        return animFSMRegistry.getOptional(identifier).orElse(null);
    }

    public static @Nullable PowerDefaultAnim getPowerDefaultAnim(Identifier identifier) {
        return powerAnimIDRegistry.getOptional(identifier).orElse(null);
    }

    static {
        AnimRegistries.register();  // 注册基础的注册项 空方法体 用于强行加载AnimRegistries 别删
    }
}