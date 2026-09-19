package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.apoli.power.PreventItemUsePower;
import io.github.apace100.apoli.power.TooltipPower;
import io.github.apace100.apoli.util.ApoliConfigClient;
import io.github.apace100.apoli.util.StackPowerUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Environment(EnvType.CLIENT)
@Mixin(ItemStack.class)
public abstract class ItemStackMixinClient implements DataComponentHolder {

    @Shadow public abstract UseAnim getUseAnimation();

    @Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0, shift = At.Shift.AFTER))
    private void addUnusableTooltip(Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir, @Local List<Component> list) {
        if(player != null) {
            ApoliConfigClient.Tooltips config = ((ApoliConfigClient) Apoli.config).tooltips;
            if(!config.showUsabilityHints) {
                return;
            }
            List<PreventItemUsePower> powers = PowerHolderComponent.getPowers(player, PreventItemUsePower.class).stream().filter(p -> p.doesPrevent((ItemStack)(Object)this)).toList();
            int powerCountWithHidden = powers.size();
            powers = powers.stream().filter(p -> !p.getType().isHidden()).toList();
            if(powerCountWithHidden == 0) {
                return;
            }
            String translationKeyBase = "tooltip.apoli.unusable." + getUseAnimation().name().toLowerCase(Locale.ROOT);
            ChatFormatting textColor = ChatFormatting.GRAY;
            ChatFormatting powerColor = ChatFormatting.RED;
            if(config.compactUsabilityHints || powers.size() == 0) {
                if(powers.size() == 1) {
                    PreventItemUsePower power = powers.get(0);
                    MutableComponent preventText = Component.translatable(translationKeyBase + ".single",
                            power.getType().getName().withStyle(powerColor)).withStyle(textColor);
                    list.add(preventText);
                } else {
                    list.add(
                            Component.translatable(translationKeyBase + ".multiple",
                                            Component.literal((powers.size() == 0 ? powerCountWithHidden : powers.size()) + "").withStyle(powerColor))
                                    .withStyle(textColor));
                }
            } else {
                MutableComponent powerNameList = powers.get(0).getType().getName().withStyle(powerColor);
                for(int i = 1; i < powers.size(); i++) {
                    powerNameList = powerNameList.append(Component.literal(", ").withStyle(textColor));
                    powerNameList = powerNameList.append(powers.get(i).getType().getName().withStyle(powerColor));
                }
                MutableComponent preventText = Component.translatable(translationKeyBase + ".single",
                        powerNameList).withStyle(textColor);
                list.add(preventText);
            }
        }
    }

    @Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/DefaultedRegistry;getKey(Ljava/lang/Object;)Lnet/minecraft/resources/ResourceLocation;", shift = At.Shift.AFTER))
    private void addEquipmentPowerTooltips(Item.TooltipContext tooltipContext, Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir, @Local List<Component> list) {
        for(EquipmentSlot slot : EquipmentSlot.values()) {
            List<StackPowerUtil.StackPower> powers = StackPowerUtil.getPowers((ItemStack)(Object)this, slot)
                    .stream()
                    .filter(sp -> !sp.isHidden)
                    .toList();
            if(powers.size() > 0) {
                list.add(Component.empty());
                list.add((Component.translatable("item.modifiers." + slot.getName())).withStyle(ChatFormatting.GRAY));
                powers.forEach(sp -> {

                    if(PowerTypeRegistry.contains(sp.powerId)) {
                        PowerType<?> powerType = PowerTypeRegistry.get(sp.powerId);
                        list.add(
                                Component.literal(" ")
                                        .append(powerType.getName())
                                        .withStyle(sp.isNegative ? ChatFormatting.RED : ChatFormatting.BLUE));
                        if(tooltipFlag.isAdvanced()) {
                            list.add(
                                    Component.literal("  ")
                                            .append(powerType.getDescription())
                                            .withStyle(ChatFormatting.GRAY));
                        }
                    }
                });
            }
        }
        PowerHolderComponent.getPowers(player, TooltipPower.class)
                .stream().filter(t -> t.doesApply((ItemStack) (Object)this))
                .sorted(Comparator.comparing(TooltipPower::getOrder))
                .forEachOrdered(t -> {
                    var components = new ArrayList<Component>();
                    t.addToTooltip(components);
                    for (Component component : components) {
                        list.add(component);
                    }
                });
    }
}
