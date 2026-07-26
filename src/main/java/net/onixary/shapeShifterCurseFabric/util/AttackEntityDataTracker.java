package net.onixary.shapeShifterCurseFabric.util;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import java.util.HashMap;
import java.util.UUID;

public class AttackEntityDataTracker {
    /*
    // 原版经过混淆 没法通过反射获取具体Class 就算有框架也只能用jar使用
    public static final HashSet<Class<? extends Entity>> TrackerEntitySetStrict = new HashSet<>();
    public static final HashSet<Class<? extends Entity>> TrackerEntitySet = new HashSet<>();
    public static final HashMap<UUID, HashMap<Class<? extends Entity>, Long>> TrackerMap = new HashMap<>();


    public static void registerEntityTrackerStrict(Class<? extends Entity> entityClass) {
        if (TrackerEntitySetStrict.contains(entityClass)) {
            return;
        }
        TrackerEntitySetStrict.add(entityClass);
    }

    public static void registerEntityTracker(Class<? extends Entity> entityClass) {
        if (TrackerEntitySet.contains(entityClass)) {
            return;
        }
        TrackerEntitySet.add(entityClass);
    }

    public static void onPlayerAttack(PlayerEntity player, Entity target, World world) {
        UUID playerId = player.getUuid();
        Class<? extends Entity> targetClass = target.getClass();
        if (TrackerEntitySetStrict.contains(targetClass)) {
            TrackerMap.computeIfAbsent(playerId, k -> new HashMap<>()).put(targetClass, world.getTime());
        }
        for (Class<? extends Entity> entityClass : TrackerEntitySet) {
            if (entityClass.isInstance(target)) {
                TrackerMap.computeIfAbsent(playerId, k -> new HashMap<>()).put(entityClass, world.getTime());
            }
        }
        return;
    }
     */

    public static final HashMap<UUID, Long> lastAttackPillagerTimeMap = new HashMap<>();
    public static final HashMap<UUID, Long> lastAttackWitchTimeMap = new HashMap<>();

    public static void onPlayerAttack(Player player, Entity target, Level world) {
        if (target instanceof Witch) {
            lastAttackWitchTimeMap.put(player.getUUID(), world.getGameTime());
        }
        if (target instanceof LivingEntity livingEntity) {
            if (livingEntity.getType().is(ModTags.Illager_Tag)) {
                lastAttackPillagerTimeMap.put(player.getUUID(), world.getGameTime());
            }
        }
    }

    public static void init() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            onPlayerAttack(player, entity, world);
            return InteractionResult.PASS;
        });
    }
}
