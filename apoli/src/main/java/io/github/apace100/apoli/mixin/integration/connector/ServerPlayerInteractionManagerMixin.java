package io.github.apace100.apoli.mixin.integration.connector;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ActionOnBlockBreakPower;
import io.github.apace100.apoli.power.ActionOnBlockUsePower;
import io.github.apace100.apoli.power.PreventBlockUsePower;
import io.github.apace100.apoli.util.HarvestContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 主包 {@code ServerPlayerInteractionManagerMixin} 的 Connector 替代版（对应 1.21.1 提交 ea98804）。
 *
 * <p>主版用 {@code @ModifyVariable(method = "destroyBlock", ... ordinal = 1)} 改「工具是否有效」这个局部 boolean，
 * {@code @Inject(..., ordinal = 4)} + {@code @Local} 取「是否真的挖掉 / 是否正确工具」两个局部变量 ——
 * NeoForge 重编译后局部变量顺序与编号都可能变，两处都会静默失效。
 *
 * <p>本版改用 Apoli 2.9.2 connector 的 ThreadLocal 方案：{@code destroyBlock HEAD} 缓存方块位置与
 * canHarvest 结果，{@code ModifyHarvestPower} 的判定移交给 {@link PlayerEntityMixin} 的
 * {@code hasCorrectToolForDrops} HEAD（见该文件），破坏动作改在 {@code RETURN} 处读缓存执行。
 *
 * <p>注意 {@code HarvestContext.setCanHarvest(player.hasCorrectToolForDrops(...))} 这一句本身就会走到
 * {@link PlayerEntityMixin}，因此在 BLOCK_POSITION 已写入之后再调用，得到的就是**被 power 改写过的**结果 ——
 * 与主版 {@code @Local(ordinal = 1) hasCorrectTool} 取的是同一个值。
 *
 * <p>由 {@link io.github.apace100.apoli.ApoliMixinPlugin} 在 Connector 环境下启用、Fabric 下跳过。
 */
@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerInteractionManagerMixin {

    @Shadow
    public ServerLevel level;

    @Shadow
    public ServerPlayer player;

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void cacheBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        HarvestContext.setBlockPosition(level, pos);
        HarvestContext.setCanHarvest(player.hasCorrectToolForDrops(level.getBlockState(pos)));
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void actionOnBlockBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            boolean harvested = HarvestContext.getCanHarvest();
            PowerHolderComponent.getPowers(player, ActionOnBlockBreakPower.class)
                .stream()
                .filter(p -> p.doesApply(HarvestContext.getBlockPosition()))
                .forEach(p -> p.executeActions(harvested, pos, null));
        }
        HarvestContext.clearCanHarvest();
        HarvestContext.clearBlockPosition();
    }

    @Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isSecondaryUseActive()Z"), cancellable = true)
    private void preventBlockInteraction(ServerPlayer player, Level world, ItemStack stack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if(PowerHolderComponent.getPowers(player, PreventBlockUsePower.class).stream().anyMatch(p -> p.doesPrevent(world, hitResult.getBlockPos()))) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
    private void executeBlockUseActions(ServerPlayer player, Level world, ItemStack stack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        PowerHolderComponent.getPowers(player, ActionOnBlockUsePower.class).stream()
            .filter(p -> p.shouldExecute(hitResult.getBlockPos(), hitResult.getDirection(), hand, stack))
            .forEach(p -> p.executeAction(hitResult.getBlockPos(), hitResult.getDirection(), hand));
    }
}
