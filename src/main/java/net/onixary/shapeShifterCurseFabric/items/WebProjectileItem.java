package net.onixary.shapeShifterCurseFabric.items;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;
import net.onixary.shapeShifterCurseFabric.entity.projectile.WebBullet;

public class WebProjectileItem extends Item {
    public WebProjectileItem(Item.Properties settings) { super(settings); }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide()) {
            WebBullet bullet = new WebBullet(player, 3);
            bullet.EnableVenomSpindle = false;
            bullet.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, 2, 1);
            if (!world.addFreshEntity(bullet)) return InteractionResult.FAIL;
            if (!player.isCreative()) stack.shrink(1);
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        return world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }
}
