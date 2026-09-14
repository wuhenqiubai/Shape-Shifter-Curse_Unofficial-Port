package net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.axolotl;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.AxolotlAttackablesSensor;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import org.jetbrains.annotations.NotNull;

public class TAxolotlEntitySensor extends AxolotlAttackablesSensor {
    public static final SensorType<TAxolotlEntitySensor> T_AXOLOTL_ENTITY_SENSOR;

    static {
        T_AXOLOTL_ENTITY_SENSOR = Registry.register(BuiltInRegistries.SENSOR_TYPE, ShapeShifterCurseFabric.identifier("t_axolotl_attackables"), new SensorType<>(TAxolotlEntitySensor::new));
    }

    public static void init() {}

    @Override
    protected boolean isMatchingEntity(ServerLevel serverLevel, LivingEntity entity, LivingEntity target) {
        return this.isInRange(entity, target) && target.isInWater() && (this.isAlwaysHostileTo(target) || this.canHunt(entity, target)) && Sensor.isEntityAttackable(serverLevel, entity, target);
    }

    private boolean isInRange(LivingEntity axolotl, LivingEntity target) {
        return target.distanceToSqr(axolotl) <= (double)64.0F;  // 8.0f x 8.0f
    }

    private boolean canHunt(LivingEntity axolotl, LivingEntity target) {
        return !axolotl.getBrain().hasMemoryValue(MemoryModuleType.HAS_HUNTING_COOLDOWN) && target.is(EntityTypeTags.AXOLOTL_HUNT_TARGETS);
    }

    private boolean isAlwaysHostileTo(LivingEntity axolotl) {
        return axolotl.is(EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES) || (axolotl instanceof Player player && RegPlayerForms.ORIGINAL_SHIFTER.isPlayerForm(player));
    }

    // 26.1: NearestVisibleLivingEntitySensor#getMemory() 改名为 getMemoryToSet()（isMatchingEntity 签名未变）。
    // 顺带注意：该基类 26.1 的 requires() 变成 Set.of(getMemoryToSet(), NEAREST_VISIBLE_LIVING_ENTITIES)，
    // 比 1.21.11 多声明了一个依赖记忆，属原版行为变化，无需在子类处理。
    @Override
    protected @NotNull MemoryModuleType<LivingEntity> getMemoryToSet() {
        return MemoryModuleType.NEAREST_ATTACKABLE;
    }
}
