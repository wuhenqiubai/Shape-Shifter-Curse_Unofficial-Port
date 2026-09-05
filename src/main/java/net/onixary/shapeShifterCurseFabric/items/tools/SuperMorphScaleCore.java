package net.onixary.shapeShifterCurseFabric.items.tools;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.List;

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
        // 持久耐久工具：每次升级固定耗 damagePerItem(64) 耐久（core 耐久 4096 ≈ 可升级约 64 次）。
        // 不再按 base 的 maxStackSize 放大——护甲 maxStack=1 会算出 multiplier=64，一次即耗光 4096 耐久（核心一次报废）。
        return 1;
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
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        user.startUsingItem(hand);
        return InteractionResult.success(user.getItemInHand(hand));
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
        if (user instanceof Player player && !world.isClientSide) {
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
            // 1.21.1 Enchantments.MENDING 是 ResourceKey，getItemEnchantmentLevel 只收 Holder，需先转 Holder（直接强转 ResourceKey→Holder 会 ClassCastException）
            if (EnchantmentHelper.getItemEnchantmentLevel(world.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.MENDING), stack) > 0) {
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.translatable("item.shape-shifter-curse.super_morphscale_core.tooltip", getMaxUseCount(stack, 1)).withStyle(ChatFormatting.DARK_PURPLE));
    }
}