package net.onixary.shapeShifterCurseFabric.items;

import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
    private final Holder<Potion> potion;
    public PotionCharmItem(Holder<Potion> potion, Properties properties) { super(properties); this.potion = potion; }
    @Override public @NotNull InteractionResult use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!FormUtils.getPlayerForm(player).getFormID().equals(ShapeShifterCurseFabric.identifier("familiar_fox_3"))) {
            return InteractionResult.FAIL;
        }
        if (!world.isClientSide()) {
            var arrow = new PotionCharmArrowEntity(RegCustomEntity.POTION_CHARM_ARROW, world);
            arrow.setOwner(player);
            arrow.setCharmStack(stack);
            arrow.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            arrow.initFromStack(PotionContents.createItemStack(Items.TIPPED_ARROW, potion));
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, 3, 0);
            if (!world.addFreshEntity(arrow)) return InteractionResult.FAIL;
            player.getCooldowns().addCooldown(stack, 20);
            if (!player.isCreative()) stack.shrink(1);
        }
        return world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }
}
