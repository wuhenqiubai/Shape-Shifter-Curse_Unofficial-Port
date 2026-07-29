package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.status_effects.CTPUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(AreaEffectCloud.class)
public class AreaEffectCloudEntityMixin implements CTPUtils.CTPFormIDHolder {
    @Unique
    private Identifier ctpFormID = null;

    @Final
    @Shadow
    private Map<Entity, Integer> victims;

    @Override
    public Identifier getCTPFormID() {
        return this.ctpFormID;
    }

    @Override
    public void setCTPFormID(Identifier formID) {
        this.ctpFormID = formID;
    }

    @Inject(method="tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        if (((AreaEffectCloud)(Object)this).tickCount % 5 == 0) {
            if (this.ctpFormID != null) {
                for (Map.Entry<Entity, Integer> entry : this.victims.entrySet()) {
                    if (entry.getKey() instanceof Player player) {
                        CTPUtils.setTransformativePotionForm(player, this.ctpFormID);
                    }
                }
            }
        }
    }
}