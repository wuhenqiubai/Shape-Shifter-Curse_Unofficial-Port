package net.onixary.shapeShifterCurseFabric.items;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;
import net.onixary.shapeShifterCurseFabric.entity.projectile.WebBullet;

public class WebProjectileItem extends Item {
    public WebProjectileItem(Item.Properties settings) { super(settings); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            WebBullet bullet = new WebBullet(player, 3);
            bullet.EnableVenomSpindle = false;
            bullet.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, 2, 1);
            if (!world.addFreshEntity(bullet)) return InteractionResultHolder.fail(stack);
            if (!player.isCreative()) stack.shrink(1);
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        // Yarn TypedActionResult.success(stack, isClient) → Mojmap sidedSuccess
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
    }
}
