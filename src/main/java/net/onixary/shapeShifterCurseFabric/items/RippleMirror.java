package net.onixary.shapeShifterCurseFabric.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsS2CServer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RippleMirror extends Item {
    public RippleMirror(Properties settings) {
        super(settings);
    }

    @Override
    public @NotNull UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 36;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        user.startUsingItem(hand);
        return InteractionResultHolder.success(user.getItemInHand(hand));
    }

    @Override
    public @NotNull ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        // 只有选中后发送变形包时才减少物品数量
        if (!world.isClientSide) {
            ModPacketsS2CServer.sendOpenSelectSubFormMenu((ServerPlayer) user);
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.translatable("item.shape-shifter-curse.ripple_mirror.tooltip").withStyle(ChatFormatting.YELLOW));
    }
}