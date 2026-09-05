package net.onixary.shapeShifterCurseFabric.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.player_form.utils.TransformRelatedItems;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
    public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        if (user.canEat(true)) {
            user.startUsingItem(hand);
            return InteractionResultHolder.consume(user.getItemInHand(hand));
        }
        return InteractionResultHolder.fail(user.getItemInHand(hand));
    }

    @Override
    public @NotNull ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        // 1.21.1 修复：之前效果靠 ItemStackMixin 注入，但该 mixin 现已只处理金苹果/牛奶、不处理 powerful_catalyst，
        // 导致 OnUsePowerfulCatalyst 从未被调用。对照上游（1.20.1 的 finishUsing 直接调用）改为自身直接调用。
        if (user instanceof Player player) {
            TransformRelatedItems.OnUsePowerfulCatalyst(player, stack);
        }
        super.finishUsingItem(stack, world, user);
        if (user instanceof Player playerEntity) {
            if (playerEntity.getAbilities().instabuild) {
                return stack;
            }
        }
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
        tooltip.add(Component.translatable("item.shape-shifter-curse.powerful_catalyst.tooltip").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}