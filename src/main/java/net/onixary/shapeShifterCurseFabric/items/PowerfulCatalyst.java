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
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.player_form.utils.TransformRelatedItems;

import java.util.function.Consumer;

public class PowerfulCatalyst extends Item {
    public PowerfulCatalyst(Properties settings) {
        super(settings
                .stacksTo(16)
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
        // 1.21.11 修复：效果改由自身直接调用（此前依赖 ItemStackMixin 注入，但该 mixin 已不处理 powerful_catalyst）
        if (user instanceof Player player) {
            TransformRelatedItems.OnUsePowerfulCatalyst(player, stack);
        }
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag type) {
        consumer.accept(Component.translatable("item.shape-shifter-curse.powerful_catalyst.tooltip").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}