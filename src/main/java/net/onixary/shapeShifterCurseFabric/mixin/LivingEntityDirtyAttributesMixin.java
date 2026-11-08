package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDirtyAttributesMixin {

    @Shadow
    public abstract AttributeMap getAttributes();

    @Shadow
    protected abstract void onAttributeUpdated(Holder<Attribute> attribute);

    @Inject(method = "refreshDirtyAttributes", at = @At("HEAD"), cancellable = true)
    private void ssc$refreshDirtyAttributesFromSnapshot(CallbackInfo ci) {
        AttributeMap attributeMap = this.getAttributes();
        Set<AttributeInstance> pending = attributeMap.getAttributesToUpdate();
        AttributeInstance[] snapshot = pending.toArray(new AttributeInstance[0]);
        if (snapshot.length == 0) {
            ci.cancel();
            return;
        }
        pending.clear();
        for (AttributeInstance instance : snapshot) {
            this.onAttributeUpdated(instance.getAttribute());
        }
        ci.cancel();
    }
}
