package net.onixary.shapeShifterCurseFabric.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.client.ShapeShifterCurseFabricClient;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;

import java.util.List;

public class BookOfShapeShifter extends Item {
    public BookOfShapeShifter(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        IForm currentForm = FormUtils.getPlayerForm(player);
        if (level.isClientSide()) {
            // 客户端逻辑：仅处理打开界面
            if (currentForm.equals(RegPlayerForms.ORIGINAL_BEFORE_ENABLE))
                ShapeShifterCurseFabricClient.openStartBookScreen(player);
            else ShapeShifterCurseFabricClient.openBookScreen(player);
        } else {
            // 服务端逻辑：触发成就
            if (!currentForm.equals(RegPlayerForms.ORIGINAL_BEFORE_ENABLE)) {
                if (player instanceof ServerPlayer serverPlayer) {
                    ShapeShifterCurseFabric.ON_OPEN_BOOK_OF_SHAPE_SHIFTER.trigger(serverPlayer);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.translatable("item.shape-shifter-curse.book_of_shape_shifter.tooltip").withStyle(ChatFormatting.GRAY));
    }

}