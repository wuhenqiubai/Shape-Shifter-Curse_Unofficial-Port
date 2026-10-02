package net.onixary.shapeShifterCurseFabric.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerCapeModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.PlayerFormBodyType;
import net.onixary.shapeShifterCurseFabric.player_form.utils.ModifyCapeRender;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 披风 X 轴旋转角钳制 —— 1.21.1 {@code CapeFeatureRendererMixin.modifyXRotationAngle} 的迁入落点。
 *
 * <p><b>为什么换地方</b>：1.21.1 是在 {@code CapeLayer.render} 里 @ModifyArg 改
 * {@code Axis.rotationDegrees(...)} 的入参；1.21.11 把整段披风摆动计算搬进了
 * {@code PlayerCapeModel.setupAnim}，CapeLayer 里已无任何旋转调用，原注入点不存在。</p>
 *
 * <p><b>等价性</b>：1.21.1 的角度式为 {@code angle = 6.0F + capeLean / 2.0F + capeFlap}（度），
 * 原实现只钳制扣掉基础 6.0F 之后的部分到 [-10, 35]，再把整个 angle 交回。
 * 这里反算出 capeFlap 需要的修正量写回 render state，使 setupAnim 中的算式保持不变 ——
 * 钳制结果与 1.21.1 完全一致。</p>
 *
 * <p><b>注入点选 HEAD 的原因</b>：setupAnim 内部是 joml 的
 * {@code new Quaternionf().rotateY(...).rotateX(...)...} 链式调用，注入
 * {@code org.joml.Quaternionf} 这类第三方库方法在 remap 与 Connector 兼容性上均不可靠
 * （同 CustomEdibleItemMixin 的教训）。HEAD 不依赖方法体内部结构，注入点恒存在。</p>
 */
@Environment(EnvType.CLIENT)
@Mixin(PlayerCapeModel.class)
public class PlayerCapeModelMixin {

    // ⚠ 必须写完整描述符：目标类继承链上 setupAnim 有多个重载
    // （EntityModel.setupAnim(Object) / HumanoidModel.setupAnim(HumanoidRenderState)），
    // 只写方法名会让 Mixin 匹配到错误的目标。
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("HEAD"))
    private void ssc$clampCapeXRotation(AvatarRenderState avatarRenderState, CallbackInfo ci) {
        // 取一次存局部变量：level 在未进入世界时为 null，且二次取用无法通过 IDE 的非空推断
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Entity entity = level.getEntity(avatarRenderState.id);
        if (!(entity instanceof AbstractClientPlayer player)) {
            return;
        }
        if (!ssc$needModifyXRotationAngle(player)) {
            return;
        }
        // angle = 6.0F + capeLean / 2.0F + capeFlap，钳制的是扣掉基础 6.0F 之后的可动部分
        float movable = avatarRenderState.capeLean / 2.0F + avatarRenderState.capeFlap;
        float clamped = Mth.clamp(movable, -10.0F, 35.0F);
        avatarRenderState.capeFlap += clamped - movable;
    }

    @Unique
    private boolean ssc$needModifyXRotationAngle(AbstractClientPlayer player) {
        IForm curForm = FormTextureUtils.getPlayerForm_Render(player);
        if (curForm instanceof ModifyCapeRender mcr) {
            return mcr.NeedModifyXRotationAngle();
        } else {
            return curForm.getBodyType() == PlayerFormBodyType.FERAL;
        }
    }
}
