package io.github.apace100.apoli.power.factory.condition;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.MovingEntity;
import io.github.apace100.apoli.access.SubmergableEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.mixin.EntityAccessor;
import io.github.apace100.apoli.power.ClimbingPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeReference;
import io.github.apace100.apoli.power.factory.condition.entity.*;
import io.github.apace100.apoli.power.factory.condition.entity.legacy.DistanceToGroundCondition;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.apoli.util.Shape;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.ladysnake.pal.PlayerAbility;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class EntityConditions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ConditionFactory<>(Apoli.identifier("constant"), new SerializableData()
            .add("value", SerializableDataTypes.BOOLEAN),
            (data, entity) -> data.getBoolean("value")));
        register(new ConditionFactory<>(Apoli.identifier("and"), new SerializableData()
            .add("conditions", ApoliDataTypes.ENTITY_CONDITIONS),
            (data, entity) -> {
                for (ConditionFactory<Entity>.Instance condition : ((List<ConditionFactory<Entity>.Instance>) data.get("conditions"))) {
                    if (!condition.test(entity))
                        return false;
                }

                return true;
            }));
        register(new ConditionFactory<>(Apoli.identifier("or"), new SerializableData()
            .add("conditions", ApoliDataTypes.ENTITY_CONDITIONS),
            (data, entity) -> {
                for (ConditionFactory<Entity>.Instance condition : ((List<ConditionFactory<Entity>.Instance>) data.get("conditions"))) {
                    if (condition.test(entity))
                        return true;
                }

                return false;
            }));
        register(BlockCollisionCondition.getFactory());
        register(new ConditionFactory<>(Apoli.identifier("brightness"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, entity) -> ((Comparison)data.get("comparison")).compare(entity.getLightLevelDependentMagicValue(), data.getFloat("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("daytime"), new SerializableData(), (data, entity) -> entity.level().getDayTime() % 24000L < 13000L));
        register(new ConditionFactory<>(Apoli.identifier("time_of_day"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT), (data, entity) ->
            ((Comparison)data.get("comparison")).compare(entity.level().getDayTime() % 24000L, data.getInt("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("fall_flying"), new SerializableData(), (data, entity) -> entity instanceof LivingEntity && ((LivingEntity) entity).isFallFlying()));
        register(new ConditionFactory<>(Apoli.identifier("exposed_to_sun"), new SerializableData(), (data, entity) -> {
            if (entity.level().isBrightOutside() && !((EntityAccessor) entity).callIsInRain()) {
                float f = entity.getLightLevelDependentMagicValue();
                BlockPos blockPos = entity.getVehicle() instanceof Boat ? (BlockPos.containing(entity.getX(), (double) Math.round(entity.getY()), entity.getZ())).above() : BlockPos.containing(entity.getX(), (double) Math.round(entity.getY()), entity.getZ());
                return f > 0.5F && entity.level().canSeeSky(blockPos);
            }
            return false;
        }));
        register(new ConditionFactory<>(Apoli.identifier("in_rain"), new SerializableData(), (data, entity) -> ((EntityAccessor) entity).callIsInRain()));
        register(new ConditionFactory<>(Apoli.identifier("invisible"), new SerializableData(), (data, entity) -> entity.isInvisible()));
        register(new ConditionFactory<>(Apoli.identifier("on_fire"), new SerializableData(), (data, entity) -> entity.isOnFire()));
        register(new ConditionFactory<>(Apoli.identifier("exposed_to_sky"), new SerializableData(), (data, entity) -> {
            BlockPos blockPos = entity.getVehicle() instanceof Boat ? (BlockPos.containing(entity.getX(), (double) Math.round(entity.getY()), entity.getZ())).above() : BlockPos.containing(entity.getX(), (double) Math.round(entity.getY()), entity.getZ());
            return entity.level().canSeeSky(blockPos);
        }));
        register(new ConditionFactory<>(Apoli.identifier("sneaking"), new SerializableData(), (data, entity) -> entity.isShiftKeyDown()));
        register(new ConditionFactory<>(Apoli.identifier("sprinting"), new SerializableData(), (data, entity) -> entity.isSprinting()));
        register(new ConditionFactory<>(Apoli.identifier("power_active"), new SerializableData().add("power", ApoliDataTypes.POWER_TYPE),
            (data, entity) -> ((PowerTypeReference<?>)data.get("power")).isActive(entity)));
        register(new ConditionFactory<>(Apoli.identifier("status_effect"), new SerializableData()
            .add("effect", SerializableDataTypes.STATUS_EFFECT)
            .add("min_amplifier", SerializableDataTypes.INT, 0)
            .add("max_amplifier", SerializableDataTypes.INT, Integer.MAX_VALUE)
            .add("min_duration", SerializableDataTypes.INT, 0)
            .add("max_duration", SerializableDataTypes.INT, Integer.MAX_VALUE),
            (data, entity) -> {
                MobEffect effect = data.get("effect");
                var effectHolder = entity.registryAccess().lookupOrThrow(Registries.MOB_EFFECT).wrapAsHolder(effect);
                if(entity instanceof LivingEntity living) {
                    if (living.hasEffect(effectHolder)) {
                        MobEffectInstance instance = living.getEffect(effectHolder);
                        return instance.getDuration() <= data.getInt("max_duration") && instance.getDuration() >= data.getInt("min_duration")
                            && instance.getAmplifier() <= data.getInt("max_amplifier") && instance.getAmplifier() >= data.getInt("min_amplifier");
                    }
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("submerged_in"), new SerializableData().add("fluid", SerializableDataTypes.FLUID_TAG),
            (data, entity) -> ((SubmergableEntity)entity).isSubmergedInLoosely(data.get("fluid"))));
        register(new ConditionFactory<>(Apoli.identifier("fluid_height"), new SerializableData()
            .add("fluid", SerializableDataTypes.FLUID_TAG)
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.DOUBLE),
            (data, entity) -> ((Comparison)data.get("comparison")).compare(((SubmergableEntity)entity).getFluidHeightLoosely(data.get("fluid")), data.getDouble("compare_to"))));
        register(PowerCondition.getFactory());
        register(new ConditionFactory<>(Apoli.identifier("food_level"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, entity) -> {
                if(entity instanceof Player) {
                    return ((Comparison)data.get("comparison")).compare(((Player)entity).getFoodData().getFoodLevel(), data.getInt("compare_to"));
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("saturation_level"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, entity) -> {
                if(entity instanceof Player) {
                    return ((Comparison) data.get("comparison")).compare(((Player)entity).getFoodData().getSaturationLevel(), data.getFloat("compare_to"));
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("on_block"), new SerializableData()
            .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null),
            (data, entity) -> entity.onGround() &&
                (!data.isPresent("block_condition") || ((ConditionFactory<BlockInWorld>.Instance)data.get("block_condition")).test(
                    new BlockInWorld(entity.level(), BlockPos.containing(entity.getX(), entity.getBoundingBox().minY - 0.5000001D, entity.getZ()), true)))));
        register(new ConditionFactory<>(Apoli.identifier("equipped_item"), new SerializableData()
            .add("equipment_slot", SerializableDataTypes.EQUIPMENT_SLOT)
            .add("item_condition", ApoliDataTypes.ITEM_CONDITION),
            (data, entity) -> entity instanceof LivingEntity && ((ConditionFactory<ItemStack>.Instance) data.get("item_condition")).test(
                ((LivingEntity) entity).getItemBySlot(data.get("equipment_slot")))));
        register(new ConditionFactory<>(Apoli.identifier("attribute"), new SerializableData()
            .add("attribute", SerializableDataTypes.ATTRIBUTE)
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.DOUBLE),
            (data, entity) -> {
                double attrValue = 0F;
                if(entity instanceof LivingEntity living) {
                    AttributeInstance attributeInstance = living.getAttribute(data.get("attribute"));
                    if(attributeInstance != null) {
                        attrValue = attributeInstance.getValue();
                    }
                }
                return ((Comparison)data.get("comparison")).compare(attrValue, data.getDouble("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("swimming"), new SerializableData(), (data, entity) -> entity.isSwimming()));
        register(ResourceCondition.getFactory());
        register(new ConditionFactory<>(Apoli.identifier("air"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, entity) -> ((Comparison)data.get("comparison")).compare(entity.getAirSupply(), data.getInt("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("in_block"), new SerializableData()
            .add("block_condition", ApoliDataTypes.BLOCK_CONDITION),
            (data, entity) ->((ConditionFactory<BlockInWorld>.Instance)data.get("block_condition")).test(
                new BlockInWorld(entity.level(), entity.blockPosition(), true))));
        register(new ConditionFactory<>(Apoli.identifier("block_in_radius"), new SerializableData()
            .add("block_condition", ApoliDataTypes.BLOCK_CONDITION)
            .add("radius", SerializableDataTypes.INT)
            .add("shape", SerializableDataType.enumValue(Shape.class), Shape.CUBE)
            .add("compare_to", SerializableDataTypes.INT, 1)
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL),
            (data, entity) -> {
                Predicate<BlockInWorld> blockCondition = data.get("block_condition");
                int stopAt = -1;
                Comparison comparison = data.get("comparison");
                int compareTo = data.getInt("compare_to");
                switch(comparison) {
                    case EQUAL: case LESS_THAN_OR_EQUAL: case GREATER_THAN:
                        stopAt = compareTo + 1;
                        break;
                    case LESS_THAN: case GREATER_THAN_OR_EQUAL:
                        stopAt = compareTo;
                        break;
                }
                int count = 0;
                for(BlockPos pos : Shape.getPositions(entity.blockPosition(), data.get("shape"), data.getInt("radius"))) {
                    if(blockCondition.test(new BlockInWorld(entity.level(), pos, true))) {
                        count++;
                        if(count == stopAt) {
                            break;
                        }
                    }
                }
                return comparison.compare(count, compareTo);
            }));
        DistanceFromCoordinatesConditionRegistry.registerEntityCondition(EntityConditions::register);
        register(new ConditionFactory<>(Apoli.identifier("dimension"), new SerializableData()
            .add("dimension", SerializableDataTypes.IDENTIFIER),
            (data, entity) -> entity.level().dimension() == ResourceKey.create(Registries.DIMENSION, data.getId("dimension"))));
        register(new ConditionFactory<>(Apoli.identifier("xp_levels"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, entity) -> {
                if(entity instanceof Player) {
                    return ((Comparison)data.get("comparison")).compare(((Player)entity).experienceLevel, data.getInt("compare_to"));
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("xp_points"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, entity) -> {
                if(entity instanceof Player) {
                    return ((Comparison)data.get("comparison")).compare(((Player)entity).totalExperience, data.getInt("compare_to"));
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("health"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, entity) -> ((Comparison)data.get("comparison")).compare(entity instanceof LivingEntity ? ((LivingEntity)entity).getHealth() : 0f, data.getFloat("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("relative_health"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, entity) -> {
                float health = 0f;
                if(entity instanceof LivingEntity living) {
                    health = living.getHealth() / living.getMaxHealth();
                }
                return ((Comparison)data.get("comparison")).compare(health, data.getFloat("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("biome"), new SerializableData()
            .add("biome", SerializableDataTypes.IDENTIFIER, null)
            .add("biomes", SerializableDataTypes.IDENTIFIERS, null)
            .add("condition", ApoliDataTypes.BIOME_CONDITION, null),
            (data, entity) -> {
                Holder<Biome> biomeEntry = entity.level().getBiome(entity.blockPosition());
                Biome biome = biomeEntry.value();
                ConditionFactory<Holder<Biome>>.Instance condition = data.get("condition");
                if(data.isPresent("biome") || data.isPresent("biomes")) {
                    Identifier biomeId = entity.level().registryAccess().lookupOrThrow(Registries.BIOME).getKey(biome);
                    if(data.isPresent("biome") && biomeId.equals(data.getId("biome"))) {
                        return condition == null || condition.test(biomeEntry);
                    }
                    if(data.isPresent("biomes") && ((List<Identifier>)data.get("biomes")).contains(biomeId)) {
                        return condition == null || condition.test(biomeEntry);
                    }
                    return false;
                }
                return condition == null || condition.test(biomeEntry);
            }));
        register(new ConditionFactory<>(Apoli.identifier("entity_type"), new SerializableData()
            .add("entity_type", SerializableDataTypes.ENTITY_TYPE),
            (data, entity) -> entity.getType() == data.get("entity_type")));
        register(ScoreboardCondition.getFactory());
        register(new ConditionFactory<>(Apoli.identifier("command"), new SerializableData()
            .add("command", SerializableDataTypes.STRING)
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, entity) -> {
                MinecraftServer server = entity.level().getServer();
                if(server != null) {
                    // [移植 b7a79a9] source 恒用实体（ServerPlayer.commandSource()），避免 CommandSource.NULL 使 /say 等命令失效。
                    CommandSourceStack source = new CommandSourceStack(
                        entity instanceof ServerPlayer serverPlayer ? serverPlayer.commandSource() : CommandSource.NULL,
                        entity.position(),
                        entity.getRotationVector(),
                        entity.level() instanceof ServerLevel ? (ServerLevel)entity.level() : null,
                        Apoli.config.executeCommand.getPermissionHandler(),
                        entity.getName().getString(),
                        entity.getDisplayName(),
                        server,
                        entity);
                    if(!Apoli.config.executeCommand.showOutput) {
                        source = source.withSuppressedOutput();
                    }
                    int output = 0;
                    try {
                        output = server.getCommands().getDispatcher().execute(data.getString("command").replaceFirst("/", ""), source);
                    } catch (CommandSyntaxException e) {
                        //throw new RuntimeException(e);
                    }
                    return ((Comparison)data.get("comparison")).compare(output, data.getInt("compare_to"));
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("predicate"), new SerializableData()
            .add("predicate", SerializableDataTypes.IDENTIFIER),
            (data, entity) -> {
                MinecraftServer server = entity.level().getServer();
                if (server != null) {
                    LootItemCondition lootCondition = server.reloadableRegistries().lookup().lookupOrThrow(Registries.PREDICATE).getOrThrow(ResourceKey.create(Registries.PREDICATE, (Identifier) data.get("predicate"))).value();
                    if (lootCondition != null) {
                        LootParams lootContextParameterSet = new LootParams.Builder((ServerLevel) entity.level())
                                .withParameter(LootContextParams.ORIGIN, entity.position())
                                .withOptionalParameter(LootContextParams.THIS_ENTITY, entity)
                                .create(LootContextParamSets.COMMAND);
                        LootContext lootContext = new LootContext.Builder(lootContextParameterSet).create(Optional.empty());
                        return lootCondition.test(lootContext);
                    }
                }
                return false;
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("fall_distance"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, entity) -> ((Comparison)data.get("comparison")).compare(entity.fallDistance, data.getFloat("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("collided_horizontally"), new SerializableData(),
            (data, entity) -> entity.horizontalCollision));
        register(new ConditionFactory<>(Apoli.identifier("in_block_anywhere"), new SerializableData()
            .add("block_condition", ApoliDataTypes.BLOCK_CONDITION)
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL)
            .add("compare_to", SerializableDataTypes.INT, 1),
            (data, entity) -> {
                Predicate<BlockInWorld> blockCondition = data.get("block_condition");
                int stopAt = -1;
                Comparison comparison = data.get("comparison");
                int compareTo = data.getInt("compare_to");
                switch(comparison) {
                    case EQUAL: case LESS_THAN_OR_EQUAL: case GREATER_THAN: case NOT_EQUAL:
                        stopAt = compareTo + 1;
                        break;
                    case LESS_THAN: case GREATER_THAN_OR_EQUAL:
                        stopAt = compareTo;
                        break;
                }
                int count = 0;
                AABB box = entity.getBoundingBox();
                BlockPos blockPos = BlockPos.containing(box.minX + 0.001D, box.minY + 0.001D, box.minZ + 0.001D);
                BlockPos blockPos2 = BlockPos.containing(box.maxX - 0.001D, box.maxY - 0.001D, box.maxZ - 0.001D);
                BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
                for(int i = blockPos.getX(); i <= blockPos2.getX() && count < stopAt; ++i) {
                    for(int j = blockPos.getY(); j <= blockPos2.getY() && count < stopAt; ++j) {
                        for(int k = blockPos.getZ(); k <= blockPos2.getZ() && count < stopAt; ++k) {
                            mutable.set(i, j, k);
                            if(blockCondition.test(new BlockInWorld(entity.level(), mutable, true))) {
                                count++;
                            }
                        }
                    }
                }
                return comparison.compare(count, compareTo);}));
        register(new ConditionFactory<>(Apoli.identifier("entity_group"), new SerializableData()
            .add("group", SerializableDataTypes.ENTITY_GROUP),
            (data, entity) -> entity instanceof LivingEntity && ((List<TagKey<EntityType<?>>>) data.get("group")).stream().allMatch(tag -> entity.getType().is(tag))));
        register(new ConditionFactory<>(Apoli.identifier("in_tag"), new SerializableData()
            .add("tag", SerializableDataTypes.ENTITY_TAG),
            (data, entity) -> entity.getType().builtInRegistryHolder().is((TagKey<EntityType<?>>) data.get("tag"))));
        register(new ConditionFactory<>(Apoli.identifier("climbing"), new SerializableData(),
            (data, entity) -> {
                if(entity instanceof LivingEntity && ((LivingEntity)entity).onClimbable()) {
                    return true;
                }
                if(PowerHolderComponent.hasPower(entity, ClimbingPower.class)) {
                    return true;
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("tamed"), new SerializableData(), (data, entity) -> {
            if(entity instanceof TamableAnimal) {
                return ((TamableAnimal)entity).isTame();
            }
            return false;
        }));
        register(new ConditionFactory<>(Apoli.identifier("using_item"), new SerializableData()
            .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null), (data, entity) -> {
            if(entity instanceof LivingEntity living) {
                if (living.isUsingItem()) {
                    ConditionFactory<ItemStack>.Instance condition = data.get("item_condition");
                    if (condition != null) {
                        InteractionHand activeHand = living.getUsedItemHand();
                        ItemStack handStack = living.getItemInHand(activeHand);
                        return condition.test(handStack);
                    } else {
                        return true;
                    }
                }
            }
            return false;
        }));
        register(new ConditionFactory<>(Apoli.identifier("moving"), new SerializableData(),
            (data, entity) -> ((MovingEntity)entity).isMoving()));
        register(new ConditionFactory<>(Apoli.identifier("enchantment"), new SerializableData()
            .add("enchantment", SerializableDataTypes.ENCHANTMENT)
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT)
            .add("calculation", SerializableDataTypes.STRING, "sum"),
            (data, entity) -> {
                int value = 0;
                if(entity instanceof LivingEntity le) {
                    ResourceKey<Enchantment> enchantment = data.get("enchantment");
                    Holder<Enchantment> enchantmentHolder = entity.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment);
                    String calculation = data.getString("calculation");
                    switch(calculation) {
                        case "sum":
                            for(ItemStack stack : enchantmentHolder.value().getSlotItems(le).values()) {
                                value += EnchantmentHelper.getItemEnchantmentLevel(enchantmentHolder, stack);
                            }
                            break;
                        case "max":
                            value = EnchantmentHelper.getEnchantmentLevel(enchantmentHolder, le);
                            break;
                        default:
                            Apoli.LOGGER.error("Error in \"enchantment\" entity condition, undefined calculation type: \"" + calculation + "\".");
                            break;
                    }
                }
                return ((Comparison)data.get("comparison")).compare(value, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("riding"), new SerializableData()
            .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
            (data, entity) -> {
                if(entity.isPassenger()) {
                    if(data.isPresent("bientity_condition")) {
                        Predicate<Tuple<Entity, Entity>> condition = data.get("bientity_condition");
                        Entity vehicle = entity.getVehicle();
                        return condition.test(new Tuple<>(entity, vehicle));
                    }
                    return true;
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("riding_root"), new SerializableData()
            .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
            (data, entity) -> {
                if(entity.isPassenger()) {
                    if(data.isPresent("bientity_condition")) {
                        Predicate<Tuple<Entity, Entity>> condition = data.get("bientity_condition");
                        Entity vehicle = entity.getRootVehicle();
                        return condition.test(new Tuple<>(entity, vehicle));
                    }
                    return true;
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("riding_recursive"), new SerializableData()
            .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL)
            .add("compare_to", SerializableDataTypes.INT, 1),
            (data, entity) -> {
                int count = 0;
                if(entity.isPassenger()) {
                    Predicate<Tuple<Entity, Entity>> cond = data.get("bientity_condition");
                    Entity vehicle = entity.getVehicle();
                    while(vehicle != null) {
                        if(cond == null || cond.test(new Tuple<>(entity, vehicle))) {
                            count++;
                        }
                        vehicle = vehicle.getVehicle();
                    }
                }
                return ((Comparison)data.get("comparison")).compare(count, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("living"), new SerializableData(), (data, entity) -> entity instanceof LivingEntity));
        register(new ConditionFactory<>(Apoli.identifier("passenger"), new SerializableData()
            .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL)
            .add("compare_to", SerializableDataTypes.INT, 1),
            (data, entity) -> {
                int count = 0;
                if(entity.isVehicle()) {
                    if(data.isPresent("bientity_condition")) {
                        Predicate<Tuple<Entity, Entity>> condition = data.get("bientity_condition");
                        count = (int)entity.getPassengers().stream().filter(e -> condition.test(new Tuple<>(e, entity))).count();
                    } else {
                        count = entity.getPassengers().size();
                    }
                }
                return ((Comparison)data.get("comparison")).compare(count, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("passenger_recursive"), new SerializableData()
            .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL)
            .add("compare_to", SerializableDataTypes.INT, 1),
            (data, entity) -> {
                int count = 0;
                if(entity.isVehicle()) {
                    if(data.isPresent("bientity_condition")) {
                        Predicate<Tuple<Entity, Entity>> condition = data.get("bientity_condition");
                        List<Entity> passengers = entity.getPassengers();
                        count = (int)passengers.stream().flatMap(Entity::getSelfAndPassengers).filter(e -> condition.test(new Tuple<>(e, entity))).count();
                    } else {
                        count = (int)entity.getPassengers().stream().flatMap(Entity::getSelfAndPassengers).count();
                    }
                }
                return ((Comparison)data.get("comparison")).compare(count, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("nbt"), new SerializableData()
            .add("nbt", SerializableDataTypes.NBT),
            (data, entity) -> {
                var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, entity.registryAccess());
                entity.saveWithoutId(output);
                return NbtUtils.compareNbt(data.get("nbt"), output.buildResult(), true);
            }));
        register(new ConditionFactory<>(Apoli.identifier("exists"), new SerializableData(), (data, entity) -> entity != null));
        register(new ConditionFactory<>(Apoli.identifier("creative_flying"), new SerializableData(),
            (data, entity) -> entity instanceof Player && ((Player)entity).getAbilities().flying));
        register(new ConditionFactory<>(Apoli.identifier("power_type"), new SerializableData()
            .add("power_type", ApoliDataTypes.POWER_TYPE),
            (data, entity) -> {
                PowerTypeReference<?> powerTypeReference = data.get("power_type");
                PowerType<?> powerType = powerTypeReference.getReferencedPowerType();
                return PowerHolderComponent.KEY.maybeGet(entity).map(phc -> phc.getPowerTypes(true).contains(powerType)).orElse(false);
            }));
        register(new ConditionFactory<>(Apoli.identifier("ability"), new SerializableData()
            .add("ability", ApoliDataTypes.PLAYER_ABILITY),
            (data, entity) -> {
                if(entity instanceof Player && !entity.level().isClientSide()) {
                    return ((PlayerAbility) data.get("ability")).isEnabledFor((Player) entity);
                }
                return false;
            }));
        register(RaycastCondition.getFactory());
        register(ElytraFlightPossibleCondition.getFactory());
        register(InventoryCondition.getFactory());

        // Origins: Legacy
        register(DistanceToGroundCondition.getFactory());
    }

    private static void register(ConditionFactory<Entity> conditionFactory) {
        Registry.register(ApoliRegistries.ENTITY_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
