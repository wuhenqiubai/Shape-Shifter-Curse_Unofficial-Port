package net.onixary.shapeShifterCurseFabric.items;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsS2CServer;
import net.onixary.shapeShifterCurseFabric.util.PatronUtils;

import java.util.List;

public class PatronFormItem extends Item {
    public PatronFormItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        if (!PatronUtils.EnablePatronFeature) {
            return super.use(world, user, hand);
        }
        if (!world.isClientSide) {
            ModPacketsS2CServer.OpenPatronFormSelectMenu(((ServerPlayer) user));
        }
        return super.use(world, user, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.nullToEmpty("This Feature Is In Development"));
    }
}
