package net.onixary.shapeShifterCurseFabric.mixin.integration;

import com.tacz.guns.compat.playeranimator.PlayerAnimatorCompat;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.networking.BytePayload;
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsS2C;
import net.onixary.shapeShifterCurseFabric.player_form.PlayerFormBodyType;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 形态切换时清掉 TaCZ 残留的 PlayerAnimator 动画层。
 *
 * <p><b>为什么需要这个</b>：{@code TacZ_AnimThird} 已保证第三人称每帧清理，但<b>第一人称</b>下
 * TaCZ 的四个事件（onFire/onReload/onMelee/onDraw）都会因 {@code cameraType.isFirstPerson()} 提前 return，
 * 第一人称手臂渲染也不走 {@code HumanoidModel.setupAnim} 的 TAIL，于是「切到 FERAL 之前写进
 * TaCZ 层（优先级 93-96，高于 SSC 的 1）的动画」可能在纯第一人称下一直不被释放。</p>
 *
 * <p><b>为什么做成 mixin 而不是直接改 {@code ModPacketsS2C}</b>：后者是普通类、会被无条件加载，
 * 在其中引用 TaCZ 的类会在未装 TaCZ 时抛 {@code NoClassDefFoundError}。做成 mixin 后可由
 * {@code MixinConfigPlugin} 的 {@code tacz} 前置条件控制是否注入。</p>
 *
 * <p><b>注意</b>：{@code receiveFormChange} 把形态刷新提交到了客户端线程
 * （{@code ctx.client().execute(...)}），所以这里也提交一次，保证排在其后、形态已同步完成。
 * 判定用「当前形态」而非包里的 newFormID —— 后者是原方法的局部变量，TAIL 处拿不到。</p>
 */
// ⚠ remap = false：ModPacketsS2C 是本模组自己的类（非 MC 类），不需要也不应做混淆重映射，
// 否则 Mixin 注解处理器会去找 receiveFormChange 的 obf 映射并失败。
@Mixin(value = ModPacketsS2C.class, remap = false)
public class TacZ_AnimFormChange {
    @Inject(method = "receiveFormChange", at = @At("TAIL"))
    private static void ssc$clearTaczAnimationOnFeral(BytePayload payload, ClientPlayNetworking.Context ctx, CallbackInfo ci) {
        ctx.client().execute(() -> {
            Player player = ctx.client().player;
            if (!(player instanceof AbstractClientPlayer)) {
                return;
            }
            if (FormTextureUtils.getPlayerForm_Render(player).getBodyType() != PlayerFormBodyType.FERAL) {
                return;
            }
            PlayerAnimatorCompat.stopAllAnimation(player);
        });
    }
}
