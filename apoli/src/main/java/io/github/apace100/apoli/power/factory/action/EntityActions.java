package io.github.apace100.apoli.power.factory.action;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.power.factory.action.entity.*;
import io.github.apace100.apoli.power.factory.action.meta.*;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.apoli.util.ResourceOperation;
import io.github.apace100.apoli.util.Space;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Vector3f;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EntityActions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(AndAction.getFactory(ApoliDataTypes.ENTITY_ACTIONS));
        register(ChanceAction.getFactory(ApoliDataTypes.ENTITY_ACTION));
        register(IfElseAction.getFactory(ApoliDataTypes.ENTITY_ACTION, ApoliDataTypes.ENTITY_CONDITION));
        register(ChoiceAction.getFactory(ApoliDataTypes.ENTITY_ACTION));
        register(IfElseListAction.getFactory(ApoliDataTypes.ENTITY_ACTION, ApoliDataTypes.ENTITY_CONDITION));
        register(DelayAction.getFactory(ApoliDataTypes.ENTITY_ACTION));
        register(NothingAction.getFactory());
        register(SideAction.getFactory(ApoliDataTypes.ENTITY_ACTION, entity -> !entity.level().isClientSide()));

        register(new ActionFactory<>(Apoli.identifier("damage"), new SerializableData()
            .add("amount", SerializableDataTypes.FLOAT)
            .add("source", ApoliDataTypes.DAMAGE_SOURCE_DESCRIPTION, null)
            .add("damage_type", SerializableDataTypes.DAMAGE_TYPE, null),
            (data, entity) -> {
                DamageSource damageSource = MiscUtil.createDamageSource(entity.damageSources(), data.get("source"), data.get("damage_type"));
                entity.hurt(damageSource, data.getFloat("amount"));
            }));
        register(new ActionFactory<>(Apoli.identifier("heal"), new SerializableData()
            .add("amount", SerializableDataTypes.FLOAT),
            (data, entity) -> {
                if(entity instanceof LivingEntity) {
                    ((LivingEntity)entity).heal(data.getFloat("amount"));
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("play_sound"), new SerializableData()
                .add("sound", SerializableDataTypes.SOUND_EVENT)
                .add("volume", SerializableDataTypes.FLOAT, 1F)
                .add("pitch", SerializableDataTypes.FLOAT, 1F),
                (data, entity) -> {
                    SoundSource category;
                    if(entity instanceof Player) {
                        category = SoundSource.PLAYERS;
                    } else
                    if(entity instanceof Monster) {
                        category = SoundSource.HOSTILE;
                    } else {
                        category = SoundSource.NEUTRAL;
                    }
                    entity.level().playSound(null, (entity).getX(), (entity).getY(), (entity).getZ(), (SoundEvent) data.get("sound"),
                        category, data.getFloat("volume"), data.getFloat("pitch"));
                }));
        register(new ActionFactory<>(Apoli.identifier("exhaust"), new SerializableData()
            .add("amount", SerializableDataTypes.FLOAT),
            (data, entity) -> {
                if(entity instanceof Player)
                    ((Player)entity).getFoodData().addExhaustion(data.getFloat("amount"));
            }));
        register(new ActionFactory<>(Apoli.identifier("apply_effect"), new SerializableData()
            .add("effect", SerializableDataTypes.STATUS_EFFECT_INSTANCE, null)
            .add("effects", SerializableDataTypes.STATUS_EFFECT_INSTANCES, null),
            (data, entity) -> {
                if(entity instanceof LivingEntity le && !entity.level().isClientSide()) {
                    if(data.isPresent("effect")) {
                        MobEffectInstance effect = data.get("effect");
                        le.addEffect(new MobEffectInstance(effect));
                    }
                    if(data.isPresent("effects")) {
                        ((List<MobEffectInstance>)data.get("effects")).forEach(e -> le.addEffect(new MobEffectInstance(e)));
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("clear_effect"), new SerializableData()
            .add("effect", SerializableDataType.holder(BuiltInRegistries.MOB_EFFECT), null),
            (data, entity) -> {
                if(entity instanceof LivingEntity le) {
                    if(data.isPresent("effect")) {
                        le.removeEffect(data.get("effect"));
                    } else {
                        le.removeAllEffects();
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("set_on_fire"), new SerializableData()
            .add("duration", SerializableDataTypes.INT),
            (data, entity) -> entity.setRemainingFireTicks(data.getInt("duration") * 20)));
        register(new ActionFactory<>(Apoli.identifier("add_velocity"), new SerializableData()
            .add("x", SerializableDataTypes.FLOAT, 0F)
            .add("y", SerializableDataTypes.FLOAT, 0F)
            .add("z", SerializableDataTypes.FLOAT, 0F)
            .add("space", ApoliDataTypes.SPACE, Space.WORLD)
            .add("client", SerializableDataTypes.BOOLEAN, true)
            .add("server", SerializableDataTypes.BOOLEAN, true)
            .add("set", SerializableDataTypes.BOOLEAN, false),
            (data, entity) -> {
                if (entity instanceof Player
                    && (entity.level().isClientSide() ?
                    !data.getBoolean("client") : !data.getBoolean("server")))
                    return;
                Space space = data.get("space");
                Vector3f vec = new Vector3f(data.getFloat("x"), data.getFloat("y"), data.getFloat("z"));
                TriConsumer<Float, Float, Float> method = entity::push;
                if(data.getBoolean("set")) {
                    method = entity::setDeltaMovement;
                }
                space.toGlobal(vec, entity);
                method.accept(vec.x, vec.y, vec.z);
                entity.hurtMarked = true;
            }));
        register(SpawnEntityAction.getFactory());
        register(new ActionFactory<>(Apoli.identifier("gain_air"), new SerializableData()
            .add("value", SerializableDataTypes.INT),
            (data, entity) -> {
                if(entity instanceof LivingEntity le) {
                    le.setAirSupply(Math.min(le.getAirSupply() + data.getInt("value"), le.getMaxAirSupply()));
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("block_action_at"), new SerializableData()
            .add("block_action", ApoliDataTypes.BLOCK_ACTION),
            (data, entity) -> ((ActionFactory<Triple<Level, BlockPos, Direction>>.Instance)data.get("block_action")).accept(
                Triple.of(entity.level(), entity.blockPosition(), Direction.UP))));
        register(new ActionFactory<>(Apoli.identifier("spawn_effect_cloud"), new SerializableData()
            .add("radius", SerializableDataTypes.FLOAT, 3.0F)
            .add("radius_on_use", SerializableDataTypes.FLOAT, -0.5F)
            .add("wait_time", SerializableDataTypes.INT, 10)
            .add("effect", SerializableDataTypes.STATUS_EFFECT_INSTANCE, null)
            .add("effects", SerializableDataTypes.STATUS_EFFECT_INSTANCES, null),
            (data, entity) -> {
                AreaEffectCloud areaEffectCloudEntity = new AreaEffectCloud(entity.level(), entity.getX(), entity.getY(), entity.getZ());
                if (entity instanceof LivingEntity) {
                    areaEffectCloudEntity.setOwner((LivingEntity)entity);
                }
                areaEffectCloudEntity.setRadius(data.getFloat("radius"));
                areaEffectCloudEntity.setRadiusOnUse(data.getFloat("radius_on_use"));
                areaEffectCloudEntity.setWaitTime(data.getInt("wait_time"));
                areaEffectCloudEntity.setRadiusPerTick(-areaEffectCloudEntity.getRadius() / (float)areaEffectCloudEntity.getDuration());
                List<MobEffectInstance> effects = new LinkedList<>();
                if(data.isPresent("effect")) {
                    effects.add(data.get("effect"));
                }
                if(data.isPresent("effects")) {
                    effects.addAll(data.get("effects"));
                }
                areaEffectCloudEntity.setCustomParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, ARGB.opaque(PotionContents.getColorOptional(effects).orElse(PotionContents.BASE_POTION_COLOR))));
                effects.forEach(areaEffectCloudEntity::addEffect);

                entity.level().addFreshEntity(areaEffectCloudEntity);
            }));
        register(new ActionFactory<>(Apoli.identifier("extinguish"), new SerializableData(),
            (data, entity) -> entity.clearFire()));
        register(new ActionFactory<>(Apoli.identifier("execute_command"), new SerializableData()
            .add("command", SerializableDataTypes.STRING),
            (data, entity) -> {
                // [移植 b7a79a9] 只有实体位于服务端维度（ServerLevel）才执行：particle 等命令需要 source.getLevel()，
                // 客户端/非服务端租 level 非 ServerLevel → source.getLevel()=null → NPE。
                if(!(entity.level() instanceof ServerLevel)) {
                    return;
                }
                MinecraftServer server = entity.level().getServer();
                if(server != null) {
                    // 修复：source 总是用实体（ServerPlayer.commandSource()），避免 CommandSource.NULL 抑制 /say 输出及使 /give @s 等命令失效。
                    CommandSourceStack source = new CommandSourceStack(
                        entity instanceof ServerPlayer serverPlayer ? serverPlayer.commandSource()
                            : CommandSource.NULL,
                        entity.position(),
                        entity.getRotationVector(),
                        (ServerLevel)entity.level(),
                        Apoli.config.executeCommand.getPermissionHandler(),
                        entity.getName().getString(),
                        entity.getDisplayName(),
                        server,
                        entity);
                    // showOutput=false（默认）时抑制命令反馈广播（/give 聊天提示等），但保留命令执行效果。
                    if(!Apoli.config.executeCommand.showOutput) {
                        source = source.withSuppressedOutput();
                    }
                    try {
                        // dispatcher.execute 不剥前导 /（聊天栏的 / 由 MC 剥离后才传入），带 / 会在 position 0 报"未知命令"，这里手动剥掉。
                        String execCommand = data.getString("command").trim();
                        if(execCommand.startsWith("/")) {
                            execCommand = execCommand.substring(1);
                        }
                        int result = server.getCommands().getDispatcher().execute(execCommand, source);
                    } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("change_resource"), new SerializableData()
            .add("resource", ApoliDataTypes.POWER_TYPE)
            .add("change", SerializableDataTypes.INT)
            .add("operation", ApoliDataTypes.RESOURCE_OPERATION, ResourceOperation.ADD),
            (data, entity) -> {
                if(entity instanceof LivingEntity) {
                    PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
                    PowerType<?> powerType = data.get("resource");
                    Power p = component.getPower(powerType);
                    ResourceOperation operation = data.get("operation");
                    int change = data.getInt("change");
                    if(p instanceof VariableIntPower vip) {
                        if (operation == ResourceOperation.ADD) {
                            int newValue = vip.getValue() + change;
                            vip.setValue(newValue);
                        } else if (operation == ResourceOperation.SET) {
                            vip.setValue(change);
                        }
                        PowerHolderComponent.syncPower(entity, powerType);
                    } else if(p instanceof CooldownPower cp) {
                        if (operation == ResourceOperation.ADD) {
                            cp.modify(change);
                        } else if (operation == ResourceOperation.SET) {
                            cp.setCooldown(change);
                        }
                        PowerHolderComponent.syncPower(entity, powerType);
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("feed"), new SerializableData()
            .add("food", SerializableDataTypes.INT)
            .add("saturation", SerializableDataTypes.FLOAT),
            (data, entity) -> {
                if(entity instanceof Player) {
                    ((Player)entity).getFoodData().eat(data.getInt("food"), data.getFloat("saturation"));
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("add_xp"), new SerializableData()
            .add("points", SerializableDataTypes.INT, 0)
            .add("levels", SerializableDataTypes.INT, 0),
            (data, entity) -> {
                if(entity instanceof Player) {
                    int points = data.getInt("points");
                    int levels = data.getInt("levels");
                    if(points > 0) {
                        ((Player)entity).giveExperiencePoints(points);
                    }
                    ((Player)entity).giveExperienceLevels(levels);
                }
            }));

        register(new ActionFactory<>(Apoli.identifier("set_fall_distance"), new SerializableData()
            .add("fall_distance", SerializableDataTypes.FLOAT),
            (data, entity) -> entity.fallDistance = data.getFloat("fall_distance")));
        register(new ActionFactory<>(Apoli.identifier("give"), new SerializableData()
            .add("stack", SerializableDataTypes.ITEM_STACK)
            .add("item_action", ApoliDataTypes.ITEM_ACTION, null)
            .add("preferred_slot", SerializableDataTypes.EQUIPMENT_SLOT, null),
            (data, entity) -> {
                if(!entity.level().isClientSide()) {
                    ItemStack stack = data.get("stack");
                    if(stack.isEmpty()) {
                        return;
                    }
                    stack = stack.copy();
                    if(data.isPresent("item_action")) {
                        ActionFactory<Tuple<Level, ItemStack>>.Instance action = data.get("item_action");
                        action.accept(new Tuple<>(entity.level(), stack));
                    }
                    if(data.isPresent("preferred_slot") && entity instanceof LivingEntity living) {
                        EquipmentSlot slot = data.get("preferred_slot");
                        ItemStack stackInSlot = living.getItemBySlot(slot);
                        if(stackInSlot.isEmpty()) {
                            living.setItemSlot(slot, stack);
                            return;
                        } else
                        if(ItemStack.isSameItemSameComponents(stackInSlot, stack) && stackInSlot.getCount() < stackInSlot.getMaxStackSize()) {
                            int fit = Math.min(stackInSlot.getMaxStackSize() - stackInSlot.getCount(), stack.getCount());
                            stackInSlot.grow(fit);
                            stack.shrink(fit);
                            if(stack.isEmpty()) {
                                return;
                            }
                        }
                    }
                    if(entity instanceof Player) {
                        ((Player)entity).getInventory().placeItemBackInInventory(stack);
                    } else {
                        entity.level().addFreshEntity(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), stack));
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("equipped_item_action"), new SerializableData()
            .add("equipment_slot", SerializableDataTypes.EQUIPMENT_SLOT)
            .add("action", ApoliDataTypes.ITEM_ACTION),
            (data, entity) -> {
                if(entity instanceof LivingEntity) {
                    ItemStack stack = ((LivingEntity)entity).getItemBySlot(data.get("equipment_slot"));
                    ActionFactory<Tuple<Level, ItemStack>>.Instance action = data.get("action");
                    action.accept(new Tuple<>(entity.level(), stack));
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("trigger_cooldown"), new SerializableData()
            .add("power", ApoliDataTypes.POWER_TYPE),
            (data, entity) -> {
                if(entity instanceof LivingEntity) {
                    PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
                    Power p = component.getPower((PowerType<?>)data.get("power"));
                    if(p instanceof CooldownPower cp) {
                        cp.use();
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("toggle"), new SerializableData()
            .add("power", ApoliDataTypes.POWER_TYPE),
            (data, entity) -> {
                if(entity instanceof LivingEntity) {
                    PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
                    Power p = component.getPower((PowerType<?>)data.get("power"));
                    if(p instanceof TogglePower) {
                        ((TogglePower)p).onUse();
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("emit_game_event"), new SerializableData()
            .add("event", SerializableDataTypes.GAME_EVENT),
            (data, entity) -> entity.gameEvent(data.get("event"))));
        register(new ActionFactory<>(Apoli.identifier("set_resource"), new SerializableData()
            .add("resource", ApoliDataTypes.POWER_TYPE)
            .add("value", SerializableDataTypes.INT),
            (data, entity) -> {
                if(entity instanceof LivingEntity) {
                    PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
                    PowerType<?> powerType = data.get("resource");
                    Power p = component.getPower(powerType);
                    int value = data.getInt("value");
                    if(p instanceof VariableIntPower vip) {
                        vip.setValue(value);
                        PowerHolderComponent.syncPower(entity, powerType);
                    } else if(p instanceof CooldownPower cp) {
                        cp.setCooldown(value);
                        PowerHolderComponent.syncPower(entity, powerType);
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("grant_power"), new SerializableData()
            .add("power", ApoliDataTypes.POWER_TYPE)
            .add("source", SerializableDataTypes.IDENTIFIER),
            (data, entity) -> PowerHolderComponent.KEY.maybeGet(entity).ifPresent(component -> {
                component.addPower(data.get("power"), data.getId("source"));
                component.sync();
            })));
        register(new ActionFactory<>(Apoli.identifier("revoke_power"), new SerializableData()
            .add("power", ApoliDataTypes.POWER_TYPE)
            .add("source", SerializableDataTypes.IDENTIFIER),
            (data, entity) -> PowerHolderComponent.KEY.maybeGet(entity).ifPresent(component -> {
                component.removePower(data.get("power"), data.getId("source"));
                component.sync();
            })));
        register(ExplodeAction.getFactory());
        register(new ActionFactory<>(Apoli.identifier("dismount"), new SerializableData(),
            (data, entity) -> entity.stopRiding()));
        register(new ActionFactory<>(Apoli.identifier("passenger_action"), new SerializableData()
            .add("action", ApoliDataTypes.ENTITY_ACTION, null)
            .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null)
            .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
            .add("recursive", SerializableDataTypes.BOOLEAN, false),
            (data, entity) -> {
                Consumer<Entity> entityAction = data.get("action");
                Consumer<Tuple<Entity, Entity>> bientityAction = data.get("bientity_action");
                Predicate<Tuple<Entity, Entity>> cond = data.get("bientity_condition");
                if(!entity.isVehicle() || (entityAction == null && bientityAction == null)) {
                    return;
                }
                Iterable<Entity> passengers = data.getBoolean("recursive") ? entity.getIndirectPassengers() : entity.getPassengers();
                for(Entity passenger : passengers) {
                    if(cond == null || cond.test(new Tuple<>(passenger, entity))) {
                        if (entityAction != null) {
                            entityAction.accept(passenger);
                        }
                        if (bientityAction != null) {
                            bientityAction.accept(new Tuple<>(passenger, entity));
                        }
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("riding_action"), new SerializableData()
            .add("action", ApoliDataTypes.ENTITY_ACTION, null)
            .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null)
            .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
            .add("recursive", SerializableDataTypes.BOOLEAN, false),
            (data, entity) -> {
                Consumer<Entity> entityAction = data.get("action");
                Consumer<Tuple<Entity, Entity>> bientityAction = data.get("bientity_action");
                Predicate<Tuple<Entity, Entity>> cond = data.get("bientity_condition");
                if(!entity.isPassenger() || (entityAction == null && bientityAction == null)) {
                    return;
                }
                if(data.getBoolean("recursive")) {
                    Entity vehicle = entity.getVehicle();
                    while(vehicle != null) {
                        if(cond == null || cond.test(new Tuple<>(entity, vehicle))) {
                            if(entityAction != null) {
                                entityAction.accept(vehicle);
                            }
                            if(bientityAction != null) {
                                bientityAction.accept(new Tuple<>(entity, vehicle));
                            }
                        }
                        vehicle = vehicle.getVehicle();
                    }
                } else {
                    Entity vehicle = entity.getVehicle();
                    if(cond == null || cond.test(new Tuple<>(entity, vehicle))) {
                        if(entityAction != null) {
                            entityAction.accept(vehicle);
                        }
                        if(bientityAction != null) {
                            bientityAction.accept(new Tuple<>(entity, vehicle));
                        }
                    }
                }
            }));
        register(AreaOfEffectAction.getFactory());
        register(CraftingTableAction.getFactory());
        register(EnderChestAction.getFactory());
        register(SwingHandAction.getFactory());
        register(RaycastAction.getFactory());
        register(SpawnParticlesAction.getFactory());
        register(ModifyInventoryAction.getFactory());
        register(ReplaceInventoryAction.getFactory());
        register(DropInventoryAction.getFactory());
        register(ModifyDeathTicksAction.getFactory());
        register(ModifyResourceAction.getFactory());
        register(ModifyStatAction.getFactory());
        register(FireProjectileAction.getFactory());
        register(SelectorAction.getFactory());
        register(GrantAdvancementAction.getFactory());
        register(RevokeAdvancementAction.getFactory());
    }

    private static void register(ActionFactory<Entity> actionFactory) {
        Registry.register(ApoliRegistries.ENTITY_ACTION, actionFactory.getSerializerId(), actionFactory);
    }
}
