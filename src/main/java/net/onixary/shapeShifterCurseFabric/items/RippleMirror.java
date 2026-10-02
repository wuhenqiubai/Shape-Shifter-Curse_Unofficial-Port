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
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsS2CServer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class RippleMirror extends Item {
    public RippleMirror(Properties settings) {
        super(settings);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 36;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        user.startUsingItem(hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        // 只有选中后发送变形包时才减少物品数量
        if (!world.isClientSide()) {
            ModPacketsS2CServer.sendOpenSelectSubFormMenu((ServerPlayer) user);
        }
        return stack;
    }

    // 1.21.11：appendHoverText 收 (ItemStack, Item.TooltipContext, TooltipDisplay, Consumer<Component>, TooltipFlag)
    // —— 原来的 List<Component> tooltip 改成 Consumer<Component>，并多了一个 TooltipDisplay 参数。
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag type) {
        tooltip.accept(Component.translatable("item.shape-shifter-curse.ripple_mirror.tooltip").withStyle(ChatFormatting.YELLOW));
    }
}