package net.onixary.shapeShifterCurseFabric.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
        if (!world.isClientSide && !user.isUsingItem()) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 0.1f, 2.0f);
        }
        user.startUsingItem(hand);
        return InteractionResultHolder.success(user.getItemInHand(hand));
    }

    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!world.isClientSide) return;
        boolean finishing = remainingUseTicks == 1;
        if (!finishing && remainingUseTicks % 5 != 0) return;

        float progress = (getUseDuration(stack, user) - remainingUseTicks) / (float) getUseDuration(stack, user);
        int count = finishing ? 32 : 4 + (int) (progress * 20);
        var random = user.getRandom();
        double centerY = user.getY() + user.getBbHeight() * 0.5;
        for (int i = 0; i < count; i++) {
            // Enchantment particles converge on the player as the charge builds.
            world.addParticle(ParticleTypes.ENCHANT, user.getX(), centerY, user.getZ(),
                    (random.nextDouble() - 0.5) * 3,
                    random.nextDouble() * 2,
                    (random.nextDouble() - 0.5) * 3);
            if (finishing || (progress >= 0.25f && i % 3 == 0)) {
                world.addParticle(ParticleTypes.END_ROD,
                        user.getX() + (random.nextDouble() - 0.5) * 1.5,
                        user.getY() + random.nextDouble() * user.getBbHeight(),
                        user.getZ() + (random.nextDouble() - 0.5) * 1.5,
                        (random.nextDouble() - 0.5) * 0.08,
                        0.03 + random.nextDouble() * 0.07,
                        (random.nextDouble() - 0.5) * 0.08);
            }
        }
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
