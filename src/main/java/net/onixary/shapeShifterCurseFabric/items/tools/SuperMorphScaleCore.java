package net.onixary.shapeShifterCurseFabric.items.tools;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class SuperMorphScaleCore extends Item {
    public static final int damagePerItem = 64;
    public static final float mendingMultiplier = 2.0f;
    public static float quickChargeCostMultiplier = 0.75f;
    public static float quickChargeCostMultiplierNoMending = 0.20f;

    public SuperMorphScaleCore(Properties settings) {
        super(settings);
    }

    public static int getMaxUseCount(ItemStack stack, int multiplier) {
        int damage = (stack.getMaxDamage() - stack.getDamageValue());
        int damagePerCount = damagePerItem * multiplier;
        return damage / damagePerCount;
    }

    public static int getUpgradeDamageMultiplier(ItemStack stack) {
        int upgradeItemStackCount = stack.getMaxStackSize();
        if (upgradeItemStackCount == 0) {
            return 1;
        }
        return 64 / upgradeItemStackCount;
    }

    public static void damageItemAfterUpgrade(ItemStack stack, int multiplier) {
        int damagePerCount = damagePerItem * multiplier;
        int damage = stack.getDamageValue();
        int targetDamage = damage + damagePerCount;
	    stack.setDamageValue(Math.min(targetDamage, stack.getMaxDamage()));
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 24;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.SUCCESS;
    }

    // 我最早的Mod中的代码(没发布) 最后一次更新还是2年前了
    public static int getTotalExperience(int level, int xp) {
        int totalExp;
        if (level <= 16) {
            totalExp = level * level + 6 * level;
        }
        else if (level <= 31) {
            totalExp = (int) (2.5 * level * level - 40.5 * level + 360);
        }
        else {
            totalExp = (int) (4.5 * level * level - 162.5 * level + 2220);
        }
        int sum = totalExp + xp;
        return sum < 0 ? totalExp : sum;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (user instanceof Player player && !world.isClientSide()) {
            int damage = stack.getDamageValue();
            int need_repair = 0;
            if (user.isShiftKeyDown()) {
                need_repair = stack.getMaxDamage();
            } else {
                need_repair = damagePerItem;
            }
            int max_repair = damage;
            need_repair = Math.min(need_repair, max_repair);
            float exp_multiplier = mendingMultiplier;
            if (EnchantmentHelper.getItemEnchantmentLevel((Holder<Enchantment>) Enchantments.MENDING, stack) > 0) {
                exp_multiplier *= quickChargeCostMultiplier;
            } else {
                exp_multiplier *= quickChargeCostMultiplierNoMending;
            }
            int player_exp = getTotalExperience(player.experienceLevel, Mth.floor(player.experienceProgress * (float) player.getXpNeededForNextLevel()));
            max_repair = Mth.floor(player_exp * exp_multiplier);
            need_repair = Math.min(need_repair, max_repair);
            int exp_cost = Mth.ceil(need_repair / exp_multiplier);
            if (need_repair > 0) {
                int finalDamage = damage - need_repair;
                if (finalDamage < 0) {
                    finalDamage = 0;
                }
                stack.setDamageValue(finalDamage);
                player.giveExperiencePoints(-exp_cost);
                player.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);
            }
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag type) {
        consumer.accept(Component.translatable("item.shape-shifter-curse.super_morphscale_core.tooltip", getMaxUseCount(stack, 1)).withStyle(ChatFormatting.DARK_PURPLE));
    }
}