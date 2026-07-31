package net.onixary.shapeShifterCurseFabric.player_form.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.ITransformReason;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.status_effects.attachment.EffectManager;
import org.jetbrains.annotations.Nullable;

public class TransformRelatedItems {
    public static void OnUseCure(Player player, @Nullable ItemStack stack) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        IForm nowForm = FormUtils.getPlayerForm(player);
        int Tier = nowForm.getFormTier();
        if (EffectManager.hasTransformativeEffect(player)) {
            serverPlayer.sendSystemMessage(Component.translatable("info.shape-shifter-curse.transformative_effect_cure").withStyle(ChatFormatting.YELLOW));
            EffectManager.clearTransformativeEffect(player);
        }
        else if (RegPlayerForms.ORIGINAL_BEFORE_ENABLE.isPlayerForm(player)) { }
        else if (RegPlayerForms.ORIGINAL_SHIFTER.isPlayerForm(player)) {
            serverPlayer.sendSystemMessage(Component.translatable("info.shape-shifter-curse.origin_form_used_cure").withStyle(ChatFormatting.YELLOW));
        }
        else if (Tier == 1) {
            serverPlayer.sendSystemMessage(Component.translatable("info.shape-shifter-curse.transformed_by_cure_0").withStyle(ChatFormatting.YELLOW));
            ShapeShifterCurseFabric.ON_TRANSFORM_BY_CURE.trigger(serverPlayer);
        }
        else if (FormUtils.InhibitorImmune.hasFlag(nowForm)) {
            serverPlayer.sendSystemMessage(Component.translatable("info.shape-shifter-curse.permanent_form_used_cure").withStyle(ChatFormatting.YELLOW));
        }
        else if (FormUtils.InhibitorResist.hasFlag(nowForm)) {
            serverPlayer.sendSystemMessage(Component.translatable("info.shape-shifter-curse.max_form_used_cure").withStyle(ChatFormatting.YELLOW));
        }
        else {
            serverPlayer.sendSystemMessage(Component.translatable("info.shape-shifter-curse.transformed_by_cure").withStyle(ChatFormatting.YELLOW));
            ShapeShifterCurseFabric.ON_TRANSFORM_BY_CURE.trigger(serverPlayer);
        }
        IForm nextForm = nowForm._getPrevForm(player, ITransformReason.ItemReasonBuilder.apply(stack));
        if (nextForm != nowForm) {
            PlayerFormComponent.COMPONENT.get(player).lastTransformByCure = true;
            PlayerFormComponent.COMPONENT.sync(player);
            TransformManager.startTransform(player, nextForm, null);
        }
    }

    public static void OnUseCureFinal(Player player, @Nullable ItemStack stack) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        IForm nowForm = FormUtils.getPlayerForm(player);
        if (EffectManager.hasTransformativeEffect(player)) {
            serverPlayer.sendSystemMessage(Component.translatable("info.shape-shifter-curse.transformative_effect_cure").withStyle(ChatFormatting.YELLOW));
            EffectManager.clearTransformativeEffect(player);
        }
        else if (RegPlayerForms.ORIGINAL_BEFORE_ENABLE.isPlayerForm(player)) { }
        else if (RegPlayerForms.ORIGINAL_SHIFTER.isPlayerForm(player)) {
            serverPlayer.sendSystemMessage(Component.translatable("info.shape-shifter-curse.origin_form_used_cure_final").withStyle(ChatFormatting.YELLOW));
        }
        else if (FormUtils.InhibitorImmune.hasFlag(nowForm)) {
            serverPlayer.sendSystemMessage(Component.translatable("info.shape-shifter-curse.permanent_form_used_cure_final").withStyle(ChatFormatting.YELLOW));
        }
        else if (FormUtils.InhibitorResist.hasFlag(nowForm)) {
            serverPlayer.sendSystemMessage(Component.translatable("info.shape-shifter-curse.max_form_used_cure_final").withStyle(ChatFormatting.YELLOW));
            ShapeShifterCurseFabric.ON_TRANSFORM_BY_CURE_FINAL.trigger(serverPlayer);
            ShapeShifterCurseFabric.ON_TRANSFORM_BY_CURE.trigger(serverPlayer);
        }
        else {
            serverPlayer.sendSystemMessage(Component.translatable("info.shape-shifter-curse.transformed_by_cure_final").withStyle(ChatFormatting.YELLOW));
            ShapeShifterCurseFabric.ON_TRANSFORM_BY_CURE.trigger(serverPlayer);
        }
        IForm nextForm = nowForm._getPrevForm(player, ITransformReason.ItemReasonBuilder.apply(stack));
        if (nextForm != nowForm) {
            PlayerFormComponent.COMPONENT.get(player).lastTransformByCure = true;
            PlayerFormComponent.COMPONENT.sync(player);
            TransformManager.startTransform(player, nextForm, null);
        }
    }

    public static void OnUseCreativeCure(Player player, @Nullable ItemStack stack) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        PlayerFormComponent.COMPONENT.get(player).lastTransformByCure = true;
        PlayerFormComponent.COMPONENT.sync(player);
        if (EffectManager.hasTransformativeEffect(player)) {
            EffectManager.clearTransformativeEffect(player);
        }
        if(!RegPlayerForms.ORIGINAL_SHIFTER.isPlayerForm(player) && !RegPlayerForms.ORIGINAL_BEFORE_ENABLE.isPlayerForm(player)){
            TransformManager.forceTransform(player, RegPlayerForms.ORIGINAL_SHIFTER, false);
        }
    }

    public static void OnUseCatalyst(Player player, @Nullable ItemStack stack) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        IForm nowForm = FormUtils.getPlayerForm(player);
        if (RegPlayerForms.ORIGINAL_SHIFTER.isPlayerForm(player) || EffectManager.playerCanHaveTransformativeEffect(player)) {
            if (EffectManager.hasTransformativeEffect(player)) {
                EffectManager.ActiveTransformativeEffect(serverPlayer);
                serverPlayer.sendSystemMessage(Component.translatable("info.shape-shifter-curse.origin_form_used_catalyst_attached").withStyle(ChatFormatting.YELLOW));
                ShapeShifterCurseFabric.ON_TRANSFORM_BY_CATALYST.trigger(serverPlayer);
            }
            else{
                serverPlayer.sendSystemMessage(Component.translatable("info.shape-shifter-curse.origin_form_used_catalyst").withStyle(ChatFormatting.YELLOW));
            }
        }
        else if (RegPlayerForms.ORIGINAL_BEFORE_ENABLE.isPlayerForm(player)) {

        }
        else if (FormUtils.SpecialForm.hasFlag(nowForm)) {
            // 为了这句文本 专门加了一个flag
            serverPlayer.sendSystemMessage(Component.translatable("info.shape-shifter-curse.sp_form_used_catalyst").withStyle(ChatFormatting.YELLOW));
        }
        else if (FormUtils.CatalystImmune.hasFlag(nowForm)) {
            serverPlayer.sendSystemMessage(Component.translatable("info.shape-shifter-curse.use_catalyst_when_ignore").withStyle(ChatFormatting.DARK_PURPLE));
        }
        else if (FormUtils.CatalystResist.hasFlag(nowForm)) {
            serverPlayer.sendSystemMessage(Component.translatable("info.shape-shifter-curse.max_form_used_catalyst").withStyle(ChatFormatting.YELLOW));
        }
        else {
            serverPlayer.sendSystemMessage(Component.translatable("info.shape-shifter-curse.use_catalyst").withStyle(ChatFormatting.YELLOW));
        }
        IForm nextForm = nowForm._getNextForm(player, ITransformReason.ItemReasonBuilder.apply(stack));
        if (nextForm != nowForm) {
            TransformManager.startTransform(player, nextForm, null);
        }
    }

    public static void OnUsePowerfulCatalyst(Player player, @Nullable ItemStack stack) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        IForm nowForm = FormUtils.getPlayerForm(player);
        if (RegPlayerForms.ORIGINAL_BEFORE_ENABLE.isPlayerForm(player)) { }
        else if (FormUtils.CanTFToFinalForm.hasFlag(nowForm)) {
            IForm nextForm = nowForm._getNextForm(player, ITransformReason.ItemReasonBuilder.apply(stack));
            if (nextForm != nowForm) {
                serverPlayer.sendSystemMessage(Component.translatable("info.shape-shifter-curse.max_form_used_powerful_catalyst").withStyle(ChatFormatting.YELLOW));
                TransformManager.startTransform(player, nextForm, null);
            } else {
                serverPlayer.sendSystemMessage(Component.translatable("info.shape-shifter-curse.form_used_powerful_catalyst_failed").withStyle(ChatFormatting.YELLOW));
            }
        } else {
            serverPlayer.sendSystemMessage(Component.translatable("info.shape-shifter-curse.form_used_powerful_catalyst_failed").withStyle(ChatFormatting.YELLOW));
        }
    }
}
