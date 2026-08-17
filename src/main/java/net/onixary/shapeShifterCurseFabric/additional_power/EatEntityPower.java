package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public class EatEntityPower extends Power {
    public boolean mustEmptyHand = true;
    public HashMap<ResourceLocation, FoodProperties> entityFoodMap;

    public EatEntityPower(PowerType<?> type, LivingEntity entity, boolean mustEmptyHand, HashMap<ResourceLocation, FoodProperties> entityFoodMap) {
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
            for (FoodProperties.PossibleEffect possibleEffect : foodComponent.effects()) {
                if (!world.isClientSide && world.random.nextFloat() < possibleEffect.probability()) {
                    player.addEffect(possibleEffect.effect());
                }
            }
            world.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
            targetEntity.discard();
            return true;
        }
        return false;
    }

    public record FoodPair(ResourceLocation entity, FoodProperties food) {}

    public static final SerializableDataType<FoodPair> ENTITY_FOOD_PAIR = SerializableDataType.compound(
            FoodPair.class,
            new SerializableData()
                    .add("entity", SerializableDataTypes.IDENTIFIER)
                    .add("food", SerializableDataTypes.FOOD_COMPONENT),
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
                    HashMap<ResourceLocation, FoodProperties> entityFoodMap = new HashMap<>();
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
