package net.onixary.shapeShifterCurseFabric.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsS2CServer;

import java.util.List;

public class SelectFormItem extends Item {
    public SelectFormItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            ModPacketsS2CServer.OpenFormSelectMenu((ServerPlayer) player, player);
        }
        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        if (entity instanceof Player player) {
            if (!user.level().isClientSide()) {
                ModPacketsS2CServer.OpenFormSelectMenu((ServerPlayer) user, player);
            }
            return InteractionResult.SUCCESS;
        }
        return super.interactLivingEntity(stack, user, entity, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.translatable("item.shape-shifter-curse.select_form_item.tooltip").withStyle(ChatFormatting.YELLOW));
    }
}