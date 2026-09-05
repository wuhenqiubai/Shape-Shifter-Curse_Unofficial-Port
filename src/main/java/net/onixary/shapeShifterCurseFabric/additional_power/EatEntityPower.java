package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class EatEntityPower extends Power {
    public boolean mustEmptyHand = true;
    public HashMap<Identifier, FoodProperties> entityFoodMap;

    public EatEntityPower(PowerType<?> type, LivingEntity entity, boolean mustEmptyHand, HashMap<Identifier, FoodProperties> entityFoodMap) {
        super(type, entity);
        this.mustEmptyHand = mustEmptyHand;
        this.entityFoodMap = entityFoodMap;
    }

    public boolean onUseEntity(Entity targetEntity, InteractionHand playerHand, EntityHitResult hitResult) {
        if (targetEntity == null || !targetEntity.isAlive() || !(entity instanceof Player player) || !this.isActive()) {
            return false;
        }
        if (this.mustEmptyHand && !player.getItemInHand(playerHand).isEmpty()) {
            return false;
        }
        @Nullable FoodProperties foodComponent = entityFoodMap.get(EntityType.getKey(targetEntity.getType()));
        Level world = player.level();
        if (foodComponent != null && player.canEat(foodComponent.canAlwaysEat())) {
            player.getFoodData().eat(foodComponent.nutrition(), foodComponent.saturation());
            // 1.21.11 降级：FoodProperties record 不再含 effects 列表（移到 ConsumableListener，无公开 API），无法应用食物附带的药水效果
            world.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
            targetEntity.discard();
            return true;
        }
        return false;
    }

    public record FoodPair(Identifier entity, FoodProperties food) {}

    // 1.21.1: FoodProperties 是 6 字段 record（nutrition/saturation/canAlwaysEat/eatSeconds/usingConvertsTo/effects）。
    // Calio-Legacy 的 FOOD_COMPONENT 返回 DataComponentPatch 且 write 不完整（meat 字段报错导致 power_list 编码崩），
    // 改用自定义类型直接解析 FoodProperties，read/write 完整。
    public static final SerializableDataType<FoodProperties.PossibleEffect> POSSIBLE_EFFECT_TYPE = SerializableDataType.compound(
            FoodProperties.PossibleEffect.class,
            new SerializableData()
                    .add("effect", SerializableDataTypes.STATUS_EFFECT_INSTANCE)
                    .add("probability", SerializableDataTypes.FLOAT),
            (data) -> createPossibleEffect(data.get("effect"), data.getFloat("probability")),
            (data, possibleEffect) -> {
                SerializableData.Instance inst = data.new Instance();
                inst.set("effect", possibleEffect.effect());
                inst.set("probability", possibleEffect.probability());
                return inst;
            }
    );

    // NeoForge/Connector 兼容：NeoForge 重编译把 FoodProperties$PossibleEffect 的 canonical 构造器改成
    // (Supplier<MobEffectInstance>, float)（effect 延迟实例化），并保留 private (MobEffectInstance, float) 兼容构造器。
    // 直接 new 在 NeoForge 下撞 private 构造器 IllegalAccessError。改用反射选 public canonical 构造器
    // （Fabric=(MobEffectInstance,float)，NeoForge=(Supplier,float)），跨环境安全。
    private static FoodProperties.PossibleEffect createPossibleEffect(MobEffectInstance effect, float probability) {
        try {
            Constructor<FoodProperties.PossibleEffect> c = FoodProperties.PossibleEffect.class.getConstructor(Supplier.class, float.class);
            return c.newInstance((Supplier<MobEffectInstance>) () -> effect, probability);
        } catch (NoSuchMethodException ignored) {
            // Fabric：canonical 构造器是 (MobEffectInstance, float)
            try {
                Constructor<FoodProperties.PossibleEffect> c = FoodProperties.PossibleEffect.class.getConstructor(MobEffectInstance.class, float.class);
                return c.newInstance(effect, probability);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create FoodProperties.PossibleEffect", e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create FoodProperties.PossibleEffect", e);
        }
    }

    public static final SerializableDataType<FoodProperties> FOOD_TYPE = SerializableDataType.compound(
            FoodProperties.class,
            new SerializableData()
                    .add("nutrition", SerializableDataTypes.INT)
                    .add("saturation", SerializableDataTypes.FLOAT)
                    .add("can_always_eat", SerializableDataTypes.BOOLEAN, false)
                    .add("effects", SerializableDataType.list(POSSIBLE_EFFECT_TYPE), List.of()),
            (data) -> new FoodProperties(
                    data.getInt("nutrition"),
                    data.getFloat("saturation"),
                    data.getBoolean("can_always_eat"),
                    1.6F,
                    Optional.empty(),
                    data.get("effects")),
            (data, food) -> {
                SerializableData.Instance inst = data.new Instance();
                inst.set("nutrition", food.nutrition());
                inst.set("saturation", food.saturation());
                inst.set("can_always_eat", food.canAlwaysEat());
                inst.set("effects", food.effects());
                return inst;
            }
    );

    public static final SerializableDataType<FoodPair> ENTITY_FOOD_PAIR = SerializableDataType.compound(
            FoodPair.class,
            new SerializableData()
                    .add("entity", SerializableDataTypes.IDENTIFIER)
                    .add("food", FOOD_TYPE),
            (serializableData) -> new FoodPair(serializableData.get("entity"), serializableData.get("food")),
            (serializableData, pair) -> {
                SerializableData.Instance inst = serializableData.new Instance();
                inst.set("entity", pair.entity());
                inst.set("food", pair.food());
                return inst;
            }
    );

    public static final SerializableDataType<List<FoodPair>> ENTITY_FOOD_PAIR_LIST = SerializableDataType.list(ENTITY_FOOD_PAIR);

    static {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (player != null) {
                for (EatEntityPower power : PowerHolderComponent.getPowers(player, EatEntityPower.class)) {
                    if (power.onUseEntity(entity, hand, hitResult)) {
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            return InteractionResult.PASS;
        });
    }

    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(
                ShapeShifterCurseFabric.identifier("eat_entity"),
                new SerializableData()
                        .add("must_empty_hand", SerializableDataTypes.BOOLEAN, true)
                        .add("food_map", ENTITY_FOOD_PAIR_LIST, null),
                data -> (powerType, livingEntity) -> {
                    @Nullable List<FoodPair> foodMap = data.get("food_map");
                    HashMap<Identifier, FoodProperties> entityFoodMap = new HashMap<>();
                    if (foodMap != null) {
                        for (FoodPair pair : foodMap) {
                            entityFoodMap.put(pair.entity(), pair.food());
                        }
                    }
                    return new EatEntityPower(powerType, livingEntity, data.get("must_empty_hand"), entityFoodMap);
                }
        ).allowCondition();
    }
}
