package net.onixary.shapeShifterCurseFabric.mixin.test;

import net.fabricmc.fabric.impl.networking.AbstractChanneledNetworkAddon;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.onixary.shapeShifterCurseFabric.util.test.NetWorkTest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(AbstractChanneledNetworkAddon.class)
public class AbstractChanneledNetworkAddonMixin {
    @Inject(method = "handle", at = @At("HEAD"))
    public void handle(CustomPacketPayload payload, CallbackInfoReturnable<Boolean> cir) {
        NetWorkTest.packetCounter.computeIfAbsent(ResourceLocation.parse(payload.toString()), k -> new AtomicInteger(0)).incrementAndGet();
    }
}
