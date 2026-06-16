package net.onixary.shapeShifterCurseFabric.player_animation.v3;

import com.google.gson.JsonObject;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_animation.AnimationHolder;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateControllerDP.EmptyController;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

/**
 * 动画工具类。提供动画数据的读取、反序列化、播放控制等核心功能。
 * <p>
 * 全库 77 个来源引用此工具类，主要用于：
 * <ul>
 *   <li>{@link #readAnim} / {@link #readAnimInJson} — 从 JSON 反序列化动画数据</li>
 *   <li>{@link #readController} — 从 JSON 反序列化动画状态控制器</li>
 *   <li>{@link #playPowerAnimWithTime} / {@link #playPowerAnimWithCount} — 播放 Power 动画的便捷入口</li>
 *   <li>{@link AnimationHolderData} — 动画数据的构建期表示，最终通过 {@link AnimationHolderData#build} 产出 {@link AnimationHolder}</li>
 * </ul>
 * <p>
 * 此类的 {@link #EMPTY_ANIM} 和 {@link #EMPTY_CONTROLLER} 作为哨兵对象，用于跳过特定动画或控制器的处理。
 *
 * @see AnimationHolder
 * @see AnimationHolderData
 */
public class AnimUtils {
    public static @NotNull AnimationHolderData readAnim(JsonObject jsonData) {
        try {
            Identifier AnimID = Identifier.tryParse(jsonData.get("animID").getAsString());
            float Speed = 1.0f;
            int Fade = 2;
            if (jsonData.has("speed")) {
                Speed = jsonData.get("speed").getAsFloat();
            }
            if (jsonData.has("fade")) {
                Fade = jsonData.get("fade").getAsInt();
            }
            return new AnimUtils.AnimationHolderData(AnimID, Speed, Fade);
        }
        catch(Exception e) {
	        ShapeShifterCurseFabric.LOGGER.warn("Error while loading player animation: {}", e.getMessage());
            return EMPTY_ANIM;
        }
    }

    public static final String ANIM_CONTROLLER_TYPE_KEY = "controllerType";

    private static class EmptyAnimationHolderData extends AnimationHolderData {
        public EmptyAnimationHolderData() {
            super(null, 0.0f, 0);
        }
        @Override
        public AnimationHolder build() {
            return null;
        }
    }

    public static AnimationHolderData EMPTY_ANIM = new EmptyAnimationHolderData();
    public static AbstractAnimStateController EMPTY_CONTROLLER = new EmptyController();

    public static @NotNull AbstractAnimStateController readController(JsonObject jsonData) {
        try {
            Identifier ControllerType = Identifier.tryParse(jsonData.get(ANIM_CONTROLLER_TYPE_KEY).getAsString());
            Function<JsonObject, AbstractAnimStateController> controllerFactory = AnimRegistry.getAnimStateControllerSupplier(ControllerType);
            if (controllerFactory != null) {
                return controllerFactory.apply(jsonData);
            } else {
	            ShapeShifterCurseFabric.LOGGER.warn("Unknown animation controller type: {}", ControllerType);
                return EMPTY_CONTROLLER;
            }
        } catch (Exception e) {
	        ShapeShifterCurseFabric.LOGGER.warn("Error while loading player animation: {}", e.getMessage());
            return EMPTY_CONTROLLER;
        }
    }

    // 还是防一下AnimationHolderData=null的情况吧
    public static @NotNull AnimationHolderData ensureAnimHolderDataNotNull(AnimationHolderData animationHolderData) {
        if (animationHolderData == null) {
            return EMPTY_ANIM;
        }
        else {
            return animationHolderData;
        }
    }

    public static @NotNull AnimationHolderData readAnimInJson(JsonObject jsonObject, String Key, @Nullable AnimationHolderData defaultValue) {
        if (ANIM_CONTROLLER_TYPE_KEY.equals(Key)) {
            throw new IllegalArgumentException("Cannot read animation from controllerType");
        }
        if (jsonObject.has(Key) && jsonObject.get(Key).isJsonObject()) {
            return readAnim(jsonObject.get(Key).getAsJsonObject());
        } else {
            return ensureAnimHolderDataNotNull(defaultValue);
        }
    }

    /**
     * 按时间播放 Power 动画（播完指定时长后停止）。
     *
     * @param playerEntity 目标玩家
     * @param powerAnimID  Power 动画 ID
     * @param animDuration 动画时长（tick）
     * @param sendSideType 播放端类型
     */
    public static void playPowerAnimWithTime(PlayerEntity playerEntity, Identifier powerAnimID, int animDuration, AnimationSendSideType sendSideType) {
        if (!sendSideType.canPlayAnim(playerEntity)) {
            return;
        }
        if (playerEntity instanceof IPlayerAnimController playerAnimController) {
            playerAnimController.shape_shifter_curse$playAnimationWithTime(powerAnimID, animDuration);
        } else {
        }
    }

    /**
     * 按次数播放 Power 动画（播完指定次数后停止）。
     *
     * @param playerEntity 目标玩家
     * @param powerAnimID  Power 动画 ID
     * @param animCount    播放次数
     * @param sendSideType 播放端类型
     */
    public static void playPowerAnimWithCount(PlayerEntity playerEntity, Identifier powerAnimID, int animCount, AnimationSendSideType sendSideType) {
        if (!sendSideType.canPlayAnim(playerEntity)) {
            return;
        }
        if (playerEntity instanceof IPlayerAnimController playerAnimController) {
            playerAnimController.shape_shifter_curse$playAnimationWithCount(powerAnimID, animCount);
        } else {
        }
    }

    /**
     * 循环播放 Power 动画（直到调用 {@link #stopPowerAnim} 停止）。
     *
     * @param playerEntity 目标玩家
     * @param powerAnimID  Power 动画 ID
     * @param sendSideType 播放端类型
     */
    public static void playPowerAnimLoop(PlayerEntity playerEntity, Identifier powerAnimID, AnimationSendSideType sendSideType) {
        if (!sendSideType.canPlayAnim(playerEntity)) {
            return;
        }
        if (playerEntity instanceof IPlayerAnimController playerAnimController) {
            playerAnimController.shape_shifter_curse$playAnimationLoop(powerAnimID);
        } else {
        }
    }

    /**
     * 停止当前正在播放的 Power 动画。
     *
     * @param playerEntity 目标玩家
     * @param sendSideType 执行端类型
     */
    public static void stopPowerAnim(PlayerEntity playerEntity, AnimationSendSideType sendSideType) {
        if (!sendSideType.canPlayAnim(playerEntity)) {
            return;
        }
        if (playerEntity instanceof IPlayerAnimController playerAnimController) {
            playerAnimController.shape_shifter_curse$stopAnimation();
        } else {
        }
    }

    /**
     * 动画发送端的类型，用于控制 Power 动画在客户/服务端的执行策略。
     * <p>
     * 各端调用 {@link #playPowerAnimWithTime} 等便捷方法时通过此参数决定哪些端触发动画播放。
     */
    public enum AnimationSendSideType {
        /**
         * 仅在客户端播放
         */
        ONLY_CLIENT((player -> player.getWorld().isClient)),
        /** 仅在服务端播放 */
        ONLY_SERVER((player -> !player.getWorld().isClient)),
        /** 不播放 */
        NONE((player -> false)),
        /** 双端均播放 */
        BOTH_SIDE((player -> true));

        private final Function<PlayerEntity, Boolean> canPlayAnimCondition;

        public boolean canPlayAnim(PlayerEntity player) {
            return this.canPlayAnimCondition.apply(player);
        }

        AnimationSendSideType(Function<PlayerEntity, Boolean> canPlayAnimCondition) {
            this.canPlayAnimCondition = canPlayAnimCondition;
        }
    }

    /**
     * 动画持有者的构建期数据。存储动画 ID、速度、淡入时长等信息，
     * 通过 {@link #build} 方法懒加载并缓存为 {@link AnimationHolder} 实例。
     * <p>
     * 所有 19+ 个形态类都通过此类定义动画，是全库被引用最多的类型之一（76 引用）。
     * 支持链式 setter 调用：{@code new AnimationHolderData(id).setSpeed(2f).setFade(4)}。
     * <p>
     * 注意：{@link #build} 内部做了懒加载（animationHolder 缓存），相同参数多次调用返回同一实例。
     * 如需新实例应使用 {@link #makeCopy}。
     */
    public static class AnimationHolderData {
        public Identifier AnimID;
        public float Speed;
        public int Fade;
        public @Nullable String Easing;
        public boolean skipFade;
        private @Nullable AnimationHolder animationHolder;
        public AnimationHolderData(Identifier AnimID, float Speed, int Fade, @Nullable String easing) {
            this.AnimID = AnimID;
            this.Speed = Speed;
            this.Fade = Fade;
            this.Easing = easing;
        }
        public AnimationHolderData(Identifier AnimID, float Speed, int Fade) {
            this(AnimID, Speed, Fade, null);
        }

        public AnimationHolderData setAnimID(Identifier AnimID) {
            this.AnimID = AnimID;
            return this;
        }

        public AnimationHolderData setAnimID(AnimationHolderData otherAnimationHolder) {
            this.AnimID = otherAnimationHolder.AnimID;
            return this;
        }

        public AnimationHolderData setSpeed(float Speed) {
            this.Speed = Speed;
            return this;
        }

        public AnimationHolderData setFade(int Fade) {
            this.Fade = Fade;
            return this;
        }

        public AnimationHolderData makeCopy() {
            return new AnimationHolderData(AnimID, Speed, Fade);
        }

        public AnimationHolderData(Identifier AnimID, float Speed) {
            this(AnimID, Speed, 2);
        }

        public AnimationHolderData(Identifier AnimID) {
            this(AnimID, 1.0f, 2);
        }

        public AnimationHolder build() {
            if (animationHolder == null) {
                animationHolder = new AnimationHolder(AnimID, true, Speed, Fade);
                if (Easing != null) animationHolder.setEasingTypeName(Easing);
                if (skipFade) animationHolder.setSkipFade(true);
            }
            return animationHolder;
        }
    }

    public static void stopPowerAnimWithIDs(PlayerEntity playerEntity, AnimationSendSideType sendSideType, List<Identifier> powerAnimIDs) {
        stopPowerAnimWithIDs(playerEntity, sendSideType, powerAnimIDs.toArray(new Identifier[0]));
    }

    public static boolean stopPowerAnimWithIDs(PlayerEntity playerEntity, AnimationSendSideType sendSideType, Identifier... powerAnimIDs) {
        if (!sendSideType.canPlayAnim(playerEntity)) {
            return false;
        }
        if (playerEntity instanceof IPlayerAnimController playerAnimController) {
            @Nullable Identifier nowAnimID = playerAnimController.shape_shifter_curse$getPowerAnimationID();
            for (Identifier powerAnimID : powerAnimIDs) {
                if (powerAnimID.equals(nowAnimID)) {
                    stopPowerAnim(playerEntity, sendSideType);
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }
}
