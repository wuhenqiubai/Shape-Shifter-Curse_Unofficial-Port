package net.onixary.shapeShifterCurseFabric.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class PowerfulInhibitor extends Item {
    public PowerfulInhibitor(Properties settings) {
        super(settings.stacksTo(16)
                .food(
                new FoodProperties.Builder()
                        .nutrition(2)
                        .saturationModifier(0.3f)
                        .alwaysEdible()
                        .build()
        ));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.canEat(true)) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        // 实际效果在ItemStackMixin的注入中进行处理
        super.finishUsingItem(stack, world, user);
        if (user instanceof Player playerEntity) {
            if (playerEntity.getAbilities().instabuild) {
                return stack;
            }
        }

        if (stack.isEmpty()) {
            return new ItemStack(Items.BOWL);
        } else {
            if (user instanceof Player playerEntity) {
                if (!playerEntity.getInventory().add(new ItemStack(Items.BOWL))) {
                    playerEntity.drop(new ItemStack(Items.BOWL), false);
                }
            }
            return stack;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.translatable("item.shape-shifter-curse.powerful_inhibitor.tooltip").withStyle(ChatFormatting.YELLOW));
    }
}