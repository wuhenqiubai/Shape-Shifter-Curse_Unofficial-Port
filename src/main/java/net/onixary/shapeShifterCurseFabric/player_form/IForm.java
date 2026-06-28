package net.onixary.shapeShifterCurseFabric.player_form;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.data.CodexData;
import net.onixary.shapeShifterCurseFabric.player_animation.AnimationHolder;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AbstractAnimStateController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimSystem;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * 新形态系统核心接口。定义形态的生命周期方法和变形链协议。
 * <p>
 * 与旧系统 {@link net.onixary.shapeShifterCurseFabric.player_form.PlayerFormBase} 不同，
 * 新系统将所有形态属性整合为 {@code Flag} 集合，并通过 {@link ITransformReason} 处理变形逻辑。
 * <p>
 * 变形链协议：{@link #_getNextForm} → {@link #getNextForm} / {@link #getDefaultNextForm} / reason.getFallBackNextForm
 * 使用责任链模式，每个步骤返回 null 则进入下一级兜底。
 * <p>
 * 注意：此系统尚未完全启用（标记 TODO），当前活跃形态系统仍为 {@code PlayerFormBase}。
 *
 * @see IFormGroup
 * @see ITransformReason
 * @see FormUtils
 */
// 新形态变形引擎代码
public interface IForm {
	/**
	 * @return 形态唯一 ID
	 */
    @NotNull Identifier getFormID();

	/**
	 * 形态标志集合（HasSlowFall 等已整合进此标志系统）。
	 *
	 * @return 标志字符串集合
     */
    @NotNull Set<String> getFormFlag();

	/** @return 形态在组内的层级（类似旧系统的 {@code FormIndex}） */
	int getFormTier();

	/** @return 所属形态组 */
    @Nullable IFormGroup getFormGroup();

	/** 设置形态组及层级。 */
	void setFormGroup(IFormGroup group, int formTier);

	/** @return 临时能力系统的层标识（等 Origins 移除后再完善） */
    @NotNull Pair<Identifier, Identifier> getFormLayer();


    @NotNull PlayerFormBodyType getBodyType();

    // 将 Name 合并进 ContentType
    default @NotNull Text getContentText(CodexData.ContentType type) {
        return Text.translatable("codex.form." + this.getFormID().getNamespace() + "." + this.getFormID().getPath() + "." + type.toString().toLowerCase());
    }

    // 变形系统
    default @NotNull IForm _getNextForm(PlayerEntity player, ITransformReason reason) {
        IForm nextForm = getNextForm(player, reason);
        if (nextForm == null) {
            nextForm = reason.getFallBackNextForm(player, this);
        }
        if (nextForm == null) {
            nextForm = getDefaultNextForm(player, reason);
        }
        // 按代码来说是不可能为null的 但是getDefaultXXXX可能会被大量重载 所以还是加一个判断
        if (nextForm == null) {
            nextForm = this;
            ShapeShifterCurseFabric.LOGGER.error("Form {} has no next form, something wrong!", this.getFormID());
        }
        return nextForm;
    }

    default @NotNull IForm _getPrevForm(PlayerEntity player, ITransformReason reason) {
        IForm prevForm = getPrevForm(player, reason);
        if (prevForm == null) {
            prevForm = reason.getFallBackPrevForm(player, this);
        }
        if (prevForm == null) {
            prevForm = getDefaultPrevForm(player, reason);
        }
        // 按代码来说是不可能为null的 但是getDefaultXXXX可能会被大量重载 所以还是加一个判断
        if (prevForm == null) {
            prevForm = this;
            ShapeShifterCurseFabric.LOGGER.error("Form {} has no prev form, something wrong!", this.getFormID());
        }
        return prevForm;
    }

    // 选择性处理 如果不匹配则必须返回null
    public default @Nullable IForm getNextForm(PlayerEntity player, ITransformReason reason) {
        return null;
    }

    default @Nullable IForm getPrevForm(PlayerEntity player, ITransformReason reason) {
        return null;
    }

    default @NotNull IForm getDefaultNextForm(PlayerEntity player, ITransformReason reason) {
        IFormGroup group = this.getFormGroup();
        int tier = this.getFormTier() + 1;
        IForm result = null;
        if (group != null) {
            result = group.getRandomForm(tier, player.getRandom(), null);
        }
        return result == null ? this : result;
    }

    default @NotNull IForm getDefaultPrevForm(PlayerEntity player, ITransformReason reason) {
        IForm prevForm = FormUtils.getPrevForm(player);
        int tier = this.getFormTier() - 1;
        if (prevForm != null && prevForm.getFormTier() == tier) {
            return prevForm;
        }
        IFormGroup group = this.getFormGroup();
        IForm result = null;
        if (group != null) {
            result = group.getRandomForm(tier, player.getRandom(), null);
        }
        return result == null ? this : result;
    }

    // 动画系统
    default @Nullable AbstractAnimStateController getAnimStateController(PlayerEntity player, AnimSystem.AnimSystemData animSystemData, @NotNull Identifier animStateID) {
        return null;
    }

    default void registerPowerAnim(PlayerEntity player, AnimSystem.AnimSystemData animSystemData) { }

    default boolean isPowerAnimRegistered(PlayerEntity player, AnimSystem.AnimSystemData animSystemData) {
        return true;
    }

    default @NotNull Pair<Boolean, @Nullable AnimationHolder> getPowerAnim(PlayerEntity player, AnimSystem.AnimSystemData animSystemData, @NotNull Identifier powerAnimID) {
        return new Pair<>(false, null);
    }

	/**
	 * 变形转换钩子。调用顺序为：
	 * <ol>
	 *   <li>当前形态的 {@code onTransform_To}</li>
	 *   <li>目标形态的 {@code onTransform_From}</li>
	 *   <li>目标形态的 {@code onTransform_Finish}</li>
	 * </ol>
     */
    default void onTransform_From(PlayerEntity player, IForm prevForm) {
    }

	/** 变形完成钩子。 */
    default void onTransform_Finish(PlayerEntity player) {
    }

	/** 变形开始钩子（准备切换到下一个形态时调用）。 */
    default void onTransform_To(PlayerEntity player, IForm nextForm) {
    }

    default void onApplyPowerEnd(PlayerEntity player) { }

    // Scale 系统
    // 先这样写 等我之后翻一下 pehkui 的代码
	/**
	 * 应用形态的缩放。
	 * <p>
	 * 通过 Pehkui API 修改玩家实体大小。
	 */
    public void applyScale(PlayerEntity player);

	/**
	 * 比较两个形态是否相等（基于 {@link #getFormID}）。
	 * <p>
	 * 注：接口无法覆写 {@code Object.equals}，因此单独提供此方法。
     */
    default boolean isEquals(IForm form) {
        return form != null && this.getFormID().equals(form.getFormID());
    }

    default boolean isPlayerForm(PlayerEntity player) {
        IForm playerForm = FormUtils.getPlayerForm(player);
        return this.isEquals(playerForm);
    }

    default boolean isDynamicForm() {
        return false;
    }
}
