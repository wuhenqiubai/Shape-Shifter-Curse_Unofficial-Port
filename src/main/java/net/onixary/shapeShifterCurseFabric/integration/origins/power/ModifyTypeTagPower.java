package net.onixary.shapeShifterCurseFabric.integration.origins.power;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * Makes the entity be considered as being in the specified entity type tag.
 * Replacement for the removed {@code apoli:entity_group} power type.
 * Neo-forged based on the Apoli dev docs (alpha 6 addition).
 */
public class ModifyTypeTagPower extends Power {

    /**
     * ThreadLocal holding the entity currently executing an entity-type tag check.
     * 注(Apoli-Legacy 2.11.4 降级): 上游由 InTagConditionMixin 设置，但 Legacy 无 InTagCondition 类，
     * 该 mixin 已移除，CURRENT_ENTITY 恒为 null → modify_type_tag 的 tag 效果不生效（见 ModifyTypeTagMixin 注释）。
     */
    public static final ThreadLocal<net.minecraft.world.entity.Entity> CURRENT_ENTITY = new ThreadLocal<>();

    private final TagKey<EntityType<?>> tagKey;

    /**
     * Global set of entities that have active ModifyTypeTagPower instances.
     * Used by the Entity mixin to check tag membership efficiently.
     * Keyed by entity ID, stores set of tag keys the entity should be considered in.
     */
    private static final Set<Entry> ACTIVE_ENTRIES = new HashSet<>();

    public ModifyTypeTagPower(PowerType<?> type, LivingEntity entity, TagKey<EntityType<?>> tagKey) {
        super(type, entity);
        this.tagKey = tagKey;
    }

    public TagKey<EntityType<?>> getTagKey() {
        return tagKey;
    }

    @Override
    public void onAdded() {
        super.onAdded();
        synchronized (ACTIVE_ENTRIES) {
            ACTIVE_ENTRIES.add(new Entry(entity.getId(), tagKey));
        }
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        synchronized (ACTIVE_ENTRIES) {
            ACTIVE_ENTRIES.remove(new Entry(entity.getId(), tagKey));
        }
    }

    /**
     * Checks if the given entity is considered to be in the given tag
     * by any active ModifyTypeTagPower.
     */
    public static boolean isEntityInTag(int entityId, TagKey<EntityType<?>> tag) {
        synchronized (ACTIVE_ENTRIES) {
            return ACTIVE_ENTRIES.contains(new Entry(entityId, tag));
        }
    }

    private record Entry(int entityId, TagKey<EntityType<?>> tag) {}
}