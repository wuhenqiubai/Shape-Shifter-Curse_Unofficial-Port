package net.onixary.shapeShifterCurseFabric.integration.origins.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;
import net.onixary.shapeShifterCurseFabric.integration.origins.component.OriginComponent;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.Origin;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.OriginRegistry;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.OriginUpgrade;
import net.onixary.shapeShifterCurseFabric.integration.origins.registry.ModComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerAdvancements.class)
public class OriginUpgradeMixin {

    @Shadow
    private ServerPlayer player;

    @Inject(method = "award", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerAdvancements;unregisterListeners(Lnet/minecraft/advancements/AdvancementHolder;)V"))
    private void checkOriginUpgrade(AdvancementHolder advancement, String criterionName, CallbackInfoReturnable<Boolean> info, @Local AdvancementProgress advancementProgress) {
        if(advancementProgress.isDone()) {
            Origin.get(player).forEach((layer, o) -> {
                Optional<OriginUpgrade> upgrade = o.getUpgrade(advancement);
                if(upgrade.isPresent()) {
                    try {
                        Origin upgradeTo = OriginRegistry.get(upgrade.get().getUpgradeToOrigin());
                        if(upgradeTo != null) {
                            OriginComponent component = ModComponents.ORIGIN.get(player);
                            component.setOrigin(layer, upgradeTo);
                            component.sync();
                            String announcement = upgrade.get().getAnnouncement();
	                        if (announcement != null && !announcement.isEmpty()) {
		                        player.displayClientMessage(Component.translatable(announcement).withStyle(ChatFormatting.GOLD), false);
	                        }
                        }
                    } catch(IllegalArgumentException e) {
	                    Origins.LOGGER.error("Could not perform Origins upgrade from {} to {}, as the upgrade origin did not exist!", o.getIdentifier().toString(), upgrade.get().getUpgradeToOrigin().toString());
                    }
                }
            });
        }
    }
}
