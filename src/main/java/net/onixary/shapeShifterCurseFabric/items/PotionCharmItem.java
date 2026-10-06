package net.onixary.shapeShifterCurseFabric.items;

import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.entity.PotionCharmArrowEntity;
import net.onixary.shapeShifterCurseFabric.entity.RegCustomEntity;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;
import org.jetbrains.annotations.NotNull;

public class PotionCharmItem extends Item {
    // Potions.STRONG_* 在 Mojmap 里是 Holder<Potion>（不是 Potion），与 PotionContents.createItemStack 的入参一致
    private final Holder<Potion> potion;
    public PotionCharmItem(Holder<Potion> potion) { super(new Properties()); this.potion = potion; }
    @Override public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!FormUtils.getPlayerForm(player).getFormID().equals(ShapeShifterCurseFabric.identifier("familiar_fox_3"))) {
            return InteractionResultHolder.fail(stack);
        }
        if (!world.isClientSide) {
            var arrow = new PotionCharmArrowEntity(RegCustomEntity.POTION_CHARM_ARROW, world);
            arrow.setOwner(player);
            arrow.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            // Yarn 的 PotionUtil.setPotion(stack, potion) 对应这里的静态工厂 createItemStack（直接建好带药水的物品栈）
            arrow.initFromStack(PotionContents.createItemStack(Items.TIPPED_ARROW, potion));
            // 按 pitch/yaw 发射用 shootFromRotation；setDeltaMovement 只能直接设速度向量
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, 3, 0);
            if (!world.addFreshEntity(arrow)) return InteractionResultHolder.fail(stack);
            if (!player.isCreative()) stack.shrink(1);
        }
        // Yarn 的 TypedActionResult.success(stack, isClient) 对应 sidedSuccess：客户端 SUCCESS、服务端 CONSUME
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
    }
}
