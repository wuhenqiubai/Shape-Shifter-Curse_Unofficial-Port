package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;

public class DisableHurtCameraPowerCheck {
    // 1.21.11：@GameTest 注解在 1.21.5 被 Mojang 移除，改用 Fabric 的同名注解
    // （net.fabricmc.fabric.api.gametest.v1.GameTest）。其 structure() 默认即空结构，
    // 等价于旧版的 FabricGameTest.EMPTY_STRUCTURE，故不再传参。
    @GameTest
    public void conditionFollowsWither(GameTestHelper context) {
        checkCondition(context, "form_anubis_wolf_disable_hurt_camera_when_withered", DisableHurtCameraPower.class);
        checkCondition(context, "form_anubis_wolf_disable_wither_hearts", DisableWitherHeartsPower.class);
        context.succeed();
    }

    private static void checkCondition(GameTestHelper context, String powerId, Class<? extends Power> powerClass) {
        // 1.21.11：EntityType.create 的单参重载已移除，需显式给 EntitySpawnReason。
        var entity = EntityType.COW.create(context.getLevel(), EntitySpawnReason.STRUCTURE);
        var type = PowerTypeRegistry.get(Identifier.fromNamespaceAndPath("shape-shifter-curse", powerId));
        var source = Identifier.fromNamespaceAndPath("test", "hurt_camera");
        var holder = PowerHolderComponent.KEY.get(entity);
        holder.addPower(type, source);
        context.assertTrue(!PowerHolderComponent.hasPower(entity, powerClass), "Inactive without Wither: " + powerId);
        entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 200));
        context.assertTrue(PowerHolderComponent.hasPower(entity, powerClass), "Active with Wither: " + powerId);
        context.assertTrue(entity.hasEffect(MobEffects.WITHER), "Visual power must not remove Wither");
        entity.removeEffect(MobEffects.WITHER);
        context.assertTrue(!PowerHolderComponent.hasPower(entity, powerClass), "Inactive when Wither ends: " + powerId);
        entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 200));
        holder.removePower(type, source);
        context.assertTrue(!PowerHolderComponent.hasPower(entity, powerClass), "Wither alone does not suppress visuals: " + powerId);
    }
}
